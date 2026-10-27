<script lang="ts">
    let { children, robot = $bindable() } = $props();

</script>

<div
    class="w-full h-full border border-primary-500 mb-2 mx-2 flex">
	<div class="text-text relative flex grow">
	    {@render children?.()}
	</div>
</div>
