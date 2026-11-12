<script lang="ts">
	import ActionList from './ActionList.svelte';
	import {
		clamp,
		clampStart,
		emptyProgram,
		fetchProgram,
		parseProgram,
		uploadProgram,
		DEFAULT_TOLERANCE,
		START_POSITIONS,
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
	let busy = $state(false);
	let importInput: HTMLInputElement;

	const btn =
		'rounded-sm border border-accent-500 px-2 py-0.5 hover:bg-accent-500/20 disabled:opacity-40';
	const input = 'w-20 rounded-sm border border-accent-500/50 bg-background-950 px-1';

	const waypoints = $derived(program.waypoints);
	const plural = (n: number) => `${n} waypoint${n === 1 ? '' : 's'}`;

	async function run(action: () => Promise<string>) {
		busy = true;

		try {
			status = { type: 'info', msg: await action() };
		} catch (error) {
			status = { type: 'error', msg: error instanceof Error ? error.message : String(error) };
		} finally {
			busy = false;
		}
	}

	const upload = () =>
		run(async () => {
			await uploadProgram(program);
			return `Uploaded ${plural(waypoints.length)} to the robot`;
		});

	const download = () =>
		run(async () => {
			program = await fetchProgram(alliance);
			selected = null;
			return `Loaded ${plural(program.waypoints.length)} from the robot`;
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
			return `Imported ${plural(imported.waypoints.length)} from ${file.name}`;
		});

	function setAlliance(value: Alliance) {
		alliance = value;
		program.start = { ...START_POSITIONS[value] };
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

	function setCoordinate(point: Waypoint, key: 'x' | 'y' | 'tolerance', value: number) {
		if (!Number.isFinite(value)) return;
		point[key] = key === 'tolerance' ? clamp(value, 0.1, 50) : clamp(value);
	}

	function setStart(key: 'x' | 'y', value: number) {
		if (Number.isFinite(value)) program.start[key] = clampStart(value);
	}
</script>

<div class="flex flex-col gap-4 p-4">
	<div class="flex flex-wrap items-center gap-2">
		<label class="mr-2 flex items-center gap-2">
			Alliance
			<select
				value={alliance}
				onchange={(event) => setAlliance(event.currentTarget.value as Alliance)}
				class="rounded-sm border border-accent-500 bg-background-950 px-1 py-0.5"
			>
				<option value="red">Red</option>
				<option value="blue">Blue</option>
			</select>
		</label>

		<button class={btn} disabled={busy} onclick={download}>Load from robot</button>
		<button class={btn} disabled={busy} onclick={upload}>Upload to robot</button>
		<button class={btn} onclick={exportFile}>Export</button>
		<button class={btn} disabled={busy} onclick={() => importInput.click()}>Import</button>
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
		<button
			class="{btn} border-red-500/60 text-red-400"
			onclick={() => {
				program = emptyProgram(alliance);
				selected = null;
			}}
		>
			Clear
		</button>
	</div>

	{#if status}
		<p class={status.type === 'error' ? 'text-red-500' : 'text-text-400'}>{status.msg}</p>
	{/if}

	<p class="text-sm text-text-700">
		Click the field to add a waypoint, drag to move it, right-click to delete. Drag the start box to
		set where the robot begins. Select a row to give it actions, which run in order once the robot
		arrives. Units are Coordination units (0–100). The uploaded program runs when the Auto OpMode
		starts.
	</p>

	<table class="w-full text-left">
		<thead>
			<tr class="border-b border-primary-500 text-text-600">
				<th class="py-1 pl-2">#</th>
				<th>X</th>
				<th>Y</th>
				<th>Tolerance</th>
				<th>Actions</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<tr
				class="cursor-pointer border-b border-accent-500/20 {selected === 'start'
					? 'bg-accent-500/10'
					: ''}"
				onclick={() => (selected = 'start')}
				onfocusin={() => (selected = 'start')}
			>
				<td class="py-1 pl-2">Start</td>
				{#each ['x', 'y'] as const as key}
					<td>
						<input
							type="number"
							step="0.5"
							class={input}
							value={program.start[key]}
							onchange={(event) => setStart(key, event.currentTarget.valueAsNumber)}
						/>
					</td>
				{/each}
				<td class="text-text-700">—</td>
				<td>{program.startActions.length || ''}</td>
				<td></td>
			</tr>
			{#each waypoints as point, i}
				<tr
					class="cursor-pointer border-b border-accent-500/20 {selected === i
						? 'bg-accent-500/10'
						: ''}"
					onclick={() => (selected = i)}
					onfocusin={() => (selected = i)}
				>
					<td class="py-1 pl-2">{i + 1}</td>
					{#each ['x', 'y', 'tolerance'] as const as key}
						<td>
							<input
								type="number"
								step="0.5"
								class={input}
								value={point[key]}
								onchange={(event) => setCoordinate(point, key, event.currentTarget.valueAsNumber)}
							/>
						</td>
					{/each}
					<td>{point.actions.length || ''}</td>
					<td class="whitespace-nowrap">
						<button class="px-1" title="Move up" disabled={i === 0} onclick={() => move(i, -1)}
							>↑</button
						>
						<button
							class="px-1"
							title="Move down"
							disabled={i === waypoints.length - 1}
							onclick={() => move(i, 1)}>↓</button
						>
						<button class="px-1 text-red-400" title="Delete" onclick={() => remove(i)}>✕</button>
					</td>
				</tr>
			{/each}
		</tbody>
	</table>

	<button
		class="{btn} self-start"
		onclick={() => {
			const last = waypoints.at(-1) ?? program.start;
			waypoints.push({
				x: last.x,
				y: last.y,
				tolerance: waypoints.at(-1)?.tolerance ?? DEFAULT_TOLERANCE,
				actions: []
			});
			selected = waypoints.length - 1;
		}}
	>
		Add waypoint
	</button>

	{#if selected === 'start'}
		<div class="flex flex-col gap-2 border-t border-primary-500 pt-4">
			<h2 class="text-lg text-text-400">Actions at start (before moving)</h2>
			<ActionList bind:actions={program.startActions} {btn} />
		</div>
	{:else if selected !== null && waypoints[selected]}
		<div class="flex flex-col gap-2 border-t border-primary-500 pt-4">
			<h2 class="text-lg text-text-400">Actions at waypoint {selected + 1}</h2>
			<ActionList bind:actions={waypoints[selected].actions} {btn} />
		</div>
	{:else}
		<p class="border-t border-primary-500 pt-4 text-text-700">
			Select the start or a waypoint to edit its actions.
		</p>
	{/if}
</div>
