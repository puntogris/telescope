<script lang="ts">
	import ClassKotlinIcon from '$lib/icons/classKotlinIcon.svelte';
	import CommitIcon from '$lib/icons/commitIcon.svelte';
	import DotsIcon from '$lib/icons/dotsIcon.svelte';
	import DotsVertical from '$lib/icons/dotsVertical.svelte';
	import FolderIcon from '$lib/icons/folderIcon.svelte';
	import LineIcon from '$lib/icons/lineIcon.svelte';
	import MarkdownIcon from '$lib/icons/markdownIcon.svelte';
	import PullRequestIcon from '$lib/icons/pullRequestIcon.svelte';
	import ShapesIcon from '$lib/icons/shapesIcon.svelte';
	import SyncIcon from '$lib/icons/syncIcon.svelte';
	import TelescopeIcon from '$lib/icons/telescopeIcon.svelte';
	import TerminalIcon from '$lib/icons/terminalIcon.svelte';
	import XIcon from '$lib/icons/xIcon.svelte';
	import { samples } from '$lib/samples';
	import { tick } from 'svelte';
	import { twMerge } from 'tailwind-merge';

	let filtered = $state(samples);
	let fuzzyEnabled = $state(true);
	let embeddingsEnabled = $state(false);
	let showTerminal = $state(false);
	let terminalLogs: string[] = $state(['running at telescope.puntogris.com...']);

	async function filterSamples(query: string) {
		registerLog(`Resolving query: ${query}`);
		if (!query) {
			filtered = samples;
			registerLog(`No query, returning all samples`, true);
		} else if (fuzzyEnabled) {
			filtered = applyFuzzyFilter(query);
			if (filtered.length === 0) {
				registerLog(`No fuzzy matches for query ${query}`, true);
			} else {
				const matches = filtered.map((i) => i.name).join('\n -');
				registerLog(`Fuzzy match for query ${query}: \n -${matches}`, true);
			}
		} else {
			filtered = await applyEmbeddingsFilter(query);
		}
	}

	function registerLog(log: string, withSparator = false) {
		terminalLogs.push(log);
		if (withSparator) {
			terminalLogs.push('--------------------------------------------');
		}
		tick().then(() => scrollToBottom());
	}

	function scrollToBottom() {
		const terminal = document.getElementById('terminal-logs');
		if (terminal) {
			terminal.scrollTop = terminal.scrollHeight;
		}
	}

	function applyFuzzyFilter(query: string) {
		return samples.filter((i) => i.name.toLowerCase().includes(query.toLowerCase()));
	}

	function summarizeList(list: number[]) {
		if (list.length <= 10) {
			return `[${list.join(',')}]`;
		}

		const start = list.slice(0, 5);
		const end = list.slice(-5);

		return `[${start.join(',')},...,${end.join(',')}]`;
	}

	async function applyEmbeddingsFilter(query: string) {
		const response = await fetch(`/api?query=${query}`);
		if (!response.ok) {
			return [];
		}

		const result = await response.json();
		const textEmbeddings = result.embeddings as [];
		registerLog(`Embeddings for ${query}: ${summarizeList(textEmbeddings)}`);

		const scores = [];
		for (const sample of samples) {
			scores.push({
				sample: sample,
				score: dotProduct(textEmbeddings, sample.embeddings)
			});
		}

		return scores.sort((a, b) => b.score - a.score).map((i) => i.sample);
	}

	function dotProduct(vectorA: number[], vectorB: number[]) {
		if (vectorA.length !== vectorB.length) {
			throw new Error('Vectors must have the same length');
		}

		let dotProduct = 0;
		for (let i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
		}

		return dotProduct;
	}

	function updateCheckboxs(mode: string) {
		const isFuzzy = mode === 'fuzzy';
		fuzzyEnabled = isFuzzy;
		embeddingsEnabled = !isFuzzy;
	}
</script>

<svelte:head>
	<title>telescope.puntogris</title>
</svelte:head>

