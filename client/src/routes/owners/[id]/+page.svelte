<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { getOwnerById, deleteOwner as deleteOwnerApi } from '$lib/api/owner/OwnerController';
	import { getVisitsByOwner } from '$lib/api/visit/VisitController';
	import type { OwnerResponse, PetSummaryResponse, VisitResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import * as Card from '$lib/components/ui/card';
	import {
		User,
		Phone,
		MapPin,
		PawPrint,
		Calendar,
		Stethoscope,
		ArrowLeft,
		Pencil,
		Trash2,
		Plus
	} from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let owner = $state<OwnerResponse | null>(null);
	let visits = $state<VisitResponse[]>([]);
	let loading = $state(true);
	let visitsLoading = $state(true);
	let deleting = $state(false);

	const ownerId = $derived(Number($page.params.id));

	async function loadOwner() {
		loading = true;
		try {
			owner = await getOwnerById(ownerId);
		} catch (err) {
			toast.error('Failed to load owner');
			console.error('Error loading owner:', err);
		} finally {
			loading = false;
		}
	}

	async function loadVisits() {
		visitsLoading = true;
		try {
			visits = await getVisitsByOwner(ownerId);
		} catch (err) {
			toast.error('Failed to load visit history');
			console.error('Error loading visits:', err);
		} finally {
			visitsLoading = false;
		}
	}

	async function handleDeleteOwner() {
		if (!confirm('Are you sure you want to delete this owner? This will also delete all their pets.')) {
			return;
		}

		deleting = true;
		try {
			await deleteOwnerApi(ownerId);
			toast.success('Owner deleted successfully');
			goto('/owners');
		} catch (err) {
			toast.error('Failed to delete owner');
			console.error('Error deleting owner:', err);
		} finally {
			deleting = false;
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
		return years === 1 ? '1 year old' : `${years} years old`;
	}

	// Load owner and their visit history on mount
	$effect(() => {
		if (ownerId) {
			loadOwner();
			loadVisits();
		}
	});
</script>

<svelte:head>
	<title>{owner ? `${owner.firstName} ${owner.lastName}` : 'Owner'} | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Back button -->
	<Button variant="ghost" href="/owners" class="mb-6 gap-2">
		<ArrowLeft class="h-4 w-4" />
		Back to Owners
	</Button>

	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading owner details...</p>
		</div>
	{:else if !owner}
		<div class="card p-12 text-center">
			<User class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			<p class="text-muted-foreground">Owner not found</p>
		</div>
	{:else}
		<!-- Owner Info Card -->
		<div class="mb-8">
			<Card.Root>
				<Card.Header>
					<div class="flex items-start justify-between">
						<div class="flex items-center gap-4">
							<div class="flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
								<User class="h-8 w-8 text-primary" />
							</div>
							<div>
								<Card.Title class="text-2xl">{owner.firstName} {owner.lastName}</Card.Title>
								<Card.Description>Pet Owner</Card.Description>
							</div>
						</div>
						<div class="flex gap-2">
							<Button variant="outline" size="sm" href="/owners/{ownerId}/edit" class="gap-2">
								<Pencil class="h-4 w-4" />
								Edit
							</Button>
							<Button
								variant="destructive"
								size="sm"
								onclick={handleDeleteOwner}
								disabled={deleting}
								class="gap-2"
							>
								<Trash2 class="h-4 w-4" />
								{deleting ? 'Deleting...' : 'Delete'}
							</Button>
						</div>
					</div>
				</Card.Header>
				<Card.Content>
					<div class="grid gap-4 md:grid-cols-2">
						<div class="flex items-center gap-3 text-muted-foreground">
							<Phone class="h-5 w-5" />
							<span>{owner.telephone}</span>
						</div>
						<div class="flex items-center gap-3 text-muted-foreground">
							<MapPin class="h-5 w-5" />
							<span>{owner.address}, {owner.city}</span>
						</div>
					</div>
				</Card.Content>
			</Card.Root>
		</div>

		<!-- Pets Section -->
		<div class="mb-6 flex items-center justify-between">
			<h2 class="text-xl font-semibold text-foreground">Pets</h2>
			<Button href="/owners/{ownerId}/pets/new" class="gap-2">
				<Plus class="h-4 w-4" />
				Add Pet
			</Button>
		</div>

		{#if !owner.pets?.length}
			<div class="card p-8 text-center">
				<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
				<p class="text-muted-foreground">No pets registered for this owner</p>
				<Button href="/owners/{ownerId}/pets/new" class="mt-4 gap-2">
					<Plus class="h-4 w-4" />
					Add First Pet
				</Button>
			</div>
		{:else}
			<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
				{#each owner.pets as pet (pet.id)}
					<Card.Root class="hover:shadow-md transition-shadow">
						<Card.Header class="pb-2">
							<div class="flex items-center justify-between">
								<Card.Title class="flex items-center gap-2">
									<PawPrint class="h-5 w-5 text-accent" />
									{pet.name}
								</Card.Title>
								<Badge variant="secondary">{pet.typeName}</Badge>
							</div>
						</Card.Header>
						<Card.Content>
							<div class="space-y-2 text-sm text-muted-foreground">
								<div class="flex items-center gap-2">
									<Calendar class="h-4 w-4" />
									<span>Born: {formatDate(pet.birthDate)}</span>
								</div>
								<p class="text-foreground">{calculateAge(pet.birthDate)}</p>
							</div>
						</Card.Content>
						<Card.Footer class="pt-2">
							<Button variant="ghost" size="sm" href="/owners/{ownerId}/pets/{pet.id}" class="w-full">
								View Details
							</Button>
						</Card.Footer>
					</Card.Root>
				{/each}
			</div>
		{/if}

		<!-- Visit History Section -->
		<div class="mb-6 mt-10 flex items-center justify-between">
			<h2 class="text-xl font-semibold text-foreground">Visit History</h2>
			<span class="text-sm text-muted-foreground">across all of this owner's pets</span>
		</div>

		{#if visitsLoading}
			<div class="card p-8 text-center">
				<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
				<p class="text-muted-foreground">Loading visits...</p>
			</div>
		{:else if !visits.length}
			<div class="card p-8 text-center">
				<Stethoscope class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
				<p class="text-muted-foreground">No visits recorded yet</p>
			</div>
		{:else}
			<div class="space-y-4">
				{#each visits as visit (visit.id)}
					<Card.Root>
						<Card.Content class="pt-6">
							<div class="flex items-start justify-between gap-4">
								<div class="flex items-start gap-4">
									<div class="flex h-10 w-10 items-center justify-center rounded-full bg-success/10">
										<Stethoscope class="h-5 w-5 text-success" />
									</div>
									<div>
										<div class="flex flex-wrap items-center gap-2">
											<p class="font-medium text-foreground">{visit.description}</p>
											<a href="/owners/{ownerId}/pets/{visit.petId}">
												<Badge variant="secondary">{visit.petName}</Badge>
											</a>
										</div>
										<p class="text-sm text-muted-foreground">
											{#if visit.vetFirstName}
												Seen by Dr. {visit.vetFirstName} {visit.vetLastName}
											{:else}
												Seen by — not recorded
											{/if}
										</p>
									</div>
								</div>
								<span class="whitespace-nowrap text-sm text-muted-foreground">
									{formatDate(visit.date)}
								</span>
							</div>
						</Card.Content>
					</Card.Root>
				{/each}
			</div>
		{/if}
	{/if}
</div>
