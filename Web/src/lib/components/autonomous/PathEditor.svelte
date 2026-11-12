<script lang="ts">
	import {
		clamp,
		fetchPath,
		toJava,
		uploadPath,
		DEFAULT_TOLERANCE,
		type Alliance,
		type Waypoint
	} from '$lib/autonomous';

	let {
		path = $bindable(),
		selected = $bindable(),
		alliance = $bindable()
	}: { path: Waypoint[]; selected: number | null; alliance: Alliance } = $props();

	let status: { type: 'info' | 'error'; msg: string } | null = $state(null);
	let busy = $state(false);

	const btn =
		'rounded-sm border border-accent-500 px-2 py-0.5 hover:bg-accent-500/20 disabled:opacity-40';

	const java = $derived(toJava(path));

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
			await uploadPath(path);
			return `Uploaded ${path.length} waypoint${path.length === 1 ? '' : 's'} to the robot`;
		});

	const download = () =>
		run(async () => {
			path = await fetchPath();
			selected = null;
			return `Loaded ${path.length} waypoint${path.length === 1 ? '' : 's'} from the robot`;
		});

	const copyJava = () =>
		run(async () => {
			await navigator.clipboard.writeText(java);
			return 'Copied Java to clipboard';
		});

	function move(index: number, offset: number) {
		const target = index + offset;
		if (target < 0 || target >= path.length) return;

		[path[index], path[target]] = [path[target], path[index]];
		selected = target;
	}

	function remove(index: number) {
		path.splice(index, 1);
		selected = null;
	}

	function setCoordinate(point: Waypoint, key: 'x' | 'y' | 'tolerance', value: number) {
		if (!Number.isFinite(value)) return;
		point[key] = key === 'tolerance' ? clamp(value, 0.1, 50) : clamp(value);
	}
</script>

<div class="flex flex-col gap-4 p-4">
	<div class="flex flex-wrap items-center gap-2">
		<label class="mr-2 flex items-center gap-2">
			Alliance
			<select
				bind:value={alliance}
				class="rounded-sm border border-accent-500 bg-background-950 px-1 py-0.5"
			>
				<option value="red">Red</option>
				<option value="blue">Blue</option>
			</select>
		</label>

		<button class={btn} disabled={busy} onclick={download}>Load from robot</button>
		<button class={btn} disabled={busy} onclick={upload}>Upload to robot</button>
		<button class={btn} disabled={busy || path.length === 0} onclick={copyJava}>Copy Java</button>
		<button
			class="{btn} border-red-500/60 text-red-400"
			disabled={path.length === 0}
			onclick={() => {
				path = [];
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
		Click the field to add a waypoint, drag to move it, right-click to delete. Units are
		Coordination units (0–100). The uploaded path runs when the Auto OpMode starts.
	</p>

	<table class="w-full text-left">
		<thead>
			<tr class="border-b border-primary-500 text-text-600">
				<th class="py-1 pl-2">#</th>
				<th>X</th>
				<th>Y</th>
				<th>Tolerance</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			{#each path as point, i}
				<tr
					class="border-b border-accent-500/20 {selected === i ? 'bg-accent-500/10' : ''}"
					onfocusin={() => (selected = i)}
				>
					<td class="py-1 pl-2">{i + 1}</td>
					{#each ['x', 'y', 'tolerance'] as const as key}
						<td>
							<input
								type="number"
								step="0.5"
								class="w-20 rounded-sm border border-accent-500/50 bg-background-950 px-1"
								value={point[key]}
								onchange={(event) => setCoordinate(point, key, event.currentTarget.valueAsNumber)}
							/>
						</td>
					{/each}
					<td class="whitespace-nowrap">
						<button class="px-1" title="Move up" disabled={i === 0} onclick={() => move(i, -1)}
							>↑</button
						>
						<button
							class="px-1"
							title="Move down"
							disabled={i === path.length - 1}
							onclick={() => move(i, 1)}>↓</button
						>
						<button class="px-1 text-red-400" title="Delete" onclick={() => remove(i)}>✕</button>
					</td>
				</tr>
			{:else}
				<tr>
					<td colspan="5" class="py-4 text-center text-text-700">No waypoints yet</td>
				</tr>
			{/each}
		</tbody>
	</table>

	<button
		class="{btn} self-start"
		onclick={() => {
			const last = path.at(-1);
			path.push({
				x: last?.x ?? 50,
				y: last?.y ?? 50,
				tolerance: last?.tolerance ?? DEFAULT_TOLERANCE
			});
			selected = path.length - 1;
		}}
	>
		Add waypoint
	</button>

	{#if path.length > 0}
		<pre
			class="overflow-x-auto rounded-sm border border-accent-500/40 p-2 text-xs text-text-400">{java}</pre>
	{/if}
</div>