<div class="grid h-screen grid-cols-2">
	<div class="flex overflow-hidden bg-ide-bg">
		<div class="flex flex-col gap-6 border-r border-ide-border-dark p-3 text-ide-text">
			<FolderIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
			<CommitIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
			<TelescopeIcon class="size-10 rounded-md bg-blue-500 p-2" />
			<ShapesIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
			<PullRequestIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
			<DotsIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
			<button class="mt-auto" onclick={() => (showTerminal = !showTerminal)}>
				<TerminalIcon
					class={twMerge(
						'size-10 rounded-md p-2 hover:bg-zinc-700',
						showTerminal ? 'bg-zinc-700' : 'animate-pulse'
					)}
				/>
			</button>
		</div>
		<div class="flex w-full flex-col">
			<div
				class="flex h-12 shrink-0 items-center justify-center gap-6 border-b border-ide-border-dark px-4 text-ide-text"
			>
				<div class="font-semibold">Telescope</div>
				<div class="relative flex h-full px-4">
					<div class="self-center">Home</div>
					<div class="absolute bottom-0 left-0 h-1 w-full rounded-full bg-blue-500"></div>
				</div>
				<div>Settings</div>
				<DotsVertical class="ml-auto size-5" />
				<LineIcon class="size-5" />
			</div>
			<div class="flex gap-4 p-4 text-ide-text">
				<div>Filters:</div>
				<label class="flex items-center gap-1.5">
					<input
						class="rounded p-1 checked:bg-blue-500"
						type="checkbox"
						id="exampleCheckbox"
						bind:checked={fuzzyEnabled}
						onchange={(e) => {
							if (e.currentTarget.checked) updateCheckboxs('fuzzy');
						}}
					/>
					Fuzzy
				</label>
				<label class="flex items-center gap-1.5">
					<input
						class="rounded p-1 checked:bg-blue-500"
						type="checkbox"
						bind:checked={embeddingsEnabled}
						onchange={(e) => {
							if (e.currentTarget.checked) updateCheckboxs('embeddings');
						}}
					/>
					Embeddings
				</label>
				<SyncIcon class="ml-auto size-5" />
			</div>
			<input
				class="mx-4 rounded-md border border-ide-border-dark bg-transparent px-4 py-2 text-ide-text outline-none focus:ring-2 focus:ring-blue-500"
				type="text"
				oninput={(e) => filterSamples(e.currentTarget.value)}
			/>
			<div class="mt-1 flex flex-col gap-3 overflow-y-auto p-4">
				{#each filtered as sample}
					<div class="flex gap-6">
						<div
							class="chess flex size-24 shrink-0 items-center justify-center border border-ide-border-dark"
						>
							<img class="size-12" src={sample.path} alt="icon" />
						</div>

						<div class="flex w-full flex-col justify-between border-b border-ide-border-dark py-2">
							<div class="text-ide-text">{sample.name}</div>
							<div class="text-zinc-500">:app | 1 version</div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
	<div class="flex flex-col">
		<div
			class="flex h-12 shrink-0 items-center gap-6 border-b border-ide-border-light text-ide-text"
		>
			<div class="relative flex h-full px-4">
				<div class="flex items-center gap-2">
					<MarkdownIcon />
					<div class="self-center text-sm">README.md</div>
					<XIcon class="size-3 text-zinc-400" />
				</div>
				<div class="absolute bottom-0 left-0 h-1 w-full rounded-full bg-blue-500"></div>
			</div>
			<a
				href="https://www.github.com/puntogris/telescope"
				target="_blank"
				class="flex h-full items-center gap-2 text-sm text-ide-text hover:text-white"
			>
				<ClassKotlinIcon />
				VisitGithubRepository.kt
			</a>
			<a
				href="https://www.puntogris.com"
				target="_blank"
				class="flex h-full items-center gap-2 text-sm text-ide-text hover:text-white"
			>
				<ClassKotlinIcon />
				VisitPuntogrisWebsite.kt
			</a>
			<DotsVertical class="ml-auto mr-3 size-5" />
		</div>
		<div class="flex h-full flex-col gap-1 p-8">
			<h1 class="text-xl font-semibold text-ide-text">Telescope, plugin for Android Studio</h1>
			<p class="mt-2 text-zinc-300">
				Preview of the actual plugin, i got carried away and made a simplified web version of it.
			</p>

			<h1 class="mt-4 text-lg font-semibold text-ide-text">How it works</h1>
			<p class="mt-2 text-zinc-300">
				It follows the same approach where we generate embeddings for text and images using
				OpenCLIP-compatible models that are converted to the GGUF format. This allows us to leverage
				the clip.cpp Python bindings to run inference.
			</p>
			<p class="mt-2 text-zinc-300">
				I generated the image embeddings ahead of time, and stored them in a JSON file. When
				searching, we generate the text embeddings and order them by similarity score.
			</p>
			<h1 class="mt-4 text-lg font-semibold text-ide-text">Tip of the day</h1>
			<p class="mt-2 text-zinc-300">
				In the bottom-left corner, you'll find the terminal button, where you can view the process
				logs. You can also see the embeddings of the text and images being used for the search.
			</p>
			<h1 class="mt-4 text-lg font-semibold text-ide-text">So it goes</h1>
			<p class="mt-2 text-zinc-300">
				If you're here, you probably already know that this project, is open source and somewhat of
				an experiment. Feel free to check out the code via the link at the top, compile and have
				fun!
			</p>
			<div class="ml-auto mt-auto text-sm text-zinc-600">puntogris corporation ltd</div>
		</div>
	</div>
</div>

{#if showTerminal}
	<div
		class="fixed bottom-0 right-0 flex h-1/2 w-1/2 flex-col border border-ide-border-dark bg-zinc-900"
	>
		<div class="flex items-center justify-between bg-ide-bg p-2">
			<div class="text-sm font-semibold text-ide-text">Terminal</div>
			<button class="rounded p-1 hover:bg-zinc-700" onclick={() => (showTerminal = !showTerminal)}>
				<LineIcon class="size-5 text-ide-text" />
			</button>
		</div>

		<div id="terminal-logs" class="flex flex-col gap-2 overflow-y-auto p-2">
			<div class="text-sm text-green-400">
				puntogris@pc ~/telescope (main) &gt; ./start-telescope
			</div>
			{#each terminalLogs as log}
				<div class="whitespace-pre-wrap text-sm text-ide-text">
					{log}
				</div>
			{/each}
		</div>
	</div>
{/if}

<style>
	.chess {
		background: repeating-conic-gradient(#393a3b 0 90deg, #414243 0 180deg) 0 0/25% 25%;
	}
</style>
