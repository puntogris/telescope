<script lang="ts">
	import CommitIcon from '$lib/commitIcon.svelte';
	import DotsIcon from '$lib/dotsIcon.svelte';
	import DotsVertical from '$lib/dotsVertical.svelte';
	import FolderIcon from '$lib/folderIcon.svelte';
	import LineIcon from '$lib/lineIcon.svelte';
	import PullRequestIcon from '$lib/pullRequestIcon.svelte';
	import ShapesIcon from '$lib/shapesIcon.svelte';
	import SyncIcon from '$lib/syncIcon.svelte';
	import TelescopeIcon from '$lib/telescopeIcon.svelte';

	let items = [
		{
			name: 'icon 1'
		},
		{
			name: 'icon 2'
		},
		{
			name: 'icon 3'
		},
		{
			name: 'icon 4'
		}
	];

	let filteredItems = items;

	function filterItems(query: string) {
		if (query) {
			filteredItems = items.filter((item) => item.name.toLowerCase().includes(query.toLowerCase()));
		} else {
			filteredItems = items;
		}
	}
</script>

<div class="flex h-screen w-1/2 overflow-hidden bg-ide-bg">
	<div class="flex flex-col gap-6 border-r border-ide-border p-3 text-ide-text">
		<FolderIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
		<CommitIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
		<TelescopeIcon class="size-10 rounded-md bg-blue-500 p-2" />
		<ShapesIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
		<PullRequestIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
		<DotsIcon class="size-10 rounded-md p-2 hover:bg-zinc-700" />
	</div>
	<div class="flex w-full flex-col">
		<div
			class="flex h-14 items-center justify-center gap-6 border-b border-ide-border px-4 text-ide-text"
		>
			<div class="text-lg font-semibold">Telescope</div>
			<div class="text relative flex h-full px-4">
				<div class="self-center">Home</div>
				<div class="absolute bottom-0 left-0 h-1 w-full rounded-full bg-blue-500"></div>
			</div>
			<div class="text">Settings</div>
			<DotsVertical class="ml-auto size-5" />
			<LineIcon class="size-5" />
		</div>
		<div class="flex gap-4 p-4 text-ide-text">
			<div>Filters:</div>
			<label>
				<input type="checkbox" id="exampleCheckbox" name="example" value="option1" />
				Fuzzy
			</label>
			<label>
				<input type="checkbox" id="exampleCheckbox" name="example" value="option1" />
				Embeddings
			</label>
			<SyncIcon class="ml-auto size-5" />
		</div>
		<input
			class="mx-4 rounded-md border border-ide-border bg-transparent px-4 py-2 text-ide-text outline-none focus:ring-2 focus:ring-blue-500"
			type="text"
			on:input={(e) => filterItems(e.currentTarget.value)}
		/>
		<div class="flex flex-col gap-2 overflow-y-auto p-4">
			{#each filteredItems as item}
				<div class="flex gap-6">
					<div class="chess size-24 shrink-0 border border-ide-border">
						<ShapesIcon />
					</div>

					<div class="flex w-full flex-col justify-between border-b border-ide-border py-2">
						<div class="text-ide-text">{item.name}</div>
						<div class="text-zinc-500">:app | 1 version</div>
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>

<style>
	.chess {
		background: repeating-conic-gradient(#393a3b 0 90deg, #414243 0 180deg) 0 0/25% 25%;
		margin: 15px;
		padding: 10px;
	}
</style>
