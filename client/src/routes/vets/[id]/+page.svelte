<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { getVetById, deleteVet } from '$lib/api/vet/VetController';
	import type { VetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import * as Card from '$lib/components/ui/card';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Stethoscope, ArrowLeft, Pencil, Trash2 } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let vet = $state<VetResponse | null>(null);
	let loading = $state(true);
	let deleteDialogOpen = $state(false);
	let deleting = $state(false);

	let vetId = $derived(Number($page.params.id));

	async function loadVet() {
		loading = true;
		try {
			vet = await getVetById(vetId);
		} catch (err) {
			toast.error('Failed to load veterinarian');
			console.error('Error:', err);
		} finally {
			loading = false;
		}
	}

	async function handleDeleteVet() {
		deleting = true;
		try {
			await deleteVet(vetId);
			toast.success('Veterinarian deleted successfully');
			goto('/vets');
		} catch (err) {
			toast.error('Failed to delete veterinarian');
			console.error('Error:', err);
		} finally {
			deleting = false;
			deleteDialogOpen = false;
		}
	}

	// Load vet on mount and when ID changes
	$effect(() => {
		if (vetId) {
			loadVet();
		}
	});
</script>

<svelte:head>
	<title>{vet ? `Dr. ${vet.firstName} ${vet.lastName}` : 'Veterinarian'} | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Back Button -->
	<div class="mb-6">
		<Button variant="ghost" href="/vets" class="gap-2">
			<ArrowLeft class="h-4 w-4" />
			Back to Veterinarians
		</Button>
	</div>

	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading veterinarian...</p>
		</div>
	{:else if !vet}
		<div class="card p-12 text-center">
			<Stethoscope class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			<p class="text-muted-foreground">Veterinarian not found</p>
		</div>
	{:else}
		<!-- Vet Info Card -->
		<Card.Root class="mb-8">
			<Card.Header>
				<div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
					<div class="flex items-center gap-4">
						<div class="flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
							<Stethoscope class="h-8 w-8 text-success" />
						</div>
						<div>
							<Card.Title class="text-2xl">Dr. {vet.firstName} {vet.lastName}</Card.Title>
							<Card.Description>Veterinarian</Card.Description>
						</div>
					</div>
					<div class="flex gap-2">
						<Button variant="outline" href="/vets/{vet.id}/edit" class="gap-2">
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
				<div class="space-y-4">
					<div>
						<h3 class="text-sm font-medium text-muted-foreground mb-2">Specialties</h3>
						{#if vet.specialties && vet.specialties.length > 0}
							<div class="flex flex-wrap gap-2">
								{#each vet.specialties as specialty (specialty.id)}
									<Badge variant="secondary" class="text-sm">
										{specialty.name}
									</Badge>
								{/each}
							</div>
						{:else}
							<Badge variant="secondary" class="text-sm">
								General Practice
							</Badge>
						{/if}
					</div>
				</div>
			</Card.Content>
		</Card.Root>
	{/if}
</div>

<!-- Delete Confirmation Dialog -->
<Dialog.Root bind:open={deleteDialogOpen}>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Delete Veterinarian</Dialog.Title>
			<Dialog.Description>
				Are you sure you want to delete Dr. {vet?.firstName} {vet?.lastName}? This action cannot be undone.
			</Dialog.Description>
		</Dialog.Header>
		<Dialog.Footer>
			<Button variant="outline" onclick={() => (deleteDialogOpen = false)} disabled={deleting}>
				Cancel
			</Button>
			<Button variant="destructive" onclick={handleDeleteVet} disabled={deleting}>
				{#if deleting}
					Deleting...
				{:else}
					Delete
				{/if}
			</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
