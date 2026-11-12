import { existsSync } from 'node:fs';
import { extname, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

/**
 * Mock of the FTC Robot Controller dashboard backend (see `TeamCode/Core.java`).
 *
 * - HTTP on :8080   serves `/dashboard/*`, including the health endpoint and the
 *                   static build in `Web/build` (after `bun run build`).
 * - WS   on :8081   broadcasts `log` and `position` messages to clients that
 *                   subscribe to the `dashboard` namespace.
 */

const PROJECT_ROOT = fileURLToPath(new URL('..', import.meta.url));
const STATIC_DIR = resolve(PROJECT_ROOT, process.env.MOCK_STATIC_DIR ?? 'build');

const HTTP_PORT = Number(process.env.MOCK_HTTP_PORT ?? 8080);
const WS_PORT = Number(process.env.MOCK_WS_PORT ?? 8081);

const ROUTE_PREFIX = '/dashboard';
const NAMESPACE = 'dashboard';
const POSITION_INTERVAL_MS = 100;

type LogType = 'debug' | 'info' | 'warn' | 'error';

type LogMessage = {
	type: LogType;
	tag: string;
	msg: () => string;
};

type Position = {
	x: number;
	y: number;
	rotation: number;
};

const subscribers = new Set<ServerWebSocket<unknown>>();
const subscriptions = new WeakMap<ServerWebSocket<unknown>, Set<string>>();

let healthy = process.env.MOCK_UNHEALTHY !== '1';
let autonomousPath: { x: number; y: number; tolerance: number }[] = [];
let positionOverride: (Position & { until: number }) | null = null;

const startedAt = Date.now();

const MIME_TYPES: Record<string, string> = {
	'.css': 'text/css; charset=utf-8',
	'.html': 'text/html; charset=utf-8',
	'.ico': 'image/x-icon',
	'.js': 'text/javascript; charset=utf-8',
	'.json': 'application/json; charset=utf-8',
	'.map': 'application/json; charset=utf-8',
	'.png': 'image/png',
	'.svg': 'image/svg+xml',
	'.txt': 'text/plain; charset=utf-8',
	'.webp': 'image/webp',
	'.woff': 'font/woff',
	'.woff2': 'font/woff2'
};

const LOG_MESSAGES: LogMessage[] = [
	{
		type: 'info',
		tag: 'Core',
		msg: () => 'Dashboard mock server connected'
	},
	{
		type: 'debug',
		tag: 'Odometer',
		msg: () => {
			const position = simulatedPosition();
			return `x=${position.x.toFixed(2)} y=${position.y.toFixed(2)} heading=${position.rotation.toFixed(2)}`;
		}
	},
	{
		type: 'info',
		tag: 'Driver',
		msg: () => 'Driver input received: left_stick_y=0.42 right_stick_x=-0.18'
	},
	{
		type: 'debug',
		tag: 'Vision',
		msg: () => `AprilTag 24 detected (${(85 + Math.random() * 10).toFixed(1)}% confidence)`
	},
	{
		type: 'info',
		tag: 'Autonomous',
		msg: () => 'Path segment complete, advancing to next waypoint'
	},
	{
		type: 'warn',
		tag: 'Drivetrain',
		msg: () => 'Motor 2 current draw above soft limit, throttling'
	},
	{
		type: 'warn',
		tag: 'Vision',
		msg: () => 'AprilTag not visible, falling back to odometry'
	},
	{
		type: 'error',
		tag: 'Scheduler',
		msg: () => 'Task "sample-intake" exceeded its deadline by 42ms'
	},
	{
		type: 'debug',
		tag: 'Scheduler',
		msg: () => `${4 + Math.floor(Math.random() * 8)} tasks queued this cycle`
	},
	{
		type: 'info',
		tag: 'Telemetry',
		msg: () => 'Battery 12.6V · loop time 7.3ms'
	}
];

function simulatedPosition(now = Date.now()): Position {
	const t = (now - startedAt) / 1000;

	const x = 50 + 28 * Math.cos(t * 0.55);
	const y = 50 + 28 * Math.sin(t * 0.9);

	const dx = -28 * 0.55 * Math.sin(t * 0.55);
	const dy = 28 * 0.9 * Math.cos(t * 0.9);

	return {
		x: round(x, 2),
		y: round(y, 2),
		rotation: round(Math.atan2(dy, dx), 3)
	};
}

function currentPosition(): Position {
	if (positionOverride && positionOverride.until > Date.now()) {
		return {
			x: positionOverride.x,
			y: positionOverride.y,
			rotation: positionOverride.rotation
		};
	}

	positionOverride = null;
	return simulatedPosition();
}

function round(value: number, digits: number): number {
	const factor = 10 ** digits;
	return Math.round(value * factor) / factor;
}

function broadcast(type: string, payload: string) {
	const message = JSON.stringify({ namespace: NAMESPACE, type, payload });

	for (const socket of subscribers) {
		try {
			socket.send(message);
		} catch {
			subscribers.delete(socket);
		}
	}
}

function broadcastPosition(position: Position) {
	broadcast('position', JSON.stringify(position));
}

function sendLog(log: { type: LogType; tag: string; msg: string }) {
	broadcast('log', JSON.stringify(log));
}

function scheduleLog() {
	const delay = 900 + Math.random() * 2200;

	setTimeout(() => {
		if (subscribers.size > 0) {
			const entry = LOG_MESSAGES[Math.floor(Math.random() * LOG_MESSAGES.length)];
			sendLog({ type: entry.type, tag: entry.tag, msg: entry.msg() });
		}

		scheduleLog();
	}, delay);
}

function textResponse(body: string, status = 200): Response {
	return new Response(body, {
		status,
		headers: {
			'Content-Type': 'text/plain; charset=utf-8',
			'Access-Control-Allow-Origin': '*'
		}
	});
}

function jsonResponse(body: unknown, status = 200): Response {
	return new Response(JSON.stringify(body), {
		status,
		headers: {
			'Content-Type': 'application/json; charset=utf-8',
			'Access-Control-Allow-Origin': '*'
		}
	});
}

async function readJson(request: Request): Promise<Record<string, unknown>> {
	try {
		const body = await request.json();
		return body && typeof body === 'object' ? (body as Record<string, unknown>) : {};
	} catch {
		return {};
	}
}

async function serveStatic(pathname: string): Promise<Response> {
	const relativePath = pathname.slice(ROUTE_PREFIX.length + 1) || 'index.html';
	const filePath = resolve(STATIC_DIR, decodeURIComponent(relativePath));

	if (filePath !== STATIC_DIR && !filePath.startsWith(STATIC_DIR + sep)) {
		return textResponse('Forbidden', 403);
	}

	const file = Bun.file(filePath);

	if (await file.exists()) {
		return new Response(file, {
			headers: {
				'Content-Type': MIME_TYPES[extname(filePath).toLowerCase()] ?? 'application/octet-stream',
				'Access-Control-Allow-Origin': '*'
			}
		});
	}

	// SPA fallback: extension-less routes are handled by the client router.
	if (!extname(filePath)) {
		const index = Bun.file(resolve(STATIC_DIR, 'index.html'));
		if (await index.exists()) {
			return new Response(index, {
				headers: {
					'Content-Type': MIME_TYPES['.html'],
					'Access-Control-Allow-Origin': '*'
				}
			});
		}
	}

	return textResponse(
		`Not found: ${pathname}\n\nNo static build at ${STATIC_DIR}.\nRun "bun run build" to build the dashboard, or use "bun run dev" with the Vite proxy.`,
		404
	);
}

const httpServer = Bun.serve({
	port: HTTP_PORT,
	async fetch(request) {
		const url = new URL(request.url);
		const pathname = url.pathname;

		if (request.method === 'OPTIONS') {
			return new Response(null, {
				status: 204,
				headers: {
					'Access-Control-Allow-Origin': '*',
					'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
					'Access-Control-Allow-Headers': 'Content-Type'
				}
			});
		}

		if (pathname === `${ROUTE_PREFIX}/api/get-health`) {
			return textResponse(healthy ? 'ok' : 'unhealthy', healthy ? 200 : 503);
		}

		if (pathname === `${ROUTE_PREFIX}/api/trigger-init`) {
			return textResponse('ok');
		}

		if (pathname === `${ROUTE_PREFIX}/api/autonomous/get-path`) {
			return jsonResponse(autonomousPath);
		}

		if (pathname === `${ROUTE_PREFIX}/api/autonomous/set-path`) {
			if (request.method !== 'POST') return textResponse('POST only', 405);

			let body: unknown;
			try {
				body = await request.json();
			} catch {
				return textResponse('Invalid path: body is not JSON', 400);
			}

			if (!Array.isArray(body)) return textResponse('Invalid path: expected an array', 400);

			const path = [];
			for (const [i, point] of body.entries()) {
				const { x, y, tolerance } = (point ?? {}) as Record<string, unknown>;
				const valid =
					[x, y, tolerance].every((n) => typeof n === 'number' && Number.isFinite(n)) &&
					(x as number) >= 0 &&
					(x as number) <= 100 &&
					(y as number) >= 0 &&
					(y as number) <= 100 &&
					(tolerance as number) > 0;

				if (!valid) return textResponse(`Invalid path: Point ${i} is out of range`, 400);
				path.push({ x: x as number, y: y as number, tolerance: tolerance as number });
			}

			autonomousPath = path;
			sendLog({
				type: 'info',
				tag: 'Autonomous',
				msg: `Autonomous path updated with ${path.length} points`
			});
			return textResponse('ok');
		}

		// Mock controls, handy for exercising the dashboard UI manually.
		if (pathname === `${ROUTE_PREFIX}/api/mock/health` && request.method === 'POST') {
			const body = await readJson(request);
			healthy = body.healthy !== false;
			return jsonResponse({ healthy });
		}

		if (pathname === `${ROUTE_PREFIX}/api/mock/log` && request.method === 'POST') {
			const body = await readJson(request);
			const type = isLogType(body.type) ? body.type : 'info';
			const tag = typeof body.tag === 'string' ? body.tag : 'Mock';
			const msg = typeof body.msg === 'string' ? body.msg : 'Manual mock log';

			sendLog({ type, tag, msg });
			return jsonResponse({ type, tag, msg }, 202);
		}

		if (pathname === `${ROUTE_PREFIX}/api/mock/position` && request.method === 'POST') {
			const body = await readJson(request);
			const fallback = currentPosition();
			const position: Position = {
				x: numberOr(body.x, fallback.x),
				y: numberOr(body.y, fallback.y),
				rotation: numberOr(body.rotation, fallback.rotation)
			};

			positionOverride = { ...position, until: Date.now() + 2000 };
			broadcastPosition(position);

			return jsonResponse(position, 202);
		}

		if (pathname === ROUTE_PREFIX) {
			return new Response(null, {
				status: 302,
				headers: { Location: `${ROUTE_PREFIX}/` }
			});
		}

		if (pathname === '/') {
			return new Response(null, {
				status: 302,
				headers: { Location: `${ROUTE_PREFIX}/` }
			});
		}

		if (pathname === `${ROUTE_PREFIX}/` || pathname.startsWith(`${ROUTE_PREFIX}/`)) {
			return serveStatic(pathname);
		}

		return textResponse('Not found', 404);
	}
});

const wsServer = Bun.serve({
	port: WS_PORT,
	fetch(request, server) {
		if (server.upgrade(request)) return;

		return textResponse('This endpoint only accepts WebSocket connections.', 426);
	},
	websocket: {
		open(socket) {
			console.log('[mock] WebSocket client connected');
			subscriptions.set(socket, new Set());
		},
		message(socket, raw) {
			let message: Record<string, unknown>;

			try {
				message = JSON.parse(String(raw));
			} catch {
				console.warn('[mock] Ignoring invalid WebSocket message:', raw);
				return;
			}

			const type = message.type;

			if (type === 'subscribeToNamespace') {
				const namespace = namespaceOf(message);

				if (namespace === NAMESPACE) {
					subscriptions.get(socket)?.add(NAMESPACE);
					subscribers.add(socket);

					console.log('[mock] Client subscribed to the dashboard namespace');
					socket.send(
						JSON.stringify({
							namespace: NAMESPACE,
							type: 'subscribed',
							payload: NAMESPACE
						})
					);

					// Give the dashboard something immediately instead of waiting for the next tick.
					broadcastPosition(currentPosition());
					sendLog({ type: 'info', tag: 'Core', msg: 'Subscribed to dashboard namespace' });
				}

				return;
			}

			if (type === 'unsubscribeFromNamespace') {
				const namespace = namespaceOf(message);

				if (namespace === NAMESPACE) {
					subscriptions.get(socket)?.delete(NAMESPACE);
					subscribers.delete(socket);
				}

				return;
			}

			if (type === 'ping') {
				socket.send(JSON.stringify({ namespace: NAMESPACE, type: 'pong', payload: null }));
			}
		},
		close(socket) {
			subscribers.delete(socket);
			subscriptions.delete(socket);
			console.log('[mock] WebSocket client disconnected');
		}
	}
});

function namespaceOf(message: Record<string, unknown>): string | undefined {
	const payload = message.payload;

	if (typeof payload === 'string') return payload;

	if (payload && typeof payload === 'object' && 'namespace' in payload) {
		const namespace = (payload as Record<string, unknown>).namespace;
		if (typeof namespace === 'string') return namespace;
	}

	if (typeof message.namespace === 'string') return message.namespace;

	return undefined;
}

function isLogType(value: unknown): value is LogType {
	return value === 'debug' || value === 'info' || value === 'warn' || value === 'error';
}

function numberOr(value: unknown, fallback: number): number {
	return typeof value === 'number' && Number.isFinite(value) ? value : fallback;
}

setInterval(() => {
	if (positionOverride) return;
	broadcastPosition(simulatedPosition());
}, POSITION_INTERVAL_MS);

scheduleLog();

console.log(`[mock] Dashboard HTTP:      http://localhost:${httpServer.port}${ROUTE_PREFIX}/`);
console.log(`[mock] Dashboard WebSocket: ws://localhost:${wsServer.port}`);
console.log(`[mock] Static directory:    ${STATIC_DIR}`);

if (!existsSync(STATIC_DIR)) {
	console.log('[mock] No dashboard build found; run "bun run build" to serve it from this server.');
}

if (process.env.MOCK_UNHEALTHY === '1') {
	console.log('[mock] Health endpoint starts unhealthy (MOCK_UNHEALTHY=1).');
}
