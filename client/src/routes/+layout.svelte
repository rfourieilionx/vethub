<script lang="ts">
	import '../app.css';
	import { Toaster } from 'svelte-sonner';
	import { Header, Footer } from '$lib/components/layout';
	import { themeState } from '$lib/theme/theme.svelte';

	let { children } = $props();

	let toasterTheme: 'light' | 'dark' = $derived(themeState.current === 'dark' ? 'dark' : 'light');
</script>

<Toaster position="top-right" richColors closeButton theme={toasterTheme} />

<div class="app-shell flex min-h-screen flex-col">
	<Header />

	<main class="flex-1">
		{@render children()}
	</main>

	<Footer />
</div>
