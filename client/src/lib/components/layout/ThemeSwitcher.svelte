<script lang="ts">
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { buttonVariants } from '$lib/components/ui/button';
	import { Palette } from 'lucide-svelte';
	import { THEMES, themeState, isValidThemeId } from '$lib/theme/theme.svelte';
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger
		class={buttonVariants({ variant: 'ghost', size: 'icon' })}
		aria-label="Select theme"
	>
		<Palette class="h-5 w-5" />
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end" class="w-56">
		<DropdownMenu.Label>Theme</DropdownMenu.Label>
		<DropdownMenu.Separator />
		<DropdownMenu.RadioGroup
			value={themeState.current}
			onValueChange={(value) => {
				if (isValidThemeId(value)) themeState.set(value);
			}}
		>
			{#each THEMES as theme (theme.id)}
				<DropdownMenu.RadioItem value={theme.id}>
					<theme.icon class="h-4 w-4" />
					<span class="flex-1">{theme.label}</span>
					<span class="flex gap-0.5">
						{#each theme.swatch as color (color)}
							<span
								class="h-2.5 w-2.5 rounded-full border border-black/10"
								style="background-color: {color}"
							></span>
						{/each}
					</span>
				</DropdownMenu.RadioItem>
			{/each}
		</DropdownMenu.RadioGroup>
	</DropdownMenu.Content>
</DropdownMenu.Root>
