/**
 * A single autonomous waypoint, in Coordination units (0-100 on both axes).
 * The robot considers a waypoint reached once it is within `tolerance` on each axis.
 */
export type Waypoint = {
	x: number;
	y: number;
	tolerance: number;
};

export type Alliance = 'red' | 'blue';

/** Starting positions, mirroring `Coordination.java`. */
export const START_POSITIONS: Record<Alliance, { x: number; y: number }> = {
	red: { x: 0, y: 85 },
	blue: { x: 100, y: 15 }
};

export const DEFAULT_TOLERANCE = 2;

const STORAGE_KEY = 'autonomous-path';

export function clamp(value: number, min = 0, max = 100): number {
	return Math.min(max, Math.max(min, value));
}

export function round(value: number, digits = 2): number {
	const factor = 10 ** digits;
	return Math.round(value * factor) / factor;
}

export function isWaypoint(value: unknown): value is Waypoint {
	if (!value || typeof value !== 'object') return false;
	const point = value as Record<string, unknown>;

	return (
		typeof point.x === 'number' &&
		typeof point.y === 'number' &&
		typeof point.tolerance === 'number'
	);
}

export async function fetchPath(): Promise<Waypoint[]> {
	const response = await fetch('/dashboard/api/autonomous/get-path');
	if (!response.ok) throw new Error(`Robot responded with ${response.status}`);

	const json = await response.json();
	if (!Array.isArray(json) || !json.every(isWaypoint)) throw new Error('Malformed path from robot');

	return json;
}

export async function uploadPath(path: Waypoint[]): Promise<void> {
	const response = await fetch('/dashboard/api/autonomous/set-path', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(path)
	});

	if (!response.ok)
		throw new Error((await response.text()) || `Robot responded with ${response.status}`);
}

/** Java source that queues the path directly through `Autonomous.requestMovement`. */
export function toJava(path: Waypoint[]): string {
	return path
		.map(
			(point) =>
				`Autonomous.requestMovement(new double[] { ${point.x}, ${point.y}, ${point.tolerance} });`
		)
		.join('\n');
}

export function loadLocalPath(): Waypoint[] {
	try {
		const json = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]');
		return Array.isArray(json) ? json.filter(isWaypoint) : [];
	} catch {
		return [];
	}
}

export function saveLocalPath(path: Waypoint[]) {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(path));
}
