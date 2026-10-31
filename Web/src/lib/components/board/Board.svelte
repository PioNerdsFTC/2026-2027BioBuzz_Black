<script lang="ts">
	let { children, robot = $bindable() } = $props();
</script>

<div class="mx-2 mb-2 flex h-full w-full border border-primary-500">
	<div class="text-text relative flex grow">
		{@render children?.()}
	</div>
</div>
