<script lang="ts">
	import {
		clamp,
		round,
		DEFAULT_TOLERANCE,
		START_POSITIONS,
		type Alliance,
		type Waypoint
	} from '$lib/autonomous';

	let {
		path = $bindable(),
		selected = $bindable(),
		alliance
	}: { path: Waypoint[]; selected: number | null; alliance: Alliance } = $props();

	let field: HTMLDivElement;
	let dragging: number | null = $state(null);

	const start = $derived(START_POSITIONS[alliance]);
	const points = $derived([start, ...path].map((point) => `${point.x},${point.y}`).join(' '));

	function toField(event: PointerEvent) {
		const rect = field.getBoundingClientRect();

		return {
			x: round(clamp(((event.clientX - rect.left) / rect.width) * 100), 1),
			y: round(clamp(((event.clientY - rect.top) / rect.height) * 100), 1)
		};
	}

	function addWaypoint(event: PointerEvent) {
		if (event.button !== 0 || event.target !== field) return;

		path.push({ ...toField(event), tolerance: DEFAULT_TOLERANCE });
		selected = path.length - 1;
	}

	function startDrag(event: PointerEvent, index: number) {
		if (event.button !== 0) return;

		event.stopPropagation();
		(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);

		dragging = index;
		selected = index;
	}

	function drag(event: PointerEvent) {
		if (dragging === null) return;
		Object.assign(path[dragging], toField(event));
	}

	function removeWaypoint(event: MouseEvent, index: number) {
		event.preventDefault();
		path.splice(index, 1);
		selected = null;
	}
</script>

<div
	bind:this={field}
	role="application"
	aria-label="Autonomous path editor. Click to add a waypoint."
	class="absolute inset-0 cursor-crosshair touch-none"
	onpointerdown={addWaypoint}
	onpointermove={drag}
	onpointerup={() => (dragging = null)}
>
	<svg
		class="pointer-events-none absolute inset-0 h-full w-full"
		viewBox="0 0 100 100"
		preserveAspectRatio="none"
	>
		{#each path as point, i}
			<rect
				x={point.x - point.tolerance}
				y={point.y - point.tolerance}
				width={point.tolerance * 2}
				height={point.tolerance * 2}
				class="fill-accent-500/15 stroke-accent-500/50 {selected === i ? 'fill-accent-400/30' : ''}"
				stroke-width="1"
				vector-effect="non-scaling-stroke"
			/>
		{/each}

		<polyline
			{points}
			fill="none"
			class="stroke-primary-400"
			stroke-width="2"
			stroke-dasharray="6 4"
			vector-effect="non-scaling-stroke"
		/>
	</svg>

	<div
		style="left: {start.x}%; top: {start.y}%;"
		class="pointer-events-none absolute -translate-x-1/2 -translate-y-1/2 rounded-sm border px-1 text-xs
			{alliance === 'red' ? 'border-red-500 text-red-400' : 'border-blue-500 text-blue-400'}"
	>
		start
	</div>

	{#each path as point, i}
		<button
			type="button"
			style="left: {point.x}%; top: {point.y}%;"
			class="absolute flex h-6 w-6 -translate-x-1/2 -translate-y-1/2 cursor-grab items-center justify-center rounded-full text-xs text-black active:cursor-grabbing
				{selected === i ? 'bg-accent-300 ring-2 ring-accent-100' : 'bg-primary-500'}"
			title="({point.x}, {point.y}) ±{point.tolerance} — drag to move, right-click to delete"
			onpointerdown={(event) => startDrag(event, i)}
			oncontextmenu={(event) => removeWaypoint(event, i)}
		>
			{i + 1}
		</button>
	{/each}
</div>
