<script lang="ts">
	import { getVisits } from '$lib/api/visit/VisitController';
	import { getVets } from '$lib/api/vet/VetController';
	import type { VisitResponse, VetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import * as Table from '$lib/components/ui/table';
	import * as Select from '$lib/components/ui/select';
	import { Calendar, Search, Loader2, ExternalLink } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let visits = $state<VisitResponse[]>([]);
	let vets = $state<VetResponse[]>([]);
	let loading = $state(true);
	let searchQuery = $state('');
	let selectedVetId = $state<number | undefined>(undefined);

	let selectedVet = $derived(vets.find((v) => v.id === selectedVetId));

	let filteredVisits = $derived(() => {
		let result = visits;

		if (selectedVetId) {
			result = result.filter((visit) => visit.vetId === selectedVetId);
		}

		if (searchQuery.trim()) {
			const query = searchQuery.toLowerCase();
			result = result.filter(
				(visit) =>
					(visit.description ?? '').toLowerCase().includes(query) ||
					visit.date.includes(query) ||
					(visit.petName ?? '').toLowerCase().includes(query) ||
					`${visit.ownerFirstName ?? ''} ${visit.ownerLastName ?? ''}`.toLowerCase().includes(query) ||
					`${visit.vetFirstName ?? ''} ${visit.vetLastName ?? ''}`.toLowerCase().includes(query)
			);
		}

		return result;
	});

	async function loadVisits() {
		loading = true;
		try {
			[visits, vets] = await Promise.all([getVisits(), getVets()]);
		} catch (e) {
			toast.error('Failed to load visits');
			console.error('Error loading visits:', e);
		} finally {
			loading = false;
		}
	}

	$effect(() => {
		loadVisits();
	});
</script>

<svelte:head>
	<title>Visits | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<div class="flex items-center gap-3 mb-2">
			<div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
				<Calendar class="h-5 w-5 text-primary" />
			</div>
			<h1 class="text-3xl font-bold text-foreground">Visits</h1>
		</div>
		<p class="text-muted-foreground">View all veterinary visits across all pets</p>
	</div>

	<!-- Search & Filter -->
	<div class="mb-6 flex flex-col gap-4 sm:flex-row">
		<div class="relative max-w-md flex-1">
			<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
			<Input
				type="text"
				placeholder="Search by pet, owner, vet or date..."
				class="pl-10"
				bind:value={searchQuery}
			/>
		</div>
		<Select.Root
			type="single"
			value={selectedVetId?.toString() ?? ''}
			onValueChange={(value) => (selectedVetId = value ? Number(value) : undefined)}
		>
			<Select.Trigger class="w-full sm:w-48">
				{selectedVet ? `Dr. ${selectedVet.firstName} ${selectedVet.lastName}` : 'All vets'}
			</Select.Trigger>
			<Select.Content>
				<Select.Item value="">All vets</Select.Item>
				{#each vets as vet (vet.id)}
					<Select.Item value={vet.id.toString()}>
						Dr. {vet.firstName} {vet.lastName}
					</Select.Item>
				{/each}
			</Select.Content>
		</Select.Root>
	</div>

	<!-- Content -->
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<Loader2 class="h-8 w-8 animate-spin text-primary" />
		</div>
	{:else if visits.length === 0}
		<div class="rounded-lg border border-dashed p-12 text-center">
			<Calendar class="mx-auto h-12 w-12 text-muted-foreground/50" />
			<h3 class="mt-4 text-lg font-medium text-foreground">No visits yet</h3>
			<p class="mt-2 text-sm text-muted-foreground">
				Visits will appear here once they are recorded for pets.
			</p>
		</div>
	{:else}
		<div class="rounded-lg border bg-card">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Date</Table.Head>
						<Table.Head>Description</Table.Head>
						<Table.Head>Pet</Table.Head>
						<Table.Head>Owner</Table.Head>
						<Table.Head>Seen by</Table.Head>
						<Table.Head class="w-[100px]">Actions</Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each filteredVisits() as visit (visit.id)}
						<Table.Row>
							<Table.Cell class="font-medium">
								{new Date(visit.date).toLocaleDateString()}
							</Table.Cell>
							<Table.Cell>{visit.description}</Table.Cell>
							<Table.Cell>
								<a href="/pets/{visit.petId}" class="text-primary hover:underline">
									{visit.petName}
								</a>
							</Table.Cell>
							<Table.Cell>
								<a href="/owners/{visit.ownerId}" class="text-muted-foreground hover:text-primary">
									{visit.ownerFirstName} {visit.ownerLastName}
								</a>
							</Table.Cell>
							<Table.Cell>
								<span class="text-muted-foreground">
									{#if visit.vetFirstName}
										Dr. {visit.vetFirstName} {visit.vetLastName}
									{:else}
										—
									{/if}
								</span>
							</Table.Cell>
							<Table.Cell>
								<Button variant="ghost" size="sm" href="/pets/{visit.petId}" title="View pet details">
									<ExternalLink class="h-4 w-4" />
								</Button>
							</Table.Cell>
						</Table.Row>
					{:else}
						<Table.Row>
							<Table.Cell colspan={6} class="text-center text-muted-foreground py-8">
								No visits match your search
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>

		<p class="mt-4 text-sm text-muted-foreground">
			Showing {filteredVisits().length} of {visits.length} visits
		</p>
	{/if}
</div>
