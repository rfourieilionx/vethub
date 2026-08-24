<script lang="ts">
	import { page } from '$app/stores';
	import { Users, Stethoscope, Calendar, Home, Menu, X, PawPrint } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';

	let mobileMenuOpen = $state(false);

	const navItems = [
		{ href: '/', label: 'Home', icon: Home },
		{ href: '/owners', label: 'Owners', icon: Users },
		{ href: '/pets', label: 'Pets', icon: PawPrint },
		{ href: '/vets', label: 'Veterinarians', icon: Stethoscope },
		{ href: '/visits', label: 'Visits', icon: Calendar }
	];

	function isActive(href: string): boolean {
		if (href === '/') {
			return $page.url.pathname === '/';
		}
		return $page.url.pathname.startsWith(href);
	}

	function closeMobileMenu() {
		mobileMenuOpen = false;
	}
</script>

<header class="sticky top-0 z-50 border-b border-border bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/80">
	<div class="container mx-auto px-4">
		<div class="flex h-16 items-center justify-between">
			<!-- Logo -->
			<a href="/" class="flex items-center gap-2 transition-opacity hover:opacity-80">
				<img src="/logo-vethub.svg" alt="VetHub" class="h-10 w-auto" />
			</a>

			<!-- Desktop Navigation -->
			<nav class="hidden md:flex md:items-center md:gap-1">
				{#each navItems as item (item.href)}
					<a
						href={item.href}
						class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors
							{isActive(item.href)
								? 'bg-primary/10 text-primary'
								: 'text-muted-foreground hover:bg-muted hover:text-foreground'}"
					>
						<item.icon class="h-4 w-4" />
						{item.label}
					</a>
				{/each}
			</nav>

			<!-- Mobile Menu Button -->
			<Button
				variant="ghost"
				size="icon"
				class="md:hidden"
				onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
				aria-label={mobileMenuOpen ? 'Close menu' : 'Open menu'}
			>
				{#if mobileMenuOpen}
					<X class="h-5 w-5" />
				{:else}
					<Menu class="h-5 w-5" />
				{/if}
			</Button>
		</div>

		<!-- Mobile Navigation -->
		{#if mobileMenuOpen}
			<nav class="border-t border-border py-4 md:hidden">
				<div class="flex flex-col gap-1">
					{#each navItems as item (item.href)}
						<a
							href={item.href}
							onclick={closeMobileMenu}
							class="flex items-center gap-3 rounded-md px-3 py-3 text-sm font-medium transition-colors
								{isActive(item.href)
									? 'bg-primary/10 text-primary'
									: 'text-muted-foreground hover:bg-muted hover:text-foreground'}"
						>
							<item.icon class="h-5 w-5" />
							{item.label}
						</a>
					{/each}
				</div>
			</nav>
		{/if}
	</div>
</header>
