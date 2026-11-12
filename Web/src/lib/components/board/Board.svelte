<script lang="ts">
	import Field from './Field.svelte';

	let { children, robot = $bindable() } = $props();
</script>

<!-- The field is square, so size it to the smaller of the available width and height -->
<div class="mx-2 mb-2 flex min-h-0 grow items-center justify-center" style="container-type: size">
	<div
		class="text-text relative border border-primary-500"
		style="width: min(100cqw, 100cqh); height: min(100cqw, 100cqh);"
	>
		<Field />
		{@render children?.()}
	</div>
</div>
