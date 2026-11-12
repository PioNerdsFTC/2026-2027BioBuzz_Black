<script lang="ts">
	import Icon from '$lib/components/Icon.svelte';
	import {
		clamp,
		defaultAction,
		describeAction,
		ACTION_ICONS,
		ACTION_LABELS,
		MAX_DURATION_MS,
		MAX_SHOTS,
		type Action,
		type ActionType,
		type Piece
	} from '$lib/autonomous';

	let { actions = $bindable() }: { actions: Action[] } = $props();

	const field =
		'h-7 rounded-sm bg-background-900 px-1.5 text-right text-text-200 outline-none focus:ring-1 focus:ring-accent-400';
	const iconButton =
		'grid h-6 w-6 place-items-center rounded-sm text-text-600 hover:bg-accent-500/15 hover:text-text-200 disabled:pointer-events-none disabled:opacity-0';
	const pieces: { value: Piece; label: string; dot: string }[] = [
		{ value: 'pollen', label: 'Pollen', dot: 'bg-yellow-300' },
		{ value: 'nectar', label: 'Nectar', dot: 'bg-orange-400' }
	];

	function move(index: number, offset: number) {
		const target = index + offset;
		if (target < 0 || target >= actions.length) return;

		[actions[index], actions[target]] = [actions[target], actions[index]];
	}

	function setSeconds(action: Action & { duration: number }, seconds: number) {
		if (Number.isFinite(seconds)) {
			action.duration = Math.round(clamp(seconds * 1000, 0, MAX_DURATION_MS));
		}
	}
</script>

{#snippet seconds(action: Action & { duration: number })}
	<input
		type="number"
		min="0"
		step="0.25"
		class="{field} w-16"
		aria-label="Seconds"
		value={action.duration / 1000}
		onchange={(event) => setSeconds(action, event.currentTarget.valueAsNumber)}
	/>
	<span class="text-text-600">s</span>
{/snippet}

<div class="flex flex-col gap-1">
	{#each actions as action, i}
		<div class="group flex h-8 items-center gap-2">
			<span class="flex w-20 items-center gap-1.5 text-accent-200">
				<Icon name={ACTION_ICONS[action.type]} size={14} />
				{ACTION_LABELS[action.type]}
			</span>

			{#if action.type === 'shoot'}
				<span class="flex h-7 items-center rounded-sm bg-background-900">
					<button
						class="h-full px-2 text-text-500 hover:text-text-100 disabled:opacity-30"
						aria-label="Fewer"
						disabled={action.count <= 1}
						onclick={() => action.type === 'shoot' && action.count--}>−</button
					>
					<span class="w-4 text-center text-text-100" aria-label="Count">{action.count}</span>
					<button
						class="h-full px-2 text-text-500 hover:text-text-100 disabled:opacity-30"
						aria-label="More"
						disabled={action.count >= MAX_SHOTS}
						onclick={() => action.type === 'shoot' && action.count++}>+</button
					>
				</span>

				<span class="flex h-7 rounded-sm bg-background-900 p-0.5" role="radiogroup">
					{#each pieces as piece}
						<button
							role="radio"
							aria-checked={action.piece === piece.value}
							class="flex items-center gap-1.5 rounded-sm px-2 text-xs
								{action.piece === piece.value
								? 'bg-accent-500/25 text-text-100'
								: 'text-text-600 hover:text-text-300'}"
							onclick={() => action.type === 'shoot' && (action.piece = piece.value)}
						>
							<span class="h-2 w-2 rounded-full {piece.dot}"></span>
							{piece.label}
						</button>
					{/each}
				</span>

				<label class="flex items-center gap-1 text-text-600" title="Hood angle">
					at
					<input
						type="number"
						min="0"
						max="180"
						class="{field} w-14"
						value={action.angle}
						onchange={(event) => {
							const value = event.currentTarget.valueAsNumber;
							if (action.type === 'shoot' && Number.isFinite(value)) {
								action.angle = clamp(value, 0, 180);
							}
						}}
					/>°
				</label>
			{:else if action.type === 'intake'}
				<span class="text-text-600">for</span>
				{@render seconds(action)}
			{:else}
				{@render seconds(action)}
			{/if}

			<span class="ml-auto flex opacity-0 group-focus-within:opacity-100 group-hover:opacity-100">
				<button
					class={iconButton}
					aria-label="Move up"
					disabled={i === 0}
					onclick={() => move(i, -1)}
				>
					<Icon name="chevronUp" size={14} />
				</button>
				<button
					class={iconButton}
					aria-label="Move down"
					disabled={i === actions.length - 1}
					onclick={() => move(i, 1)}
				>
					<Icon name="chevronDown" size={14} />
				</button>
				<button
					class="{iconButton} hover:text-red-400"
					aria-label="Remove {describeAction(action)}"
					onclick={() => actions.splice(i, 1)}
				>
					<Icon name="x" size={14} />
				</button>
			</span>
		</div>
	{/each}

	<div class="mt-1 flex gap-1">
		{#each Object.keys(ACTION_LABELS) as ActionType[] as type}
			<button
				class="flex h-7 items-center gap-1.5 rounded-sm border border-dashed border-accent-500/40 px-2 text-xs text-text-500 hover:border-accent-400 hover:text-text-200"
				onclick={() => actions.push(defaultAction(type, actions.at(-1)))}
			>
				<Icon name="plus" size={12} />
				{ACTION_LABELS[type]}
			</button>
		{/each}
	</div>
</div>
