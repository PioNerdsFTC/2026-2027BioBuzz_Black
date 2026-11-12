<script lang="ts">
	import {
		clamp,
		clampStart,
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

	const nodes = $derived([program.start, ...program.waypoints]);

	/** Each leg, split into its midpoint (for the direction arrow) and its drawable path. */
	const legs = $derived(
		nodes.slice(1).map((to, i) => {
			const from = nodes[i];
			return {
				x: (from.x + to.x) / 2,
				y: (from.y + to.y) / 2,
				angle: (Math.atan2(to.y - from.y, to.x - from.x) * 180) / Math.PI,
				points: `${from.x},${from.y} ${to.x},${to.y}`,
				long: Math.hypot(to.x - from.x, to.y - from.y) > 6,
				intake: program.waypoints[i].intake
			};
		})
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

		program.waypoints.push({
			...toField(event),
			tolerance: DEFAULT_TOLERANCE,
			intake: false,
			actions: []
		});
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
</script>

<div
	bind:this={field}
	role="application"
	aria-label="Autonomous path"
	class="absolute inset-0 cursor-crosshair touch-none"
	onpointerdown={addWaypoint}
	onpointermove={drag}
	onpointerup={() => (dragging = null)}
>
	<svg class="pointer-events-none absolute inset-0 h-full w-full" viewBox="0 0 100 100">
		{#each program.waypoints as point, i}
			<rect
				x={point.x - point.tolerance}
				y={point.y - point.tolerance}
				width={point.tolerance * 2}
				height={point.tolerance * 2}
				class={selected === i
					? 'fill-accent-300/15 stroke-accent-300/60'
					: 'fill-none stroke-accent-300/25'}
				stroke-width="0.25"
			/>
		{/each}

		{#each legs as leg}
			<polyline
				points={leg.points}
				fill="none"
				class={leg.intake ? 'stroke-yellow-300' : 'stroke-accent-200/80'}
				stroke-width={leg.intake ? '0.7' : '0.45'}
				stroke-linejoin="round"
			/>

			{#if leg.long}
				<path
					d="M -1.1 -1 L 0.9 0 L -1.1 1"
					transform="translate({leg.x} {leg.y}) rotate({leg.angle})"
					fill="none"
					class={leg.intake ? 'stroke-yellow-300' : 'stroke-accent-200'}
					stroke-width="0.45"
					stroke-linecap="round"
					stroke-linejoin="round"
				/>
			{/if}
		{/each}
	</svg>

	<!-- Starting position, the 18 in. robot footprint -->
	<button
		type="button"
		aria-label="Start"
		title="Start"
		style="left: {program.start.x}%; top: {program.start
			.y}%; width: {ROBOT_SIZE}%; height: {ROBOT_SIZE}%;"
		class="absolute -translate-x-1/2 -translate-y-1/2 cursor-grab rounded-sm border-2 active:cursor-grabbing
			{alliance === 'red' ? 'border-red-400 bg-red-500/30' : 'border-blue-400 bg-blue-500/30'}
			{selected === 'start' ? 'outline-2 outline-offset-2 outline-accent-200' : ''}"
		onpointerdown={(event) => startDrag(event, 'start')}
	>
		{#if program.startActions.length}
			<span class="absolute top-0.5 right-0.5 h-2 w-2 rounded-full bg-accent-200"></span>
		{/if}
	</button>

	{#each program.waypoints as point, i}
		<button
			type="button"
			aria-label="Waypoint {i + 1}"
			title="Waypoint {i + 1}"
			style="left: {point.x}%; top: {point.y}%;"
			class="absolute flex h-6 w-6 -translate-x-1/2 -translate-y-1/2 cursor-grab items-center justify-center rounded-full border text-xs font-semibold active:cursor-grabbing
				{selected === i
				? 'border-accent-100 bg-accent-200 text-background-950'
				: 'border-accent-300 bg-background-950 text-accent-200'}"
			onpointerdown={(event) => startDrag(event, i)}
			oncontextmenu={(event) => removeWaypoint(event, i)}
		>
			{i + 1}
			{#if point.actions.length}
				<span
					class="absolute -top-0.5 -right-0.5 h-2 w-2 rounded-full border border-background-950 bg-accent-200"
				></span>
			{/if}
		</button>
	{/each}
</div>
