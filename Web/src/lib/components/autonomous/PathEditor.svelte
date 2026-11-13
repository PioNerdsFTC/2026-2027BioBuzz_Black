<script lang="ts">
	import ActionList from './ActionList.svelte';
	import Icon, { type IconName } from '$lib/components/Icon.svelte';
	import {
		clamp,
		clampStart,
		emptyProgram,
		fetchProgram,
		parseProgram,
		uploadProgram,
		normalizeAngle,
		defaultStartRotation,
		ACTION_ICONS,
		DEFAULT_TOLERANCE,
		START_POSITIONS,
		type Action,
		type Alliance,
		type Program,
		type Selection,
		type Waypoint
	} from '$lib/autonomous';

	let {
		program = $bindable(),
		selected = $bindable(),
		alliance = $bindable()
	}: { program: Program; selected: Selection; alliance: Alliance } = $props();

	let status: { type: 'info' | 'error'; msg: string } | null = $state(null);
	let statusTimeout: ReturnType<typeof setTimeout>;
	let busy = $state(false);
	let importInput: HTMLInputElement;

	const field =
		'h-7 w-full rounded-sm bg-background-900 px-1.5 text-right text-text-200 outline-none focus:ring-1 focus:ring-accent-400';
	const toolButton =
		'flex h-8 items-center gap-0.5 rounded-sm px-2 text-text-500 hover:bg-accent-500/15 hover:text-text-200 disabled:opacity-40';
	const rowButton =
		'grid h-6 w-6 place-items-center rounded-sm text-text-600 hover:bg-accent-500/15 hover:text-text-200 disabled:pointer-events-none disabled:opacity-0';

	const waypoints = $derived(program.waypoints);

	async function run(action: () => Promise<string>) {
		busy = true;
		clearTimeout(statusTimeout);

		try {
			status = { type: 'info', msg: await action() };
			statusTimeout = setTimeout(() => (status = null), 3000);
		} catch (error) {
			status = { type: 'error', msg: error instanceof Error ? error.message : String(error) };
		} finally {
			busy = false;
		}
	}

	const upload = () =>
		run(async () => {
			await uploadProgram(program);
			return 'Uploaded';
		});

	const download = () =>
		run(async () => {
			program = await fetchProgram(alliance);
			selected = null;
			return 'Loaded from robot';
		});

	function exportFile() {
		const url = URL.createObjectURL(
			new Blob([JSON.stringify(program, null, '\t')], { type: 'application/json' })
		);

		const link = document.createElement('a');
		link.href = url;
		link.download = `autonomous-${alliance}.json`;
		link.click();

		URL.revokeObjectURL(url);
	}

	const importFile = (file: File) =>
		run(async () => {
			const imported = parseProgram(JSON.parse(await file.text()), alliance);
			if (!imported) throw new Error(`${file.name} is not an autonomous program`);

			program = imported;
			selected = null;
			return `Imported ${file.name}`;
		});

	function setAlliance(value: Alliance) {
		if (value === alliance) return;

		alliance = value;
		program.start = { ...START_POSITIONS[value], rotation: defaultStartRotation(value) };
	}

	function move(index: number, offset: number) {
		const target = index + offset;
		if (target < 0 || target >= waypoints.length) return;

		[waypoints[index], waypoints[target]] = [waypoints[target], waypoints[index]];
		selected = target;
	}

	function remove(index: number) {
		waypoints.splice(index, 1);
		selected = null;
	}

	function addWaypoint() {
		const last = waypoints.at(-1);

		waypoints.push({
			x: last?.x ?? program.start.x,
			y: last?.y ?? program.start.y,
			tolerance: last?.tolerance ?? DEFAULT_TOLERANCE,
			rotation: last?.rotation,
			intake: last?.intake ?? false,
			actions: []
		});
		selected = waypoints.length - 1;
	}

	function setCoordinate(point: Waypoint, key: 'x' | 'y' | 'tolerance', value: number) {
		if (!Number.isFinite(value)) return;
		point[key] = key === 'tolerance' ? clamp(value, 0.1, 50) : clamp(value);
	}

	function setRotation(point: { rotation?: number }, value: number) {
		point.rotation = Number.isFinite(value) ? normalizeAngle(value) : undefined;
	}

	function setStart(key: 'x' | 'y', value: number) {
		if (Number.isFinite(value)) program.start[key] = clampStart(value);
	}
