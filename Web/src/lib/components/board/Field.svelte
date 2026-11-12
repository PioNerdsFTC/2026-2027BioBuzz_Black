<script lang="ts">
	import {
		CELLS,
		COLUMNS,
		FLOWER_RADIUS,
		FLOWERS,
		GARDENS,
		HIVE_FRAME,
		LOADING_ZONES,
		TILE_SIZE
	} from '$lib/field';

	const NECTAR_RADIUS = 1.25;
	const POLLEN_RADIUS = 0.95;

	const allianceFill = { red: 'fill-red-500', blue: 'fill-blue-500' };
	const allianceStroke = { red: 'stroke-red-500', blue: 'stroke-blue-500' };
</script>

<svg
	class="pointer-events-none absolute inset-0 h-full w-full select-none"
	viewBox="0 0 100 100"
	aria-hidden="true"
>
	<!-- Alliance halves -->
	<rect x="0" y="0" width="50" height="100" class="fill-red-500/5" />
	<rect x="50" y="0" width="50" height="100" class="fill-blue-500/5" />

	<!-- TILE grid -->
	{#each Array.from({ length: 5 }, (_, i) => (i + 1) * TILE_SIZE) as offset}
		<line
			x1={offset}
			y1="0"
			x2={offset}
			y2="100"
			class="stroke-primary-500/20"
			stroke-width="0.25"
		/>
		<line
			x1="0"
			y1={offset}
			x2="100"
			y2={offset}
			class="stroke-primary-500/20"
			stroke-width="0.25"
		/>
	{/each}

	{#each COLUMNS as column, i}
		{#each Array.from({ length: 6 }, (_, j) => j) as row}
			<text x={i * TILE_SIZE + 1} y={row * TILE_SIZE + 3} class="fill-text-800" font-size="2.2"
				>{column}{row + 1}</text
			>
		{/each}
	{/each}

	<text x="50" y="98.8" text-anchor="middle" class="fill-text-700" font-size="2">▼ audience ▼</text>

	<!-- LOADING ZONES -->
	{#each LOADING_ZONES as zone}
		<rect
			{...{ x: zone.x, y: zone.y, width: zone.width, height: zone.height }}
			class="fill-none {allianceStroke[zone.alliance]}"
			stroke-width="0.4"
			stroke-dasharray="1.5 1"
		/>
		<text
			x={zone.x + zone.width / 2}
			y={zone.y + zone.height / 2}
			text-anchor="middle"
			dominant-baseline="middle"
			transform="rotate(-90 {zone.x + zone.width / 2} {zone.y + zone.height / 2})"
			class={allianceFill[zone.alliance]}
			font-size="2">loading</text
		>
	{/each}

	<!-- GARDENS, each staged with 4 POLLEN -->
	{#each GARDENS as garden}
		<rect
			{...{ x: garden.x, y: garden.y, width: garden.width, height: garden.height }}
			class="{allianceFill[garden.alliance]} opacity-60"
		/>
		{#each [0, 1, 2, 3] as i}
			<circle
				cx={garden.alliance === 'red'
					? garden.x + POLLEN_RADIUS + i * 2 * POLLEN_RADIUS
					: garden.x + garden.width - POLLEN_RADIUS - i * 2 * POLLEN_RADIUS}
				cy={garden.y + garden.height / 2}
				r={POLLEN_RADIUS}
				class="fill-yellow-300"
			/>
		{/each}
		<text
			x={garden.x + garden.width / 2}
			y={garden.alliance === 'red' ? garden.y + garden.height + 2.4 : garden.y - 1}
			text-anchor="middle"
			class={allianceFill[garden.alliance]}
			font-size="2">garden</text
		>
	{/each}

	<!-- FLOWERS -->
	{#each FLOWERS as flower}
		<circle
			cx={flower.x}
			cy={flower.y}
			r={FLOWER_RADIUS}
			class="fill-pink-400/30 stroke-pink-400"
			stroke-width="0.4"
		/>
		<text
			x={flower.x}
			y={flower.y}
			text-anchor="middle"
			dominant-baseline="central"
			class="fill-pink-300"
			font-size="2.2">✿</text
		>
	{/each}

	<!-- HIVE structure -->
	<rect
		{...{ x: HIVE_FRAME.x, y: HIVE_FRAME.y, width: HIVE_FRAME.width, height: HIVE_FRAME.height }}
		class="fill-amber-900/20 stroke-amber-500/70"
		stroke-width="0.4"
		rx="0.8"
	/>
	<text
		x="50"
		y={HIVE_FRAME.y + HIVE_FRAME.height / 2}
		text-anchor="middle"
		dominant-baseline="central"
		class="fill-amber-400/80"
		font-size="2.4">HIVE</text
	>

	{#each CELLS as cell}
		<rect
			{...{ x: cell.x, y: cell.y, width: cell.width, height: cell.height }}
			class="{cell.alliance === 'red' ? 'stroke-red-500' : 'stroke-blue-500'}
				{cell.up ? (cell.alliance === 'red' ? 'fill-red-500/35' : 'fill-blue-500/35') : 'fill-none'}"
			stroke-width="0.4"
			stroke-dasharray={cell.up ? undefined : '1 0.8'}
			rx="0.5"
		>
			<title
				>{cell.alliance}
				{cell.side} cell{cell.up ? ' (up at start)' : ''} · AprilTags {cell.aprilTags.join(
					', '
				)}</title
			>
		</rect>

		<!-- The upward-facing CELL starts with 3 NECTAR -->
		{#if cell.up}
			{#each [-1, 0, 1] as i}
				<circle
					cx={cell.x + cell.width / 2 + i * 2.8}
					cy={cell.y + cell.height / 2}
					r={NECTAR_RADIUS}
					class="{allianceFill[cell.alliance]} stroke-white/70"
					stroke-width="0.25"
				/>
			{/each}
		{/if}
	{/each}

	<!-- Perimeter, alliance walls colored -->
	<line x1="0.3" y1="0" x2="0.3" y2="100" class="stroke-red-500" stroke-width="0.6" />
	<line x1="99.7" y1="0" x2="99.7" y2="100" class="stroke-blue-500" stroke-width="0.6" />
</svg>
