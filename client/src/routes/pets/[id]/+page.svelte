<script lang="ts">
	import { page } from '$app/stores';
	import { getPetById } from '$lib/api/pet/PetController';
	import type { PetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import * as Card from '$lib/components/ui/card';
	import { PawPrint, Calendar, ArrowLeft, Stethoscope } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let pet = $state<PetResponse | null>(null);
	let loading = $state(true);

	const petId = $derived(Number($page.params.id));

	async function loadPet() {
		loading = true;
		try {
			pet = await getPetById(petId);
		} catch (err) {
			toast.error('Failed to load pet');
			console.error('Error:', err);
		} finally {
			loading = false;
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

	// Load pet on mount and when the ID changes
	$effect(() => {
		if (petId) {
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
		<Button variant="ghost" href="/pets" class="gap-2">
			<ArrowLeft class="h-4 w-4" />
			Back to Pets
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
				<div class="flex items-center gap-4">
					<div class="flex h-16 w-16 items-center justify-center rounded-full bg-accent/10">
						<PawPrint class="h-8 w-8 text-accent" />
					</div>
					<div>
						<Card.Title class="text-2xl">{pet.name}</Card.Title>
						<div class="mt-1 flex items-center gap-2">
							<Badge variant="secondary">{pet.type?.name ?? 'Unknown type'}</Badge>
							<span class="text-muted-foreground">•</span>
							<span class="text-muted-foreground">{calculateAge(pet.birthDate)}</span>
						</div>
					</div>
				</div>
			</Card.Header>
			<Card.Content class="space-y-3">
				<div class="flex items-center gap-3 text-muted-foreground">
					<Calendar class="h-5 w-5" />
					<span>Born: {formatDate(pet.birthDate)}</span>
				</div>
				<div class="flex items-center gap-3 text-muted-foreground">
					<PawPrint class="h-5 w-5" />
					<span>
						Owner:
						<a href="/owners/{pet.ownerId}" class="font-medium text-primary hover:underline">
							{pet.ownerFirstName} {pet.ownerLastName}
						</a>
					</span>
				</div>
			</Card.Content>
		</Card.Root>

		<!-- Visits Section -->
		<div class="mb-6">
			<h2 class="text-xl font-semibold text-foreground">Visit History</h2>
		</div>

		{#if !pet.visits?.length}
			<div class="card p-8 text-center">
				<Stethoscope class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
				<p class="text-muted-foreground">No visits recorded for this pet</p>
			</div>
		{:else}
			<div class="space-y-4">
				{#each pet.visits.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()) as visit (visit.id)}
					<Card.Root>
						<Card.Content class="pt-6">
							<div class="flex items-start gap-4">
								<div class="flex h-10 w-10 items-center justify-center rounded-full bg-success/10">
									<Stethoscope class="h-5 w-5 text-success" />
								</div>
								<div>
									<p class="font-medium text-foreground">{visit.description}</p>
									<p class="text-sm text-muted-foreground">{formatDate(visit.date)}</p>
								</div>
							</div>
						</Card.Content>
					</Card.Root>
				{/each}
			</div>
		{/if}
	{/if}
</div>
