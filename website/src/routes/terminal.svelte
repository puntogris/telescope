<script lang="ts">
	import ChevronDownIcon from '$lib/icons/chevronDownIcon.svelte';
	import ChevronUpIcon from '$lib/icons/chevronUpIcon.svelte';
	import { tick } from 'svelte';

	const MIN_HEIGHT = 44;
	const DEFAULT_EXPANDED_HEIGHT = 400;

	let logs = $state(['running at telescope.puntogris.com...']);
	let initialState = $state({ y: 0, height: 0 });
	let windowHeight = $state(0);
	let terminalHeight = $state(MIN_HEIGHT);
	let isDragging = $state(false);

	let isExpanded = $derived(terminalHeight > MIN_HEIGHT);

	$effect(() => {
		if (isExpanded) {
			scrollToBottom();
		}
	});

	export function send(log: string, withSparator = false) {
		logs.push(log);
		if (withSparator) {
			logs.push('--------------------------------------------');
		}
		tick().then(() => scrollToBottom());
	}

	function scrollToBottom() {
		const terminal = document.getElementById('terminal-logs');
		if (terminal) {
			terminal.scrollTop = terminal.scrollHeight;
		}
	}

	function toggleExpanded() {
		terminalHeight = isExpanded ? MIN_HEIGHT : DEFAULT_EXPANDED_HEIGHT;
	}

	function onMouseUp(e: MouseEvent) {
		isDragging = false;
	}

	function onMouseDown(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element) return;

		initialState = {
			y: e.pageY,
			height: element.getBoundingClientRect().height
		};
		isDragging = true;
	}

	function onMouseMove(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element || !isDragging) return;

		const delta = initialState.y - e.pageY;
		let newHeight = initialState.height + delta;

		if (newHeight < MIN_HEIGHT) {
			newHeight = MIN_HEIGHT;
		}
		if (newHeight > windowHeight) {
			newHeight = windowHeight;
		}
		terminalHeight = newHeight;
	}
</script>

<svelte:window onmousemove={onMouseMove} bind:innerHeight={windowHeight} />

<div
	id="terminal"
	style="height: {terminalHeight}px;"
	class="fixed bottom-0 right-0 flex h-1/2 w-1/2 flex-col border border-ide-border-dark bg-zinc-900"
>
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<header
		onmousedown={onMouseDown}
		onmouseup={onMouseUp}
		class="flex h-11 cursor-ns-resize select-none items-center justify-between bg-ide-bg p-2"
	>
		<h2 class="text-sm font-semibold text-ide-text">Terminal</h2>
		<button class="rounded p-1 hover:bg-zinc-700" onclick={toggleExpanded}>
			{#if isExpanded}
				<ChevronDownIcon class="size-5 text-ide-text" />
			{:else}
				<ChevronUpIcon class="size-5 text-ide-text" />
			{/if}
		</button>
	</header>
	<div id="terminal-logs" class="flex flex-col gap-2 overflow-y-auto p-2">
		<span class="text-sm text-green-400">
			puntogris@pc ~/telescope (main) &gt; ./start-telescope
		</span>
		{#each logs as log}
			<p class="whitespace-pre-wrap text-sm text-ide-text">
				{log}
			</p>
		{/each}
	</div>
</div>
