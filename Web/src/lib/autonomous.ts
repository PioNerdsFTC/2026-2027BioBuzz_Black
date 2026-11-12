import type { IconName } from '$lib/components/Icon.svelte';

/**
 * Actions run in order once the robot reaches a waypoint (or before it moves, for the start).
 * Mirrors `AutonomousProgram.java`.
 */
export type Action =
	| { type: 'shoot'; piece: Piece; count: number; angle: number }
	| { type: 'intake'; duration: number }
	| { type: 'wait'; duration: number };

export type Piece = 'pollen' | 'nectar';

export type ActionType = Action['type'];

/**
 * A single autonomous waypoint, in Coordination units (0-100 on both axes).
 * The robot considers a waypoint reached once it is within `tolerance` on each axis.
 */
export type Waypoint = {
	x: number;
	y: number;
	tolerance: number;
	/** Target heading in radians, or undefined to keep the current heading. */
	rotation?: number;
	/** Run the intake while driving to this waypoint. */
	intake: boolean;
	actions: Action[];
};

export type Program = {
	start: { x: number; y: number; rotation?: number };
	startActions: Action[];
	waypoints: Waypoint[];
};

/** Either the start position or a waypoint index. */
export type Selection = 'start' | number | null;

export type Alliance = 'red' | 'blue';

/** 1 inch in Coordination units (the field is 144 in. across). */
export const INCH = 100 / 144;

/** Robots must fit in an 18 in. cube. */
export const ROBOT_SIZE = 18 * INCH;

export const MAX_DURATION_MS = 30000;
export const MAX_SHOTS = 8;

/**
 * Starting positions touching the alliance wall, outside the LOADING ZONE.
 * These replace Coordination's defaults once uploaded.
 */
export const START_POSITIONS: Record<Alliance, { x: number; y: number }> = {
	red: { x: round(ROBOT_SIZE / 2), y: 91.5 },
	blue: { x: round(100 - ROBOT_SIZE / 2), y: 8.5 }
};

export const DEFAULT_TOLERANCE = 2;

export const ACTION_LABELS: Record<ActionType, string> = {
	shoot: 'Shoot',
	intake: 'Intake',
	wait: 'Wait'
};

export const ACTION_ICONS: Record<ActionType, IconName> = {
	shoot: 'launcher',
	intake: 'intake',
	wait: 'wait'
};

const STORAGE_KEY = 'autonomous-program';
const LEGACY_STORAGE_KEY = 'autonomous-path';

export function clamp(value: number, min = 0, max = 100): number {
	return Math.min(max, Math.max(min, value));
}

/** Keep the robot footprint inside the field. */
export function clampStart(value: number): number {
	return round(clamp(value, ROBOT_SIZE / 2, 100 - ROBOT_SIZE / 2), 1);
}

export function round(value: number, digits = 2): number {
	const factor = 10 ** digits;
	return Math.round(value * factor) / factor;
}

/** Wrap an angle to [-pi, pi]. */
export function normalizeAngle(angle: number): number {
	const twoPi = 2 * Math.PI;
	return ((((angle + Math.PI) % twoPi) + twoPi) % twoPi) - Math.PI;
}

export function defaultAction(type: ActionType, previous?: Action): Action {
	switch (type) {
		case 'shoot':
			// Repeat the last shot's settings, since consecutive shots usually match
			return previous?.type === 'shoot'
				? { ...previous }
				: { type, piece: 'pollen', count: 1, angle: 45 };
		case 'intake':
			return { type, duration: 1000 };
		case 'wait':
			return { type, duration: 500 };
	}
}

export function emptyProgram(alliance: Alliance = 'red'): Program {
	return { start: { ...START_POSITIONS[alliance] }, startActions: [], waypoints: [] };
}

export function describeAction(action: Action): string {
	switch (action.type) {
		case 'shoot':
			return `Shoot ${action.count} ${action.piece} @ ${action.angle}°`;
		case 'intake':
			return `Intake ${action.duration / 1000}s`;
		case 'wait':
			return `Wait ${action.duration / 1000}s`;
	}
}

function isNumber(value: unknown): value is number {
	return typeof value === 'number' && Number.isFinite(value);
}

function isAction(value: unknown): value is Action {
	if (!value || typeof value !== 'object') return false;
	const action = value as Record<string, unknown>;

	switch (action.type) {
		case 'shoot':
			return (
				(action.piece === 'pollen' || action.piece === 'nectar') &&
				isNumber(action.count) &&
				isNumber(action.angle)
			);
		case 'intake':
		case 'wait':
			return isNumber(action.duration);
		default:
			return false;
	}
}

function parseActions(value: unknown): Action[] {
	return Array.isArray(value) ? value.filter(isAction) : [];
}

function parseWaypoint(value: unknown): Waypoint | null {
	if (!value || typeof value !== 'object') return null;
	const point = value as Record<string, unknown>;

	if (!isNumber(point.x) || !isNumber(point.y) || !isNumber(point.tolerance)) return null;

	return {
		x: point.x,
		y: point.y,
		tolerance: point.tolerance,
		rotation: isNumber(point.rotation) ? normalizeAngle(point.rotation) : undefined,
		intake: point.intake === true,
		actions: parseActions(point.actions)
	};
}

/** Accepts programs, and bare waypoint arrays from older saves. Returns null if unusable. */
export function parseProgram(value: unknown, alliance: Alliance = 'red'): Program | null {
	if (Array.isArray(value)) {
		return {
			...emptyProgram(alliance),
			waypoints: value.map(parseWaypoint).filter((point) => point !== null)
		};
	}

	if (!value || typeof value !== 'object') return null;
	const json = value as Record<string, unknown>;
	if (!Array.isArray(json.waypoints)) return null;

	const start = json.start as Record<string, unknown> | null | undefined;

	return {
		start:
			start && isNumber(start.x) && isNumber(start.y)
				? {
						x: clampStart(start.x),
						y: clampStart(start.y),
						rotation: isNumber(start.rotation) ? normalizeAngle(start.rotation) : undefined
					}
				: { ...START_POSITIONS[alliance] },
		startActions: parseActions(json.startActions),
		waypoints: json.waypoints.map(parseWaypoint).filter((point) => point !== null)
	};
}

export async function fetchProgram(alliance: Alliance): Promise<Program> {
	const response = await fetch('/dashboard/api/autonomous/get-path');
	if (!response.ok) throw new Error(`Robot responded with ${response.status}`);

	const program = parseProgram(await response.json(), alliance);
	if (!program) throw new Error('Malformed program from robot');

	return program;
}

export async function uploadProgram(program: Program): Promise<void> {
	const response = await fetch('/dashboard/api/autonomous/set-path', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(program)
	});

	if (!response.ok)
		throw new Error((await response.text()) || `Robot responded with ${response.status}`);
}

export function loadLocalProgram(): Program {
	for (const key of [STORAGE_KEY, LEGACY_STORAGE_KEY]) {
		try {
			const program = parseProgram(JSON.parse(localStorage.getItem(key) ?? 'null'));
			if (program) return program;
		} catch {
			// fall through to the next key
		}
	}

	return emptyProgram();
}

export function saveLocalProgram(program: Program) {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(program));
}
