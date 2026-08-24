<script lang="ts">
	import { getVets } from '$lib/api/vet/VetController';
	import type { VetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Badge } from '$lib/components/ui/badge';
	import * as Table from '$lib/components/ui/table';
	import { Stethoscope, Plus, Search } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let vets = $state<VetResponse[]>([]);
	let loading = $state(true);
	let searchQuery = $state('');

	// Filtered vets based on search query
	let filteredVets = $derived(() => {
		if (!searchQuery.trim()) return vets;
		const query = searchQuery.toLowerCase();
		return vets.filter(
			(vet) =>
				vet.firstName?.toLowerCase().includes(query) ||
				vet.lastName?.toLowerCase().includes(query) ||
				vet.specialties?.some((s) => s.name?.toLowerCase().includes(query))
		);
	});

	async function loadVets() {
		loading = true;
		try {
			vets = await getVets();
		} catch (err) {
			toast.error('Failed to load veterinarians');
			console.error('Error loading vets:', err);
		} finally {
			loading = false;
		}
	}

	// Load vets on mount
	$effect(() => {
		loadVets();
	});
</script>

<svelte:head>
	<title>Veterinarians | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div class="flex items-center gap-3">
			<div class="flex h-12 w-12 items-center justify-center rounded-lg bg-success/10">
				<Stethoscope class="h-6 w-6 text-success" />
			</div>
			<div>
				<h1 class="text-2xl font-bold text-foreground">Veterinarians</h1>
				<p class="text-sm text-muted-foreground">Our team of veterinary specialists</p>
			</div>
		</div>
		<Button href="/vets/new" class="gap-2">
			<Plus class="h-4 w-4" />
			Add Veterinarian
		</Button>
	</div>

	<!-- Search -->
	<div class="mb-6">
		<div class="relative max-w-md">
			<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
			<Input
				type="search"
				placeholder="Search by name or specialty..."
				bind:value={searchQuery}
				class="pl-10"
			/>
		</div>
	</div>

	<!-- Table -->
	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading veterinarians...</p>
		</div>
	{:else if filteredVets().length === 0}
		<div class="card p-12 text-center">
			<Stethoscope class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			{#if searchQuery}
				<p class="text-muted-foreground">No veterinarians found matching "{searchQuery}"</p>
			{:else}
				<p class="text-muted-foreground">No veterinarians registered yet</p>
			{/if}
		</div>
	{:else}
		<div class="card overflow-hidden">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Name</Table.Head>
						<Table.Head>Specialties</Table.Head>
						<Table.Head class="w-[100px]">Actions</Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each filteredVets() as vet (vet.id)}
						<Table.Row class="hover:bg-muted/50">
							<Table.Cell>
								<a href="/vets/{vet.id}" class="font-medium text-foreground hover:text-primary">
									Dr. {vet.firstName} {vet.lastName}
								</a>
							</Table.Cell>
							<Table.Cell>
								<div class="flex flex-wrap gap-1">
									{#each vet.specialties ?? [] as specialty (specialty.id)}
										<Badge variant="outline" class="text-xs">
											{specialty.name}
										</Badge>
									{/each}
									{#if !vet.specialties?.length}
										<Badge variant="outline" class="text-xs">
											General Practice
										</Badge>
									{/if}
								</div>
							</Table.Cell>
							<Table.Cell>
								<Button variant="ghost" size="sm" href="/vets/{vet.id}">
									View
								</Button>
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>
		<p class="mt-4 text-sm text-muted-foreground">
			Showing {filteredVets().length} of {vets.length} veterinarians
		</p>
	{/if}
</div>
