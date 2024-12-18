<script lang="ts">
	import { data } from '$lib/data';
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

	let items = data;
	let filteredItems = items;

	let fuzzyEnabled = false;
	let embeddingsEnabled = false;

	function filterItems(query: string) {
		if (query) {
			filteredItems = items.filter((item) => item.name.toLowerCase().includes(query.toLowerCase()));
		} else {
			filteredItems = items;
		}
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

	async function getEmbedding(query: string) {
		const response = await fetch(`/api?query=${query}`);

		if (response.ok) {
			const data = await response.json();
			return data.embeddings;
		} else {
			return '';
		}
	}

	async function search(query: string) {
		if (!query) {
			filteredItems = items;
			return;
		}
		let results: any[] = [];

		if (fuzzyEnabled) {
			results = items.filter((item) => item.name.toLowerCase().includes(query.toLowerCase()));
		}

		if (embeddingsEnabled) {
			const embedding = getEmbedding(query);
			//search across all files
		}

		filteredItems = results;
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
		</div>
		<div class="flex w-full flex-col">
			<div
				class="flex h-14 shrink-0 items-center justify-center gap-6 border-b border-ide-border-dark px-4 text-ide-text"
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
						name="example"
						value="option1"
					/>
					Fuzzy
				</label>
				<label class="flex items-center gap-1.5">
					<input
						class="rounded p-1 checked:bg-blue-500"
						type="checkbox"
						id="exampleCheckbox"
						name="example"
						value="option1"
					/>
					Embeddings
				</label>
				<SyncIcon class="ml-auto size-5" />
			</div>
			<input
				class="mx-4 rounded-md border border-ide-border-dark bg-transparent px-4 py-2 text-ide-text outline-none focus:ring-2 focus:ring-blue-500"
				type="text"
				on:input={(e) => filterItems(e.currentTarget.value)}
			/>
			<div class="mt-1 flex flex-col gap-2 overflow-y-auto p-4">
				{#each filteredItems as item}
					<div class="flex gap-6">
						<div
							class="chess flex size-24 shrink-0 items-center justify-center border border-ide-border-dark"
						>
							<svelte:component this={item.icon} />
						</div>

						<div class="flex w-full flex-col justify-between border-b border-ide-border-dark py-2">
							<div class="text-ide-text">{item.name}</div>
							<div class="text-zinc-500">:app | 1 version</div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
	<div class="flex h-14 shrink-0 items-center gap-6 border-b border-ide-border-light text-ide-text">
		<div class="relative flex h-full px-4">
			<div class="flex items-center gap-2">
				<MarkdownIcon />
				<div class="self-center">README.md</div>
			</div>
			<div class="absolute bottom-0 left-0 h-1 w-full rounded-full bg-blue-500"></div>
		</div>
		<a
			href="puntogris.com"
			target="_blank"
			class="flex h-full items-center gap-2 text-ide-text hover:text-white"
		>
			<ClassKotlinIcon />
			VisitPuntogrisWebsite.kt
		</a>
		<a
			href="github.com/puntogris/telescope"
			target="_blank"
			class="flex h-full items-center gap-2 text-ide-text hover:text-white"
		>
			<ClassKotlinIcon />
			VisitGithubRepository.kt
		</a>
		<DotsVertical class="ml-auto mr-3 size-5" />
	</div>
</div>

<style>
	.chess {
		background: repeating-conic-gradient(#393a3b 0 90deg, #414243 0 180deg) 0 0/25% 25%;
		margin: 15px;
		padding: 10px;
	}
</style>
