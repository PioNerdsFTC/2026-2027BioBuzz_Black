/**
 * Minimal ambient declarations for the Bun APIs used by the mock server.
 *
 * Keeping these local means the app does not need `@types/bun` (which pulls in
 * `@types/node` and changes timer types across the SvelteKit project).
 */

declare const Bun: {
	serve(options: {
		port: number;
		fetch(
			request: Request,
			server: { upgrade(request: Request): boolean }
		): Response | Promise<Response> | undefined;
		websocket?: {
			open?(socket: ServerWebSocket): void;
			message?(socket: ServerWebSocket, message: string | Uint8Array): void;
			close?(socket: ServerWebSocket, code?: number, reason?: string): void;
		};
	}): { port: number };
	file(path: string): Blob & { exists(): Promise<boolean> };
};

interface ServerWebSocket<T = unknown> {
	readonly data: T;
	readonly remoteAddress: string;
	send(data: string): number;
	close(code?: number, reason?: string): void;
}

declare const process: {
	env: Record<string, string | undefined>;
};

declare module 'node:fs' {
	export function existsSync(path: string): boolean;
}

declare module 'node:path' {
	export function extname(path: string): string;
	export function resolve(...paths: string[]): string;
	export const sep: string;
}

declare module 'node:url' {
	export function fileURLToPath(url: string | URL): string;
}
