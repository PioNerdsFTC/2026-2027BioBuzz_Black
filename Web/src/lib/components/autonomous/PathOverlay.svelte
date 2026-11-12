<script lang="ts">
	import {
		clamp,
		clampStart,
		describeAction,
		round,
		DEFAULT_TOLERANCE,
		ROBOT_SIZE,
		type Alliance,
		type Program,
		type Selection
	} from '$lib/autonomous';

	let {
		program = $bindable(),
		selected = $bindable(),
		alliance
	}: { program: Program; selected: Selection; alliance: Alliance } = $props();

	let field: HTMLDivElement;
	let dragging: Selection = $state(null);

	const points = $derived(
		[program.start, ...program.waypoints].map((point) => `${point.x},${point.y}`).join(' ')
	);

	function toField(event: PointerEvent) {
		const rect = field.getBoundingClientRect();

		return {
			x: round(clamp(((event.clientX - rect.left) / rect.width) * 100), 1),
			y: round(clamp(((event.clientY - rect.top) / rect.height) * 100), 1)
		};
	}

	function addWaypoint(event: PointerEvent) {
		if (event.button !== 0 || event.target !== field) return;

		program.waypoints.push({ ...toField(event), tolerance: DEFAULT_TOLERANCE, actions: [] });
		selected = program.waypoints.length - 1;
	}

	function startDrag(event: PointerEvent, target: Selection) {
		if (event.button !== 0) return;

		event.stopPropagation();
		(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);

		dragging = target;
		selected = target;
	}

	function drag(event: PointerEvent) {
		if (dragging === null) return;

		const position = toField(event);

		if (dragging === 'start') {
			program.start = { x: clampStart(position.x), y: clampStart(position.y) };
		} else {
			Object.assign(program.waypoints[dragging], position);
		}
	}

	function removeWaypoint(event: MouseEvent, index: number) {
		event.preventDefault();
		program.waypoints.splice(index, 1);
		selected = null;
	}

	function actionSummary(actions: Program['startActions']) {
		return actions.length ? '\n' + actions.map(describeAction).join('\n') : '';
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
		{#each program.waypoints as point, i}
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
			class="stroke-primary-300"
			stroke-width="2"
			stroke-dasharray="6 4"
			vector-effect="non-scaling-stroke"
		/>
	</svg>

	<!-- Starting position, drawn as the 18 in. robot footprint -->
	<button
		type="button"
		style="left: {program.start.x}%; top: {program.start
			.y}%; width: {ROBOT_SIZE}%; height: {ROBOT_SIZE}%;"
		class="absolute flex -translate-x-1/2 -translate-y-1/2 cursor-grab flex-col items-center justify-center rounded-sm border-2 text-xs active:cursor-grabbing
			{alliance === 'red'
			? 'border-red-500 bg-red-500/25 text-red-200'
			: 'border-blue-500 bg-blue-500/25 text-blue-200'}
			{selected === 'start' ? 'ring-2 ring-accent-100' : ''}"
		title="Start ({program.start.x}, {program.start.y}) — drag to move{actionSummary(
			program.startActions
		)}"
		onpointerdown={(event) => startDrag(event, 'start')}
	>
		start
		{#if program.startActions.length}
			<span class="text-[0.6rem] opacity-80">{program.startActions.length} action(s)</span>
		{/if}
	</button>

	{#each program.waypoints as point, i}
		<button
			type="button"
			style="left: {point.x}%; top: {point.y}%;"
			class="absolute flex h-6 w-6 -translate-x-1/2 -translate-y-1/2 cursor-grab items-center justify-center rounded-full text-xs text-black active:cursor-grabbing
				{selected === i ? 'bg-accent-300 ring-2 ring-accent-100' : 'bg-primary-500'}"
			title="({point.x}, {point.y}) ±{point.tolerance} — drag to move, right-click to delete{actionSummary(
				point.actions
			)}"
			onpointerdown={(event) => startDrag(event, i)}
			oncontextmenu={(event) => removeWaypoint(event, i)}
		>
			{i + 1}
			{#if point.actions.length}
				<span
					class="absolute -top-1 -right-1 h-2.5 w-2.5 rounded-full border border-black bg-yellow-300"
				></span>
			{/if}
		</button>
	{/each}
</div>
