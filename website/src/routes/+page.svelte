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
	import samples from '$lib/samples.json';
	import { dotProduct, summarizeList } from '$lib/utils';
	import Terminal from './terminal.svelte';

	let filtered = $state(samples);
	let fuzzyEnabled = $state(true);
	let embeddingsEnabled = $state(false);
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
			terminal.sendLog(`Encoded for ${query} failed: ${response.status}`, true);
			return [];
		}

		const result = await response.json();
		const textEmbeddings = result.embeddings as [];
		terminal.sendLog(`Encoded ${query} as: ${summarizeList(textEmbeddings)}`);

		let scores = samples
			.map((sample) => ({
				sample: sample,
				score: dotProduct(textEmbeddings, sample.embeddings)
			}))
			.sort((a, b) => b.score - a.score);

		filtered = scores.map((i) => i.sample);

		const matches = scores.map((i) => `${i.sample.name} (${i.score.toFixed(4)})`).join('\n - ');
		terminal.sendLog(`Similarity scores for query ${query}: \n - ${matches}`, true);
	}

	function updateSearchMode(mode: string) {
		const isFuzzy = mode === 'fuzzy';
		fuzzyEnabled = isFuzzy;
		embeddingsEnabled = !isFuzzy;
		terminal.sendLog(`Search mode: ${mode}`, true);
	}
</script>

<svelte:head>
	<title>telescope.puntogris</title>
</svelte:head>

<div class="grid h-screen grid-cols-2">
	<div class="bg-ide-bg flex overflow-hidden">
		<div class="border-ide-border-dark text-ide-text flex flex-col gap-4 border-r px-2 py-3">
			<FolderIcon class="size-9 p-2" />
			<CommitIcon class="size-9 p-2" />
			<TelescopeIcon class="size-9 rounded-lg bg-blue-500 p-2" />
			<ShapesIcon class="size-9 p-2" />
			<PullRequestIcon class="size-9 p-2" />
			<DotsIcon class="size-9 p-2" />
			<TerminalIcon class="mt-auto size-9 rounded-lg bg-zinc-700 p-2" />
		</div>
		<div class="flex w-full flex-col">
			<div
				class="border-ide-border-dark text-ide-text flex h-12 shrink-0 items-center justify-center gap-6 border-b px-4"
			>
				<div class="text-sm font-semibold">Telescope</div>
				<div class="relative flex h-full px-4">
					<div class="self-center text-sm">Home</div>
					<div class="absolute bottom-0 left-0 h-1 w-full rounded-full bg-blue-500"></div>
				</div>
				<div class="text-sm">Settings</div>
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
				placeholder="try related words, adding icon will improve accuracy, e.g. arrow right icon"
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
			<h1 class="text-ide-text text-lg font-semibold">Telescope, plugin for Android Studio</h1>
			<p class="mt-2 text-sm text-zinc-300">
				Preview of the actual plugin, i got carried away and made a simplified web version of it.
			</p>
			<h1 class="text-ide-text mt-4 font-semibold">How it works</h1>
			<p class="mt-2 text-sm text-zinc-300">
				It uses OpenCLIP-compatible models in GGUF format, leveraging clip.cpp Python bindings for
				inference.
			</p>
			<p class="mt-2 text-sm text-zinc-300">
				This demo runs the ViT-B-32 model with 4-bit quantized laion2B-s34B-b79K weights. Not as
				accurate as others but for 38.6MB it's awesome!
			</p>
			<p class="mt-2 text-sm text-zinc-300">
				Image embeddings were pre-generated and stored in JSON. Searches encode text and compare
				similarity scores.
			</p>
			<h1 class="text-ide-text mt-4 font-semibold">Tip of the day</h1>
			<p class="mt-2 text-sm text-zinc-300">
				Use the terminal located in the bottom-right corner to view process logs, embeddings, and
				similarity scores. It's resizable too!
			</p>
			<h1 class="text-ide-text mt-4 font-semibold">So it goes</h1>
			<p class="mt-2 text-sm text-zinc-300">
				This is an open-source experiment. Check out the code, compile it, and have fun!
			</p>
			<div class="ml-auto mt-6 text-sm text-zinc-600">puntogris corporation ltd</div>
		</div>
	</div>
</div>

<Terminal bind:this={terminal} />

<style>
	.chess {
		background: repeating-conic-gradient(#393a3b 0 90deg, #414243 0 180deg) 0 0/25% 25%;
	}
</style>
