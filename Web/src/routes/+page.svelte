<script lang="ts">
	import { onMount } from "svelte";
	import { fade, slide } from "svelte/transition";

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

    const logs: {
      tag: string,
      content: string,
      type: "debug" | "info" | "warn" | "error"
    }[] = $state([
      { tag: "Core", content: "Initialized", type: "info" },
      { tag: "Coordination", content: "Robot is upside down", type: "warn" },
      { tag: "Launcher", content: "Launching Flower", type: "debug" },
      { tag: "Coordination", content: "Whoops!", type: "error" }
    ]);

    onMount(() => {
      setTimeout(() => {
        logs.push({
          tag: "Core",
          content: "Exiting",
          type: "info"
        })
      }, 2000);
    });

</script>

<div class="flex flex-col w-screen h-screen">
    <div class="flex flex-row justify-between text-text-400 p-4 font-mono border-b border-primary-600">
        <h1 class="text-2xl">Dashboard</h1>

        <div class="flex justify-center items-center mr-4">
            {#if healthy}
                <span class="text-green-500 uppercase">Healthy</span>
            {:else}
                <span class="text-red-500 uppercase">Not Healthy</span>
            {/if}
        </div>
    </div>
    <div class="flex justify-end h-full w-full font-mono">
        <div class="flex flex-col max-w-[40vw] pl-2 w-[40vw] ml-4 mb-4 gap-0  text-text-500">
            <div class="pl-3 py-4 border-l border-b border-primary-500">
                <h1 class="text-text-500 text-2xl ml-4">Logs</h1>
            </div>
            <div class="h-full border-l p-4 border-primary-500">
                <table class="w-full mr-0 ml-auto">
                    <tbody>
                        {#each logs as log}
                            <tr in:fade>
                                <td class="pl-2 pr-1 py-1">
                                    <span
                                        class="h-full px-1.25 py-0.5 text-black rounded-md
                                        {log.type === "debug" ? "bg-blue-300" : ""}
                                        {log.type === "info" ? "bg-blue-500" : ""}
                                        {log.type === "warn" ? "bg-yellow-500" : ""}
                                        {log.type === "error" ? "bg-red-500" : ""}
                                    ">
                                        {log.type.charAt(0).toUpperCase()}
                                    </span>
                                    <span class="border border-accent-500 rounded-sm py-0.5 px-1 my-1 mx-4">{log.tag}</span>
                                </td>
                                <td class="px-1 py-1 {log.type === "debug" ? "text-text-700" : ""}  {log.type === "error" ? "text-red-500" : ""}">{log.content}</td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
