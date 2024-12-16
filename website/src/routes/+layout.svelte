<script lang="ts">
	import '../app.css';
	let { children } = $props();
</script>

<div class="bg-zinc-900">
	{@render children()}
</div>
