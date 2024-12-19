<script lang="ts">
	import LineIcon from '$lib/icons/lineIcon.svelte';
	import { tick } from 'svelte';

	let { show = $bindable() }: { show: boolean } = $props();
	let isDragging = $state(false);
	let logs = $state(['running at telescope.puntogris.com...']);

	$effect(() => {
		if (show) {
			scrollToBottom();
		}
	});

	export function sendLog(log: string, withSparator = false) {
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

	function onMouseUp(e: MouseEvent) {
		isDragging = false;
	}

	let initial = $state({
		y: 0,
		height: 0
	});

	function onMoouseDown(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element) return;

		isDragging = true;
		initial.y = e.pageY;
		initial.height = element.getBoundingClientRect().height;
	}

	function onMouseMove(e: MouseEvent) {
		const element = document.getElementById('terminal');
		if (!element || !isDragging) return;

		const delta = initial.y - e.pageY;
		let newHeight = initial.height + delta;

		element.style.height = `${newHeight}px`;
	}
</script>

<svelte:window onmousemove={onMouseMove} />

{#if show}
	<div
		id="terminal"
		class="border-ide-border-dark fixed bottom-0 right-0 flex h-2/3 w-1/2 flex-col border bg-zinc-900"
	>
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			onmousedown={onMoouseDown}
			onmouseup={onMouseUp}
			class="bg-ide-bg flex cursor-ns-resize items-center justify-between p-2"
		>
			<div class="text-ide-text text-sm font-semibold">Terminal</div>
			<button class="rounded p-1 hover:bg-zinc-700" onclick={() => (show = !show)}>
				<LineIcon class="text-ide-text size-5" />
			</button>
		</div>
		<div id="terminal-logs" class="flex flex-col gap-2 overflow-y-auto p-2">
			<div class="text-sm text-green-400">
				puntogris@pc ~/telescope (main) &gt; ./start-telescope
			</div>
			{#each logs as log}
				<div class="text-ide-text whitespace-pre-wrap text-sm">
					{log}
				</div>
			{/each}
		</div>
	</div>
{/if}
