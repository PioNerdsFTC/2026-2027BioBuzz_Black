<script lang="ts">
	let { x = $bindable(), y = $bindable(), rotation = $bindable() } = $props();
</script>

<div
	style="left: {x}%; top: {y}%; transform: rotate({rotation}rad);"
	class="absolute h-8 w-8 rounded-lg bg-primary-700"
></div>
