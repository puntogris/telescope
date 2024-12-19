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
	import { dotProduct, summarizeList } from '$lib/utils';
	import { twMerge } from 'tailwind-merge';
	import Terminal from './terminal.svelte';

	let filtered = $state(samples);
	let fuzzyEnabled = $state(true);
	let embeddingsEnabled = $state(false);
	let showTerminal = $state(true);
	let timeout: number;
	let terminal: Terminal;

	async function handleSearch(query: string) {
		if (!query) {
			handleNoQuery();
		} else if (fuzzyEnabled) {
			performFuzzySearch(query);
		} else {
			debounceEmbeddingsSearch(query);
		}
	}

	function handleNoQuery() {
		filtered = samples;
		terminal.sendLog(`No query, returning all samples`, true);
	}

	function performFuzzySearch(query: string) {
		terminal.sendLog(`Resolving query with fuzzy: ${query}`);

		filtered = samples.filter((i) => i.name.toLowerCase().includes(query.toLowerCase()));

		if (filtered.length === 0) {
			terminal.sendLog(`No fuzzy matches for query ${query}`, true);
		} else {
			const matches = filtered.map((i) => i.name).join('\n - ');
			terminal.sendLog(`Fuzzy match for query ${query}: \n - ${matches}`, true);
		}
	}

	function debounceEmbeddingsSearch(query: string) {
		clearTimeout(timeout);
		timeout = setTimeout(() => performEmbeddingsSearch(query), 500);
	}

	async function performEmbeddingsSearch(query: string) {
		terminal.sendLog(`Fetching embeddings for ${query}`);
		const response = await fetch(`api/encode?query=${query}`, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json' }
		});
		if (!response.ok) {
			terminal.sendLog(`Fetching for ${query} failed: ${response.status}`, true);
			return [];
		}

		const result = await response.json();
		const textEmbeddings = result.embeddings as [];
		terminal.sendLog(`Embeddings for ${query}: ${summarizeList(textEmbeddings)}`);

		let scores = [];
		for (const sample of samples) {
			scores.push({
				sample: sample,
				score: dotProduct(textEmbeddings, sample.embeddings)
			});
		}
		scores = scores.sort((a, b) => b.score - a.score);
		filtered = scores.map((i) => i.sample);

		const matches = scores.map((i) => `${i.sample.name} (${i.score.toFixed(4)})`).join('\n -');
		terminal.sendLog(`Embeddings similarity scores for query ${query}: \n - ${matches}`, true);
	}

	function updateSearchMode(mode: string) {
		const isFuzzy = mode === 'fuzzy';
		fuzzyEnabled = isFuzzy;
		embeddingsEnabled = !isFuzzy;
	}
</script>

<svelte:head>
	<title>telescope.puntogris</title>
</svelte:head>

<div class="grid h-screen grid-cols-2">
	<div class="bg-ide-bg flex overflow-hidden">
		<div class="border-ide-border-dark text-ide-text flex flex-col gap-6 border-r p-3">
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
				class="border-ide-border-dark text-ide-text flex h-12 shrink-0 items-center justify-center gap-6 border-b px-4"
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
			<div class="text-ide-text flex gap-4 p-4">
				<div class="text-sm">Filters:</div>
				<label class="flex items-center gap-1.5 text-sm">
					<input
						class="rounded p-1 checked:bg-blue-500"
						type="checkbox"
						id="exampleCheckbox"
						bind:checked={fuzzyEnabled}
						onchange={(e) => {
							if (e.currentTarget.checked) updateSearchMode('fuzzy');
						}}
					/>
					Fuzzy
				</label>
				<label class="flex items-center gap-1.5 text-sm">
					<input
						class="rounded p-1 checked:bg-blue-500"
						type="checkbox"
						bind:checked={embeddingsEnabled}
						onchange={(e) => {
							if (e.currentTarget.checked) updateSearchMode('embeddings');
						}}
					/>
					Embeddings
				</label>
				<SyncIcon class="ml-auto size-5" />
			</div>
			<input
				class="border-ide-border-dark text-ide-text mx-4 rounded-md border bg-transparent px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
				type="text"
				placeholder="try related words, adding icon will improve accuracy, e.g. edit icon"
				oninput={(e) => handleSearch(e.currentTarget.value)}
			/>
			<div class="mt-1 flex flex-col gap-3 overflow-y-auto p-4">
				{#each filtered as sample}
					<div class="flex gap-6">
						<div
							class="chess border-ide-border-dark flex size-20 shrink-0 items-center justify-center border"
						>
							<img class="size-12" src={sample.path} alt="icon" />
						</div>

						<div
							class="border-ide-border-dark flex w-full flex-col justify-between border-b py-2 text-sm"
						>
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
			class="border-ide-border-light text-ide-text flex h-12 shrink-0 items-center gap-6 border-b"
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
				class="text-ide-text flex h-full items-center gap-2 text-sm hover:text-white"
			>
				<ClassKotlinIcon />
				VisitGithubRepository.kt
			</a>
			<a
				href="https://www.puntogris.com"
				target="_blank"
				class="text-ide-text flex h-full items-center gap-2 text-sm hover:text-white"
			>
				<ClassKotlinIcon />
				VisitPuntogrisWebsite.kt
			</a>
			<DotsVertical class="ml-auto mr-3 size-5" />
		</div>
		<div class="flex h-full flex-col gap-1 p-8">
			<h1 class="text-ide-text text-xl font-semibold">Telescope, plugin for Android Studio</h1>
			<p class="mt-2 text-zinc-300">
				Preview of the actual plugin, i got carried away and made a simplified web version of it.
			</p>

			<h1 class="text-ide-text mt-4 text-lg font-semibold">How it works</h1>
			<p class="mt-2 text-zinc-300">
				It follows the same approach where we generate embeddings for text and images using
				OpenCLIP-compatible models that are converted to the GGUF format. This allows us to leverage
				the clip.cpp Python bindings to run inference.
			</p>
			<p class="mt-2 text-zinc-300">
				I generated the image embeddings ahead of time, and stored them in a JSON file. When
				searching, we generate the text embeddings and order them by similarity score.
			</p>
			<h1 class="text-ide-text mt-4 text-lg font-semibold">Tip of the day</h1>
			<p class="mt-2 text-zinc-300">
				In the bottom-left corner, you'll find the terminal button, where you can view the process
				logs. You can also see the embeddings of the text and images being used for the search.
			</p>
			<h1 class="text-ide-text mt-4 text-lg font-semibold">So it goes</h1>
			<p class="mt-2 text-zinc-300">
				If you're here, you probably already know that this project, is open source and somewhat of
				an experiment. Feel free to check out the code via the link at the top, compile and have
				fun!
			</p>
			<div class="ml-auto mt-auto text-sm text-zinc-600">puntogris corporation ltd</div>
		</div>
	</div>
</div>

<Terminal bind:show={showTerminal} bind:this={terminal} />

<style>
	.chess {
		background: repeating-conic-gradient(#393a3b 0 90deg, #414243 0 180deg) 0 0/25% 25%;
	}
</style>
