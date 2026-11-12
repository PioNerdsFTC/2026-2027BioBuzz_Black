import { INCH } from './autonomous';

/**
 * BIOBUZZ field elements in Coordination units (0-100), drawn with the audience at the bottom
 * (y = 100) and the red ALLIANCE AREA on the left (x = 0), matching Coordination's start positions.
 * TILE columns A-F run red to blue, rows 1-6 run from the far wall to the audience.
 *
 * Sizes come from the BIOBUZZ Competition Manual (sections 9.3, 9.6, 9.7). The manual only gives
 * exact placements in figures, so positions follow its text plus tile locations from the community
 * simulator (github.com/Soul080/ftc-sim). Check them against Figures 9-2 and 10-2 and adjust here.
 */

export type Rect = { x: number; y: number; width: number; height: number };

const TILE = 100 / 6;

function centered(cx: number, cy: number, width: number, height: number): Rect {
	return { x: cx - width / 2, y: cy - height / 2, width, height };
}

/** Frame at the field center: 49.46 in. wide, 38.95 in. deep. */
export const HIVE_FRAME = centered(50, 50, 49.46 * INCH, 38.95 * INCH);

/** Each HIVE pivots on the frame; pivots are roughly 25.5 in. apart. */
const PIVOT_OFFSET = (25.5 / 2) * INCH;

/** CELL centers are ~18.8 in. apart; openings are 20 in. wide, ~12 in. deep. */
const CELL_OFFSET = (18.8 / 2) * INCH;
const CELL_WIDTH = 20 * INCH;
const CELL_DEPTH = 12 * INCH;

export type Cell = Rect & {
	alliance: 'red' | 'blue';
	side: 'far' | 'audience';
	aprilTags: number[];
};

export const CELLS: Cell[] = [
	{
		...centered(50 - PIVOT_OFFSET, 50 - CELL_OFFSET, CELL_WIDTH, CELL_DEPTH),
		alliance: 'red',
		side: 'far',
		aprilTags: [30, 31, 32, 33]
	},
	{
		...centered(50 - PIVOT_OFFSET, 50 + CELL_OFFSET, CELL_WIDTH, CELL_DEPTH),
		alliance: 'red',
		side: 'audience',
		aprilTags: [34, 35, 36, 37]
	},
	{
		...centered(50 + PIVOT_OFFSET, 50 + CELL_OFFSET, CELL_WIDTH, CELL_DEPTH),
		alliance: 'blue',
		side: 'audience',
		aprilTags: [38, 39, 40, 41]
	},
	{
		...centered(50 + PIVOT_OFFSET, 50 - CELL_OFFSET, CELL_WIDTH, CELL_DEPTH),
		alliance: 'blue',
		side: 'far',
		aprilTags: [42, 43, 44, 45]
	}
];

/** One FLOWER centered on each perimeter wall. */
const FLOWER_INSET = 6.5 * INCH;

export const FLOWERS: { x: number; y: number; wall: string }[] = [
	{ x: 50, y: FLOWER_INSET, wall: 'far' },
	{ x: 50, y: 100 - FLOWER_INSET, wall: 'audience' },
	{ x: FLOWER_INSET, y: 50, wall: 'red' },
	{ x: 100 - FLOWER_INSET, y: 50, wall: 'blue' }
];

/** 4 in. FLOWER top opening, drawn larger so it is visible. */
export const FLOWER_RADIUS = 2.6 * INCH;

/** 23 in. x 2 in. tape strips in opposite corners: red at A1 (far wall), blue at F6 (audience wall). */
export const GARDENS: (Rect & { alliance: 'red' | 'blue' })[] = [
	{ x: 0, y: 0, width: 23 * INCH, height: 2 * INCH, alliance: 'red' },
	{ x: 100 - 23 * INCH, y: 100 - 2 * INCH, width: 23 * INCH, height: 2 * INCH, alliance: 'blue' }
];

/** 23 in. wide x 11 in. deep against the alliance wall: red at A5, blue at F2. */
export const LOADING_ZONES: (Rect & { alliance: 'red' | 'blue' })[] = [
	{ ...centered((11 / 2) * INCH, 4.5 * TILE, 11 * INCH, 23 * INCH), alliance: 'red' },
	{ ...centered(100 - (11 / 2) * INCH, 1.5 * TILE, 11 * INCH, 23 * INCH), alliance: 'blue' }
];

export const TILE_SIZE = TILE;
