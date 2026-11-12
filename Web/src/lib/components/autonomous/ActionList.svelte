<script lang="ts">
	import {
		clamp,
		defaultAction,
		describeAction,
		ACTION_LABELS,
		MAX_DURATION_MS,
		type Action,
		type ActionType
	} from '$lib/autonomous';

	let { actions = $bindable(), btn }: { actions: Action[]; btn: string } = $props();

	const input = 'rounded-sm border border-accent-500/50 bg-background-950 px-1';

	function move(index: number, offset: number) {
		const target = index + offset;
		if (target < 0 || target >= actions.length) return;

		[actions[index], actions[target]] = [actions[target], actions[index]];
	}

	function setDuration(action: Action & { duration: number }, value: number) {
		if (Number.isFinite(value)) action.duration = Math.round(clamp(value, 0, MAX_DURATION_MS));
	}
</script>

<div class="flex flex-col gap-2">
	{#each actions as action, i}
		<div class="flex flex-wrap items-center gap-2 border-b border-accent-500/20 py-1">
			<span class="w-5 text-text-700">{i + 1}.</span>
			<span class="w-20">{ACTION_LABELS[action.type]}</span>

			{#if action.type === 'intake'}
				<select class={input} bind:value={action.mode}>
					<option value="in">In</option>
					<option value="out">Out (eject)</option>
					<option value="off">Off</option>
				</select>
			{:else if action.type === 'launcher'}
				<select
					class={input}
					value={action.enabled ? 'on' : 'off'}
					onchange={(event) => (action.enabled = event.currentTarget.value === 'on')}
				>
					<option value="on">Spin up</option>
					<option value="off">Stop</option>
				</select>
				<label class="flex items-center gap-1">
					hood
					<input
						type="number"
						min="0"
						max="180"
						class="{input} w-16"
						value={action.angle}
						onchange={(event) => {
							const value = event.currentTarget.valueAsNumber;
							if (Number.isFinite(value)) action.angle = clamp(value, 0, 180);
						}}
					/>°
				</label>
			{:else if action.type === 'feed'}
				<select class={input} bind:value={action.piece}>
					<option value="pollen">Pollen</option>
					<option value="nectar">Nectar</option>
				</select>
				<label class="flex items-center gap-1">
					for
					<input
						type="number"
						min="0"
						step="100"
						class="{input} w-20"
						value={action.duration}
						onchange={(event) => setDuration(action, event.currentTarget.valueAsNumber)}
					/>ms
				</label>
			{:else if action.type === 'wait'}
				<input
					type="number"
					min="0"
					step="100"
					class="{input} w-20"
					value={action.duration}
					onchange={(event) => setDuration(action, event.currentTarget.valueAsNumber)}
				/>ms
			{/if}

			<span class="ml-auto whitespace-nowrap">
				<button class="px-1" title="Move up" disabled={i === 0} onclick={() => move(i, -1)}
					>↑</button
				>
				<button
					class="px-1"
					title="Move down"
					disabled={i === actions.length - 1}
					onclick={() => move(i, 1)}>↓</button
				>
				<button
					class="px-1 text-red-400"
					title="Delete {describeAction(action)}"
					onclick={() => actions.splice(i, 1)}>✕</button
				>
			</span>
		</div>
	{:else}
		<p class="text-text-700">No actions. The robot moves straight on.</p>
	{/each}

	<div class="flex flex-wrap gap-2">
		{#each Object.keys(ACTION_LABELS) as ActionType[] as type}
			<button class={btn} onclick={() => actions.push(defaultAction(type))}
				>+ {ACTION_LABELS[type]}</button
			>
		{/each}
	</div>
</div>
