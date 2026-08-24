<script lang="ts">
	import { getPets } from '$lib/api/pet/PetController';
	import { getPetTypes } from '$lib/api/pet-type/PetTypeController';
	import type { PetResponse, PetTypeResponse } from '$lib/api/models';
	import { Input } from '$lib/components/ui/input';
	import { Badge } from '$lib/components/ui/badge';
	import * as Table from '$lib/components/ui/table';
	import * as Select from '$lib/components/ui/select';
	import { PawPrint, Search } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let pets = $state<PetResponse[]>([]);
	let petTypes = $state<PetTypeResponse[]>([]);
	let loading = $state(true);
	let searchQuery = $state('');
	let selectedTypeId = $state<number | undefined>(undefined);

	let selectedType = $derived(petTypes.find((t) => t.id === selectedTypeId));

	function calculateAge(birthDate: string | undefined): string {
		if (!birthDate) return 'Unknown age';
		const birth = new Date(birthDate);
		const now = new Date();
		const years = Math.floor((now.getTime() - birth.getTime()) / (365.25 * 24 * 60 * 60 * 1000));
		if (years === 0) {
			const months = Math.floor((now.getTime() - birth.getTime()) / (30.44 * 24 * 60 * 60 * 1000));
			return months <= 1 ? '< 1 month old' : `${months} months old`;
		}
		return years === 1 ? '1 year old' : `${years} years old`;
	}

	function lastVisitDate(pet: PetResponse): string {
		if (!pet.visits?.length) return '—';
		const latest = [...pet.visits].sort(
			(a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
		)[0];
		return new Date(latest.date).toLocaleDateString('en-US', {
			day: '2-digit',
			month: 'short',
			year: '2-digit'
		});
	}

	// Filtered pets based on search query and selected type
	let filteredPets = $derived(() => {
		let result = pets;

		if (selectedTypeId) {
			result = result.filter((pet) => pet.type?.id === selectedTypeId);
		}

		if (searchQuery.trim()) {
			const query = searchQuery.toLowerCase();
			result = result.filter(
				(pet) =>
					pet.name?.toLowerCase().includes(query) ||
					pet.ownerFirstName?.toLowerCase().includes(query) ||
					pet.ownerLastName?.toLowerCase().includes(query) ||
					pet.type?.name?.toLowerCase().includes(query)
			);
		}

		return result;
	});

	async function loadPets() {
		loading = true;
		try {
			[pets, petTypes] = await Promise.all([getPets(), getPetTypes()]);
		} catch (err) {
			toast.error('Failed to load pets');
			console.error('Error loading pets:', err);
		} finally {
			loading = false;
		}
	}

	// Load pets on mount
	$effect(() => {
		loadPets();
	});
</script>

<svelte:head>
	<title>Pets | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8 flex items-center gap-3">
		<div class="flex h-12 w-12 items-center justify-center rounded-lg bg-accent/10">
			<PawPrint class="h-6 w-6 text-accent" />
		</div>
		<div>
			<h1 class="text-2xl font-bold text-foreground">Pets</h1>
			<p class="text-sm text-muted-foreground">Look up any patient across all owners</p>
		</div>
	</div>

	<!-- Search & Filter -->
	<div class="mb-6 flex flex-col gap-4 sm:flex-row">
		<div class="relative max-w-md flex-1">
			<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
			<Input
				type="search"
				placeholder="Search by pet, owner, or type..."
				bind:value={searchQuery}
				class="pl-10"
			/>
		</div>
		<Select.Root
			type="single"
			value={selectedTypeId?.toString() ?? ''}
			onValueChange={(value) => (selectedTypeId = value ? Number(value) : undefined)}
		>
			<Select.Trigger class="w-full sm:w-48">
				{selectedType?.name ?? 'All types'}
			</Select.Trigger>
			<Select.Content>
				<Select.Item value="">All types</Select.Item>
				{#each petTypes as petType (petType.id)}
					<Select.Item value={petType.id.toString()}>
						{petType.name}
					</Select.Item>
				{/each}
			</Select.Content>
		</Select.Root>
	</div>

	<!-- Table -->
	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading pets...</p>
		</div>
	{:else if filteredPets().length === 0}
		<div class="card p-12 text-center">
			<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			{#if searchQuery}
				<p class="text-muted-foreground">No pets found matching "{searchQuery}"</p>
			{:else}
				<p class="text-muted-foreground">No pets registered yet</p>
			{/if}
		</div>
	{:else}
		<div class="card overflow-hidden">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Name</Table.Head>
						<Table.Head>Type</Table.Head>
						<Table.Head>Age</Table.Head>
						<Table.Head>Owner</Table.Head>
						<Table.Head>Last Visit</Table.Head>
						<Table.Head class="w-[100px]">Actions</Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each filteredPets() as pet (pet.id)}
						<Table.Row class="hover:bg-muted/50">
							<Table.Cell>
								<a href="/pets/{pet.id}" class="font-medium text-foreground hover:text-primary">
									{pet.name}
								</a>
							</Table.Cell>
							<Table.Cell>
								<Badge variant="secondary">{pet.type?.name ?? 'Unknown'}</Badge>
							</Table.Cell>
							<Table.Cell>
								<span class="text-sm text-muted-foreground">{calculateAge(pet.birthDate)}</span>
							</Table.Cell>
							<Table.Cell>
								<a href="/owners/{pet.ownerId}" class="text-sm text-muted-foreground hover:text-primary">
									{pet.ownerFirstName} {pet.ownerLastName}
								</a>
							</Table.Cell>
							<Table.Cell>
								<span class="text-sm text-muted-foreground">{lastVisitDate(pet)}</span>
							</Table.Cell>
							<Table.Cell>
								<a href="/pets/{pet.id}" class="text-sm font-medium text-primary hover:underline">
									View
								</a>
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>
		<p class="mt-4 text-sm text-muted-foreground">
			Showing {filteredPets().length} of {pets.length} pets
		</p>
	{/if}
</div>
