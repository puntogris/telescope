<script lang="ts">
	import ChevronDownIcon from '$lib/icons/chevronDownIcon.svelte';
	import ChevronUpIcon from '$lib/icons/chevronUpIcon.svelte';
	import { tick } from 'svelte';

	const MIN_HEIGHT = 44;
	const HEIGHT_NOT_SET = -1;

	let logs = $state(['running at telescope.puntogris.com...']);
	let initialRect = $state({ y: 0, height: 0 });
	let windowHeight = $state(0);
	let defaultHeight = $state(HEIGHT_NOT_SET);
	let currentHeight = $state(MIN_HEIGHT);
	let isDragging = $state(false);

	let isExpanded = $derived(currentHeight > MIN_HEIGHT);

	$effect(() => {
		if (isExpanded) {
			scrollToBottom();
		}
	});

	$effect(() => {
		if (defaultHeight === HEIGHT_NOT_SET) {
			defaultHeight = (windowHeight / 3) * 2;
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
		currentHeight = isExpanded ? MIN_HEIGHT : defaultHeight;
	}

	function onMouseUp(e: MouseEvent) {
		isDragging = false;
	}

	function onMouseDown(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element) return;

		initialRect = {
			y: e.pageY,
			height: element.getBoundingClientRect().height
		};
		isDragging = true;
	}

	function onMouseMove(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element || !isDragging) return;

		const delta = initialRect.y - e.pageY;
		let newHeight = initialRect.height + delta;

		if (newHeight < MIN_HEIGHT) {
			newHeight = MIN_HEIGHT;
		}
		if (newHeight > windowHeight) {
			newHeight = windowHeight;
		}
		currentHeight = newHeight;
	}
</script>

<svelte:window onmousemove={onMouseMove} bind:innerHeight={windowHeight} />

<div
	id="terminal"
	style="height: {currentHeight}px;"
	class="fixed bottom-0 right-0 flex h-1/2 w-1/2 flex-col border border-ide-border-dark bg-zinc-900"
>
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<header
		onmousedown={onMouseDown}
		onmouseup={onMouseUp}
		class="flex h-11 cursor-ns-resize select-none items-center justify-between bg-ide-bg p-2"
	>
		<h2 class="text-sm font-semibold text-ide-text">Terminal <span class="text-xs text-zinc-500 font-normal"> (resizable)</span></h2>
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
			puntogris@pc ~/telescope (main) &gt; ./start
		</span>
		{#each logs as log}
			<p class="whitespace-pre-wrap text-sm text-ide-text">
				{log}
			</p>
		{/each}
	</div>
</div>