</script>

{#snippet tool(icon: IconName, arrow: IconName, label: string, onclick: () => void)}
	<button class={toolButton} disabled={busy} aria-label={label} title={label} {onclick}>
		<Icon name={icon} />
		<Icon name={arrow} size={12} />
	</button>
{/snippet}

{#snippet chips(actions: Action[])}
	<span class="flex min-w-0 items-center gap-0.5 overflow-hidden text-accent-300">
		{#each actions as action}
			<Icon name={ACTION_ICONS[action.type]} size={13} />
		{/each}
	</span>
{/snippet}

{#snippet number(value: number, label: string, onchange: (value: number) => void)}
	<input
		type="number"
		step="0.5"
		class={field}
		aria-label={label}
		{value}
		onclick={(event) => event.stopPropagation()}
		onchange={(event) => onchange(event.currentTarget.valueAsNumber)}
	/>
{/snippet}

{#snippet angle(value: number | undefined, label: string, onchange: (value: number) => void)}
	<input
		type="number"
		step="0.1"
		class={field}
		aria-label={label}
		title="Heading in radians, blank to keep the current heading"
		value={value ?? ''}
		onclick={(event) => event.stopPropagation()}
		onchange={(event) => onchange(event.currentTarget.valueAsNumber)}
	/>
{/snippet}

<div class="flex flex-col gap-3 p-4">
	<div class="flex items-center gap-1">
		<div class="flex gap-1" role="radiogroup" aria-label="Alliance">
			{#each ['red', 'blue'] as const as value}
				<button
					role="radio"
					aria-checked={alliance === value}
					aria-label="{value} alliance"
					title="{value} alliance"
					class="h-6 w-6 rounded-sm border-2 transition
						{value === 'red' ? 'bg-red-500' : 'bg-blue-500'}
						{alliance === value ? 'border-text-100' : 'border-transparent opacity-35 hover:opacity-70'}"
					onclick={() => setAlliance(value)}
				></button>
			{/each}
		</div>

		<p
			class="min-w-0 flex-1 truncate px-3 text-xs transition-opacity
				{status ? 'opacity-100' : 'opacity-0'}
				{status?.type === 'error' ? 'text-red-400' : 'text-accent-300'}"
			role="status"
			title={status?.msg}
		>
			{status?.msg}
		</p>

		{@render tool('bot', 'down', 'Load from robot', download)}
		{@render tool('bot', 'up', 'Upload to robot', upload)}
		<span class="mx-1 h-5 w-px bg-primary-500/40"></span>
		{@render tool('file', 'down', 'Export file', exportFile)}
		{@render tool('file', 'up', 'Import file', () => importInput.click())}
		<span class="mx-1 h-5 w-px bg-primary-500/40"></span>
		<button
			class="{toolButton} hover:text-red-400"
			aria-label="Clear"
			title="Clear"
			onclick={() => {
				program = emptyProgram(alliance);
				selected = null;
			}}
		>
			<Icon name="trash" />
		</button>

		<input
			bind:this={importInput}
			type="file"
			accept="application/json,.json"
			class="hidden"
			onchange={(event) => {
				const file = event.currentTarget.files?.[0];
				if (file) importFile(file);
				event.currentTarget.value = '';
			}}
		/>
	</div>

	<div class="flex flex-col">
		<div
			class="grid grid-cols-[2rem_4.5rem_4.5rem_4.5rem_4.5rem_1fr_4.5rem] gap-2 px-2 pb-1 text-xs text-text-700"
		>
			<span></span><span class="text-right">x</span><span class="text-right">y</span><span
				class="text-right"
				title="Tolerance">±</span
			><span class="text-right" title="Heading in radians">θ</span>
		</div>

		<!-- Start -->
		<div
			class="rounded-sm border-l-2 {selected === 'start'
				? 'border-accent-300 bg-accent-500/10'
				: 'border-transparent hover:bg-accent-500/5'}"
		>
			<!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_static_element_interactions -->
			<div
				class="grid cursor-pointer grid-cols-[2rem_4.5rem_4.5rem_4.5rem_4.5rem_1fr_4.5rem] items-center gap-2 px-2 py-1"
				onclick={() => (selected = selected === 'start' ? null : 'start')}
				onfocusin={() => (selected = 'start')}
			>
				<span
					title="Start"
					class="h-4 w-4 rounded-sm border-2 {alliance === 'red'
						? 'border-red-400 bg-red-500/30'
						: 'border-blue-400 bg-blue-500/30'}"
				></span>
				{@render number(program.start.x, 'Start x', (value) => setStart('x', value))}
				{@render number(program.start.y, 'Start y', (value) => setStart('y', value))}
				<span></span>
				{@render angle(program.start.rotation, 'Start heading', (value) =>
					setRotation(program.start, value)
				)}
				{@render chips(program.startActions)}
				<span></span>
			</div>

			{#if selected === 'start'}
				<div class="ml-10 pr-2 pb-3">
					<ActionList bind:actions={program.startActions} />
				</div>
			{/if}
		</div>

		{#each waypoints as point, i}
			<div
				class="group rounded-sm border-l-2 {selected === i
					? 'border-accent-300 bg-accent-500/10'
					: 'border-transparent hover:bg-accent-500/5'}"
			>
				<!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_static_element_interactions -->
				<div
					class="grid cursor-pointer grid-cols-[2rem_4.5rem_4.5rem_4.5rem_4.5rem_1fr_4.5rem] items-center gap-2 px-2 py-1"
					onclick={() => (selected = selected === i ? null : i)}
					onfocusin={() => (selected = i)}
				>
					<span
						class="grid h-5 w-5 place-items-center rounded-full border text-[0.65rem] font-semibold
							{selected === i
							? 'border-accent-100 bg-accent-200 text-background-950'
							: 'border-accent-300 text-accent-200'}">{i + 1}</span
					>
					{@render number(point.x, `Waypoint ${i + 1} x`, (value) =>
						setCoordinate(point, 'x', value)
					)}
					{@render number(point.y, `Waypoint ${i + 1} y`, (value) =>
						setCoordinate(point, 'y', value)
					)}
					{@render number(point.tolerance, `Waypoint ${i + 1} tolerance`, (value) =>
						setCoordinate(point, 'tolerance', value)
					)}
					{@render angle(point.rotation, `Waypoint ${i + 1} heading`, (value) =>
						setRotation(point, value)
					)}
					{@render chips(point.actions)}

					<!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_static_element_interactions -->
					<span class="flex justify-end" onclick={(event) => event.stopPropagation()}>
						<button
							class="grid h-6 w-6 place-items-center rounded-sm {point.intake
								? 'bg-yellow-300/20 text-yellow-300'
								: 'text-text-700 hover:text-text-300'}"
							aria-label="Collect while driving to waypoint {i + 1}"
							aria-pressed={point.intake}
							title="Collect on the way"
							onclick={() => (point.intake = !point.intake)}
						>
							<Icon name="intake" size={14} />
						</button>
						<span class="flex opacity-0 group-focus-within:opacity-100 group-hover:opacity-100">
							<button
								class={rowButton}
								aria-label="Move up"
								disabled={i === 0}
								onclick={() => move(i, -1)}
							>
								<Icon name="chevronUp" size={14} />
							</button>
							<button
								class={rowButton}
								aria-label="Move down"
								disabled={i === waypoints.length - 1}
								onclick={() => move(i, 1)}
							>
								<Icon name="chevronDown" size={14} />
							</button>
							<button
								class="{rowButton} hover:text-red-400"
								aria-label="Remove waypoint {i + 1}"
								onclick={() => remove(i)}
							>
								<Icon name="x" size={14} />
							</button>
						</span>
					</span>
				</div>

				{#if selected === i}
					<div class="ml-10 pr-2 pb-3">
						<ActionList bind:actions={point.actions} />
					</div>
				{/if}
			</div>
		{/each}

		<button
			class="mt-2 grid h-8 place-items-center rounded-sm border border-dashed border-accent-500/30 text-text-600 hover:border-accent-400 hover:text-text-200"
			aria-label="Add waypoint"
			title="Add waypoint"
			onclick={addWaypoint}
		>
			<Icon name="plus" />
		</button>
	</div>
</div>
