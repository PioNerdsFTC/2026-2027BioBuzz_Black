<script lang="ts" module>
	// Stroke icons on a 24px grid (Lucide-style)
	const ICONS = {
		bot: [
			'M12 8V4H8',
			'M2 14h2',
			'M20 14h2',
			'M15 13v2',
			'M9 13v2',
			'M6 8h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2Z'
		],
		file: ['M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z', 'M14 2v4a2 2 0 0 0 2 2h4'],
		down: ['M12 5v14', 'm19 12-7 7-7-7'],
		up: ['M12 19V5', 'm5 12 7-7 7 7'],
		trash: [
			'M3 6h18',
			'M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6',
			'M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2'
		],
		plus: ['M5 12h14', 'M12 5v14'],
		x: ['M18 6 6 18', 'm6 6 12 12'],
		chevronUp: ['m18 15-6-6-6 6'],
		chevronDown: ['m6 9 6 6 6-6'],
		intake: ['M12 3v12', 'm6 9 6 6 6-6', 'M5 21h14'],
		launcher: ['M12 21V9', 'm6 15 6-6 6 6', 'M5 3h14'],
		feed: ['m6 17 5-5-5-5', 'm13 17 5-5-5-5'],
		wait: ['M12 6v6l4 2', 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z']
	};

	export type IconName = keyof typeof ICONS;
</script>

<script lang="ts">
	let {
		name,
		size = 16,
		class: className = ''
	}: { name: IconName; size?: number; class?: string } = $props();
</script>

<svg
	width={size}
	height={size}
	viewBox="0 0 24 24"
	fill="none"
	stroke="currentColor"
	stroke-width="2"
	stroke-linecap="round"
	stroke-linejoin="round"
	class="shrink-0 {className}"
	aria-hidden="true"
>
	{#each ICONS[name] as d}
		<path {d} />
	{/each}
</svg>
