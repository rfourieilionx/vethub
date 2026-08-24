<script lang="ts">
	import { Stethoscope, Users, Calendar, PawPrint } from 'lucide-svelte';
</script>

<svelte:head>
	<title>VetHub - Home</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Hero Section with X Mark background -->
	<section
		class="relative mb-12 text-center animate-fade-in overflow-hidden rounded-xl bg-gradient-to-br from-[var(--hero-gradient-from)] to-[var(--hero-gradient-to)] py-12 px-6"
	>
		<!-- Decorative X Mark -->
		<div class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/4 opacity-[0.03] pointer-events-none">
			<img src="/x-mark-red.png" alt="" class="h-[400px] w-auto" aria-hidden="true" />
		</div>
		
		<div class="relative z-10">
			<div class="flex justify-center mb-6">
				<div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10">
					<PawPrint class="h-8 w-8 text-primary" />
				</div>
			</div>
			<h1 class="text-3xl font-extralight text-foreground mb-4">
				Welcome to <span class="font-medium text-primary">Vet</span><span class="font-light">Hub</span>
			</h1>
			<p class="text-lg font-light text-muted-foreground max-w-2xl mx-auto">
				A modern veterinary clinic management system. Manage pet owners, their pets, 
				veterinarians, and appointments — all in one place.
			</p>
		</div>
	</section>

	<!-- Quick Links Grid -->
	<section class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
		<a
			href="/owners"
			class="card-hover p-6 flex flex-col items-center text-center animate-fade-in-up stagger-1"
		>
			<div class="flex h-14 w-14 items-center justify-center rounded-full bg-primary/10 mb-4">
				<Users class="h-7 w-7 text-primary" />
			</div>
			<h2 class="font-medium text-foreground mb-2">Owners</h2>
			<p class="text-sm font-light text-muted-foreground">
				Manage pet owner information and their contact details
			</p>
		</a>

		<a
			href="/pets"
			class="card-hover p-6 flex flex-col items-center text-center animate-fade-in-up stagger-2"
		>
			<div class="flex h-14 w-14 items-center justify-center rounded-full bg-accent/10 mb-4">
				<PawPrint class="h-7 w-7 text-accent" />
			</div>
			<h2 class="font-medium text-foreground mb-2">Pets</h2>
			<p class="text-sm font-light text-muted-foreground">
				View and manage pets, their types, and medical history
			</p>
		</a>

		<a
			href="/vets"
			class="card-hover p-6 flex flex-col items-center text-center animate-fade-in-up stagger-3"
		>
			<div class="flex h-14 w-14 items-center justify-center rounded-full bg-success/10 mb-4">
				<Stethoscope class="h-7 w-7 text-success" />
			</div>
			<h2 class="font-medium text-foreground mb-2">Veterinarians</h2>
			<p class="text-sm font-light text-muted-foreground">
				Browse our team of veterinary specialists
			</p>
		</a>

		<a
			href="/visits"
			class="card-hover p-6 flex flex-col items-center text-center animate-fade-in-up stagger-4"
		>
			<div class="flex h-14 w-14 items-center justify-center rounded-full bg-warning/10 mb-4">
				<Calendar class="h-7 w-7 text-warning" />
			</div>
			<h2 class="font-medium text-foreground mb-2">Visits</h2>
			<p class="text-sm font-light text-muted-foreground">
				Schedule and manage veterinary appointments
			</p>
		</a>
	</section>
</div>
