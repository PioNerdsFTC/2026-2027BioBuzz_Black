<script lang="ts">
	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';

	import Board from '$lib/components/board/Board.svelte';
	import Robot from '$lib/components/board/Robot.svelte';
	import PathOverlay from '$lib/components/autonomous/PathOverlay.svelte';
	import PathEditor from '$lib/components/autonomous/PathEditor.svelte';
	import { loadLocalPath, saveLocalPath, type Alliance, type Waypoint } from '$lib/autonomous';

	let healthy = $state(false);
	let ws: WebSocket | undefined = $state();
	let healthInterval: ReturnType<typeof setInterval>;

	let mode = $state<'live' | 'autonomous'>('live');

	let path = $state<Waypoint[]>([]);
	let selected = $state<number | null>(null);
	let alliance = $state<Alliance>('red');
	let pathLoaded = false;

	$effect(() => {
		const snapshot = $state.snapshot(path);
		if (pathLoaded) saveLocalPath(snapshot);
	});

	onMount(() => {
		path = loadLocalPath();
		pathLoaded = true;

		healthInterval = setInterval(async () => {
			try {
				const response = await fetch('/dashboard/api/get-health');
				healthy = response.ok;
			} catch {
				// fetch rejects (rather than returning a failed response) when the robot is unreachable
				healthy = false;
			}
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
				let json;

				try {
					json = JSON.parse(e.data);

					if (json.type === 'log') {
						const object = JSON.parse(json.payload);
						logs.push({ tag: object.tag, content: object.msg, type: object.type });

						if (logs.length > MAX_LOGS) logs.splice(0, logs.length - MAX_LOGS);
					}

					if (json.type === 'position') {
						robot = JSON.parse(json.payload);
					}
				} catch (error) {
					console.warn('Ignoring malformed dashboard message', e.data, error);
				}
			};

			ws!.onclose = () => {
				setTimeout(() => {
					ws = new WebSocket('ws://' + location.hostname + ':8081');
					setupWebsocket();
				}, 1000);
			};
		}

		return () => {
			clearInterval(healthInterval);
		};
	});

	const MAX_LOGS = 500;

	const logs: {
		tag: string;
		content: string;
		type: 'debug' | 'info' | 'warn' | 'error';
	}[] = $state([]);

	let robot = $state({
		x: 50,
		y: 50,
		rotation: (2 * Math.PI) / 3
	});
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
	<div class="relative flex h-full w-full justify-between font-mono">
		<div class="flex grow flex-col">
			<div class="ml-2 py-4">
				<h1 class="ml-4 text-2xl text-text-500">Robot</h1>
			</div>

			<Board bind:robot>
				<Robot bind:x={robot.x} bind:y={robot.y} bind:rotation={robot.rotation} />

				{#if mode === 'autonomous'}
					<PathOverlay bind:path bind:selected {alliance} />
				{/if}
			</Board>
		</div>
		<div class="relative m-4 flex w-[40vw] overflow-scroll border-l border-primary-500">
			<div
				class="absolute top-0 right-0 bottom-0 left-0 flex min-w-[40vw] flex-col gap-0 pl-2 text-text-500"
			>
				<div class="sticky top-0 border-b border-primary-500 bg-background-950 py-2 pl-3">
					<h1 class="ml-4 text-2xl text-text-500">Logs</h1>
				</div>
				<div class="p-4">
					<table class="mr-0 whitespace-nowrap">
						<tbody>
							{#each logs as log}
								<tr in:fade class="border-b border-accent-500/20">
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
								{log.type === 'error' ? 'text-red-500' : ''}"
									>
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
</div>
