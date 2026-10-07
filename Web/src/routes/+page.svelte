<script lang="ts">
	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';

	let healthy = $state(false);
	let ws: WebSocket | undefined = $state();

	onMount(() => {
		setInterval(async () => {
			const response = await fetch('/dashboard/api/get-health');

			if (response.ok) {
				healthy = true;
				return;
			}

			healthy = false;
		}, 500);

		ws = new WebSocket('ws://' + location.hostname + ':8081');

		ws.onclose = () => {
			setTimeout(() => {
				ws = new WebSocket('ws://' + location.hostname + ':8081');
				setupWebsocket();
			}, 1000);
		};

		setupWebsocket();

		function setupWebsocket() {
			ws!.onopen = () =>
				ws!.send(
					JSON.stringify({
						namespace: 'system',
						type: 'subscribeToNamespace',
						payload: 'dashboard'
					})
				);

			ws!.onmessage = (e) => {
				const json = JSON.parse(e.data);

				if (json.type === 'log') {
					const object = JSON.parse(json.payload);
					console.log(object);
					logs.push({ tag: object.tag, content: object.msg, type: object.type });
				}
			};

			ws!.onclose = () => {
				setTimeout(() => {
					ws = new WebSocket('ws://' + location.hostname + ':8081');
					setupWebsocket();
				}, 1000);
			};
		}
	});

	const logs: {
		tag: string;
		content: string;
		type: 'debug' | 'info' | 'warn' | 'error';
	}[] = $state([]);
</script>

<div class="flex h-screen w-screen flex-col">
	<div
		class="flex flex-row justify-between border-b border-primary-600 p-4 font-mono text-text-400"
	>
		<h1 class="text-2xl">Dashboard</h1>

		<div class="mr-4 flex items-center justify-center">
			{#if healthy}
				<span class="text-green-500 uppercase">Healthy</span>
			{:else}
				<span class="text-red-500 uppercase">Not Healthy</span>
			{/if}
		</div>
	</div>
	<div class="flex h-full w-full justify-end font-mono">
		<div class="mb-4 ml-4 flex w-[40vw] max-w-[40vw] flex-col gap-0 pl-2 text-text-500">
			<div class="border-b border-l border-primary-500 py-4 pl-3">
				<h1 class="ml-4 text-2xl text-text-500">Logs</h1>
			</div>
			<div class="h-full border-l border-primary-500 p-4">
				<table class="mr-0 ml-auto w-full">
					<tbody>
						{#each logs as log}
							<tr in:fade>
								<td class="py-1 pr-1 pl-2">
									<span
										class="h-full rounded-md px-1.25 py-0.5 text-black
                                        {log.type === 'debug' ? 'bg-blue-300' : ''}
                                        {log.type === 'info' ? 'bg-blue-500' : ''}
                                        {log.type === 'warn' ? 'bg-yellow-500' : ''}
                                        {log.type === 'error' ? 'bg-red-500' : ''}
                                    "
									>
										{log.type.charAt(0).toUpperCase()}
									</span>
									<span class="mx-4 my-1 rounded-sm border border-accent-500 px-1 py-0.5">
										{log.tag}
									</span>
								</td>
								<td
									class="px-1 py-1 
									{log.type === 'debug' ? 'text-text-700' : ''}
									{log.type === 'error' ? 'text-red-500' : ''}">
									{log.content}
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
