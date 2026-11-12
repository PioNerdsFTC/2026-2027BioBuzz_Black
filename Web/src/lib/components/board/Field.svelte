<script lang="ts">
	import {
		CELLS,
		FLOWER_RADIUS,
		FLOWERS,
		GARDENS,
		HIVE_FRAME,
		LOADING_ZONES,
		TILE_SIZE,
		type Rect
	} from '$lib/field';

	const COLUMNS = ['A', 'B', 'C', 'D', 'E', 'F'];

	const allianceFill = { red: 'fill-red-500', blue: 'fill-blue-500' };
	const allianceStroke = { red: 'stroke-red-500', blue: 'stroke-blue-500' };

	const box = (rect: Rect) => ({ x: rect.x, y: rect.y, width: rect.width, height: rect.height });
	const tiles = Array.from({ length: 6 }, (_, i) => i);
</script>

<!-- Overflow is visible so the axis labels can sit in the margin Board leaves around the field -->
<svg
	class="pointer-events-none absolute inset-0 h-full w-full overflow-visible select-none"
	viewBox="0 0 100 100"
	aria-hidden="true"
>
	<!-- Alliance halves -->
	<rect x="0" y="0" width="50" height="100" class="fill-red-500/5" />
	<rect x="50" y="0" width="50" height="100" class="fill-blue-500/5" />

	<!-- TILE grid, labelled outside the field -->
	{#each tiles.slice(1) as i}
		<line
			x1={i * TILE_SIZE}
			y1="0"
			x2={i * TILE_SIZE}
			y2="100"
			class="stroke-primary-500/20"
			stroke-width="0.25"
		/>
		<line
			x1="0"
			y1={i * TILE_SIZE}
			x2="100"
			y2={i * TILE_SIZE}
			class="stroke-primary-500/20"
			stroke-width="0.25"
		/>
	{/each}

	{#each tiles as i}
		<text
			x={(i + 0.5) * TILE_SIZE}
			y="-1.2"
			text-anchor="middle"
			class="fill-text-700"
			font-size="2.2">{COLUMNS[i]}</text
		>
		<text
			x="-1.4"
			y={(i + 0.5) * TILE_SIZE}
			text-anchor="end"
			dominant-baseline="central"
			class="fill-text-700"
			font-size="2.2">{i + 1}</text
		>
	{/each}

	<text x="50" y="103" text-anchor="middle" class="fill-text-700" font-size="2">▼ audience ▼</text>

	<!-- LOADING ZONES -->
	{#each LOADING_ZONES as zone}
		<!-- Open on the wall side, so the dashes don't run over the perimeter -->
		<path
			d={zone.alliance === 'red'
				? `M 0 ${zone.y} H ${zone.x + zone.width} V ${zone.y + zone.height} H 0`
				: `M 100 ${zone.y} H ${zone.x} V ${zone.y + zone.height} H 100`}
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

	<!-- GARDENS -->
	{#each GARDENS as garden}
		<rect {...box(garden)} class="{allianceFill[garden.alliance]} opacity-60" />
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
		{...box(HIVE_FRAME)}
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
			{...box(cell)}
			class="{allianceStroke[cell.alliance]} {cell.alliance === 'red'
				? 'fill-red-500/15'
				: 'fill-blue-500/15'}"
			stroke-width="0.4"
			rx="0.5"
		>
			<title>{cell.alliance} {cell.side} cell · AprilTags {cell.aprilTags.join(', ')}</title>
		</rect>
	{/each}

	<!-- Perimeter, alliance walls colored -->
	<line x1="0.3" y1="0" x2="0.3" y2="100" class="stroke-red-500" stroke-width="0.6" />
	<line x1="99.7" y1="0" x2="99.7" y2="100" class="stroke-blue-500" stroke-width="0.6" />
</svg>
