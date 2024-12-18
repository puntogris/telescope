import type { Config } from 'tailwindcss';
import forms from '@tailwindcss/forms';

export default {
	content: ['./src/**/*.{html,js,svelte,ts}'],

	theme: {
		extend: {
			colors: {
				'ide-bg': '#2B2D30',
				'ide-border-dark': '#1E1F22',
				'ide-text': '#DFE1E5',
				'ide-bg-dark': '#1E1F22',
				'ide-border-light': '#393B41'
			}
		}
	},

	plugins: [forms]
} satisfies Config;
