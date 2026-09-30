<script lang="ts">
	import { onMount } from "svelte";

	let healthy = $state(false);

    onMount(() => {
      setInterval(async() => {
        const response = await fetch("/api/health");

        if (response.ok) {
          healthy = true;
          return;
        }

        healthy = false;
      }, 500)
    })
</script>

<div class="flex flex-row w-screen justify-between">
    <h1>14931 Dashboard</h1>

    <div class="">
        {#if healthy}
            <span>HEALTHY</span>
        {:else}
            <span>NOT HEALTHY</span>
        {/if}
    </div>
</div>
