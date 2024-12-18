<script lang="ts">
	import '../app.css';
	let { children } = $props();
</script>

<div class="bg-ide-bg-dark">
	{@render children()}
</div>
