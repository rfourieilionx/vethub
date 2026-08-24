/**
 * Theme state
 *
 * Tracks the active theme, applies it to <html data-theme="...">, and persists
 * it to localStorage. The initial value is read from the DOM attribute that
 * the inline bootstrap script in app.html already set before hydration, so
 * there is no flash-of-wrong-theme and no duplicate localStorage read.
 */
import { browser } from '$app/environment';
import { Sun, Moon, Sparkles } from 'lucide-svelte';

export const STORAGE_KEY = 'vethub:theme';

export type ThemeId = 'light' | 'dark' | 'fancy';

const VALID_THEME_IDS: readonly ThemeId[] = ['light', 'dark', 'fancy'];

export function isValidThemeId(value: unknown): value is ThemeId {
	return typeof value === 'string' && (VALID_THEME_IDS as readonly string[]).includes(value);
}

export interface ThemeDefinition {
	id: ThemeId;
	label: string;
	icon: typeof Sun;
	/** [background, primary, accent] swatch colors shown in the selector */
	swatch: [string, string, string];
}

export const THEMES: ThemeDefinition[] = [
	{
		id: 'light',
		label: 'Light',
		icon: Sun,
		swatch: ['hsl(0 0% 100%)', 'hsl(344 100% 45%)', 'hsl(175 70% 40%)']
	},
	{
		id: 'dark',
		label: 'Dark',
		icon: Moon,
		swatch: ['hsl(0 0% 9%)', 'hsl(344 90% 46%)', 'hsl(175 60% 30%)']
	},
	{
		id: 'fancy',
		label: 'Pawsome',
		icon: Sparkles,
		swatch: ['hsl(320 100% 97%)', 'hsl(330 85% 46%)', 'hsl(190 85% 30%)']
	}
];

function readInitialTheme(): ThemeId {
	if (!browser) return 'light';
	try {
		const attr = document.documentElement.dataset.theme;
		if (isValidThemeId(attr)) return attr;
	} catch {
		// fall through to default
	}
	return 'light';
}

function createThemeState() {
	let current = $state<ThemeId>(readInitialTheme());

	function set(id: ThemeId) {
		current = id;
		if (!browser) return;
		try {
			document.documentElement.dataset.theme = id;
			localStorage.setItem(STORAGE_KEY, id);
		} catch {
			// localStorage unavailable (private browsing, disabled) - theme still
			// applies to the DOM for the rest of this session, per AC8
		}
	}

	return {
		get current() {
			return current;
		},
		set
	};
}

export const themeState = createThemeState();
