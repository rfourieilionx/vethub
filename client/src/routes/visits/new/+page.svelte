<script lang="ts">
	import { goto } from '$app/navigation';
	import { createVisit } from '$lib/api/visit/VisitController';
	import { getPets } from '$lib/api/pet/PetController';
	import { getVets } from '$lib/api/vet/VetController';
	import type { PetResponse, VetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Textarea } from '$lib/components/ui/textarea';
	import * as Select from '$lib/components/ui/select';
	import { ArrowLeft, Loader2 } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let pets = $state<PetResponse[]>([]);
	let vets = $state<VetResponse[]>([]);
	let loadingOptions = $state(true);

	let visitDate = $state(new Date().toISOString().split('T')[0]);
	let description = $state('');
	let selectedPetId = $state<number | undefined>(undefined);
	let selectedVetId = $state<number | undefined>(undefined);
	let submitting = $state(false);

	let selectedPet = $derived(pets.find((p) => p.id === selectedPetId));
	let selectedVet = $derived(vets.find((v) => v.id === selectedVetId));

	async function loadOptions() {
		loadingOptions = true;
		try {
			[pets, vets] = await Promise.all([getPets(), getVets()]);
		} catch (err) {
			toast.error('Failed to load pets and vets');
			console.error('Error loading options:', err);
		} finally {
			loadingOptions = false;
		}
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		if (!selectedPetId || !selectedVetId) {
			return;
		}
		submitting = true;

		try {
			await createVisit({
				date: visitDate,
				description: description.trim(),
				petId: selectedPetId,
				vetId: selectedVetId
			});
			toast.success('Visit scheduled successfully');
			goto('/visits');
		} catch (err) {
			toast.error('Failed to schedule visit');
			console.error('Error:', err);
		} finally {
			submitting = false;
		}
	}

	$effect(() => {
		loadOptions();
	});
</script>

<svelte:head>
	<title>Schedule Visit | VetHub</title>
</svelte:head>

<div class="container mx-auto max-w-2xl px-4 py-8">
	<!-- Back button -->
	<Button variant="ghost" href="/visits" class="mb-6 gap-2">
		<ArrowLeft class="h-4 w-4" />
		Back to Visits
	</Button>

	<div class="mb-6">
		<h1 class="text-2xl font-bold">Schedule Visit</h1>
		<p class="text-muted-foreground">Record a new visit for any pet</p>
	</div>

	<div class="card p-6">
		<form onsubmit={handleSubmit} class="space-y-6">
			<div class="space-y-2">
				<Label for="pet">Pet</Label>
				<Select.Root
					type="single"
					value={selectedPetId?.toString()}
					onValueChange={(value) => (selectedPetId = value ? Number(value) : undefined)}
				>
					<Select.Trigger id="pet" class="w-full" disabled={loadingOptions || submitting}>
						{selectedPet
							? `${selectedPet.name} (${selectedPet.ownerFirstName} ${selectedPet.ownerLastName})`
							: 'Select a pet'}
					</Select.Trigger>
					<Select.Content>
						{#each pets as pet (pet.id)}
							<Select.Item value={pet.id.toString()}>
								{pet.name} ({pet.ownerFirstName} {pet.ownerLastName})
							</Select.Item>
						{/each}
					</Select.Content>
				</Select.Root>
			</div>

			<div class="space-y-2">
				<Label for="visitDate">Visit Date</Label>
				<Input
					id="visitDate"
					type="date"
					bind:value={visitDate}
					required
					disabled={submitting}
				/>
			</div>

			<div class="space-y-2">
				<Label for="vet">Seen by</Label>
				<Select.Root
					type="single"
					value={selectedVetId?.toString()}
					onValueChange={(value) => (selectedVetId = value ? Number(value) : undefined)}
				>
					<Select.Trigger id="vet" class="w-full" disabled={loadingOptions || submitting}>
						{selectedVet ? `Dr. ${selectedVet.firstName} ${selectedVet.lastName}` : 'Select the attending vet'}
					</Select.Trigger>
					<Select.Content>
						{#each vets as vet (vet.id)}
							<Select.Item value={vet.id.toString()}>
								Dr. {vet.firstName} {vet.lastName}
							</Select.Item>
						{/each}
					</Select.Content>
				</Select.Root>
			</div>

			<div class="space-y-2">
				<Label for="description">Description</Label>
				<Textarea
					id="description"
					bind:value={description}
					placeholder="Describe the reason for the visit (e.g., Annual checkup, Vaccination, etc.)"
					rows={4}
					required
					disabled={submitting}
				/>
			</div>

			<div class="flex justify-end gap-3">
				<Button type="button" variant="outline" href="/visits" disabled={submitting}>
					Cancel
				</Button>
				<Button type="submit" disabled={submitting || !selectedPetId || !selectedVetId}>
					{#if submitting}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
					{/if}
					Schedule Visit
				</Button>
			</div>
		</form>
	</div>
</div>
