<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { getPetForOwner, deletePetForOwner } from '$lib/api/pet/PetController';
	import type { PetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import * as Card from '$lib/components/ui/card';
	import * as Dialog from '$lib/components/ui/dialog';
	import {
		PawPrint,
		Calendar,
		ArrowLeft,
		Pencil,
		Trash2,
		Plus,
		Stethoscope
	} from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let pet = $state<PetResponse | null>(null);
	let loading = $state(true);
	let deleteDialogOpen = $state(false);
	let deleting = $state(false);

	const ownerId = $derived(Number($page.params.id));
	const petId = $derived(Number($page.params.petId));

	async function loadPet() {
		loading = true;
		try {
			pet = await getPetForOwner(ownerId, petId);
		} catch (err) {
			toast.error('Failed to load pet');
			console.error('Error:', err);
		} finally {
			loading = false;
		}
	}

	async function deletePet() {
		deleting = true;
		try {
			await deletePetForOwner(ownerId, petId);
			toast.success('Pet deleted successfully');
			goto(`/owners/${ownerId}`);
		} catch (err) {
			toast.error('Failed to delete pet');
			console.error('Error:', err);
		} finally {
			deleting = false;
			deleteDialogOpen = false;
		}
	}

	function formatDate(dateStr: string | undefined): string {
		if (!dateStr) return 'Unknown';
		return new Date(dateStr).toLocaleDateString('en-US', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}

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

	// Load pet on mount and when IDs change
	$effect(() => {
		if (ownerId && petId) {
			loadPet();
		}
	});
</script>

<svelte:head>
	<title>{pet ? pet.name : 'Pet'} | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Back Button -->
	<div class="mb-6">
		<Button variant="ghost" href="/owners/{ownerId}" class="gap-2">
			<ArrowLeft class="h-4 w-4" />
			Back to Owner
		</Button>
	</div>

	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading pet...</p>
		</div>
	{:else if !pet}
		<div class="card p-12 text-center">
			<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			<p class="text-muted-foreground">Pet not found</p>
		</div>
	{:else}
		<!-- Pet Info Card -->
		<Card.Root class="mb-8">
			<Card.Header>
				<div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
					<div class="flex items-center gap-4">
						<div class="flex h-16 w-16 items-center justify-center rounded-full bg-accent/10">
							<PawPrint class="h-8 w-8 text-accent" />
						</div>
						<div>
							<Card.Title class="text-2xl">{pet.name}</Card.Title>
							<div class="flex items-center gap-2 mt-1">
								<Badge variant="secondary">{pet.type?.name ?? 'Unknown type'}</Badge>
								<span class="text-muted-foreground">•</span>
								<span class="text-muted-foreground">{calculateAge(pet.birthDate)}</span>
							</div>
						</div>
					</div>
					<div class="flex gap-2">
						<Button variant="outline" href="/owners/{ownerId}/pets/{petId}/edit" class="gap-2">
							<Pencil class="h-4 w-4" />
							Edit
						</Button>
						<Button variant="destructive" onclick={() => (deleteDialogOpen = true)} class="gap-2">
							<Trash2 class="h-4 w-4" />
							Delete
						</Button>
					</div>
				</div>
			</Card.Header>
			<Card.Content>
				<div class="flex items-center gap-3 text-muted-foreground">
					<Calendar class="h-5 w-5" />
					<span>Born: {formatDate(pet.birthDate)}</span>
				</div>
			</Card.Content>
		</Card.Root>

		<!-- Visits Section -->
		<div class="mb-6 flex items-center justify-between">
			<h2 class="text-xl font-semibold text-foreground">Visit History</h2>
			<Button href="/owners/{ownerId}/pets/{petId}/visits/new" class="gap-2">
				<Plus class="h-4 w-4" />
				Add Visit
			</Button>
		</div>

		{#if !pet.visits?.length}
			<div class="card p-8 text-center">
				<Stethoscope class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
				<p class="text-muted-foreground">No visits recorded for this pet</p>
				<Button href="/owners/{ownerId}/pets/{petId}/visits/new" class="mt-4 gap-2">
					<Plus class="h-4 w-4" />
					Record First Visit
				</Button>
			</div>
		{:else}
			<div class="space-y-4">
				{#each pet.visits as visit (visit.id)}
					<Card.Root>
						<Card.Content class="pt-6">
							<div class="flex items-start justify-between">
								<div class="flex items-start gap-4">
									<div class="flex h-10 w-10 items-center justify-center rounded-full bg-success/10">
										<Stethoscope class="h-5 w-5 text-success" />
									</div>
									<div>
										<p class="font-medium text-foreground">{visit.description}</p>
										<p class="text-sm text-muted-foreground">{formatDate(visit.date)}</p>
										<p class="text-sm text-muted-foreground">
											{#if visit.vetFirstName}
												Seen by Dr. {visit.vetFirstName} {visit.vetLastName}
											{:else}
												Seen by — not recorded
											{/if}
										</p>
									</div>
								</div>
							</div>
						</Card.Content>
					</Card.Root>
				{/each}
			</div>
		{/if}
	{/if}
</div>

<!-- Delete Confirmation Dialog -->
<Dialog.Root bind:open={deleteDialogOpen}>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Delete Pet</Dialog.Title>
			<Dialog.Description>
				Are you sure you want to delete {pet?.name}? This will also delete all visit records. This action cannot be undone.
			</Dialog.Description>
		</Dialog.Header>
		<Dialog.Footer>
			<Button variant="outline" onclick={() => (deleteDialogOpen = false)} disabled={deleting}>
				Cancel
			</Button>
			<Button variant="destructive" onclick={deletePet} disabled={deleting}>
				{#if deleting}
					Deleting...
				{:else}
					Delete
				{/if}
			</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
