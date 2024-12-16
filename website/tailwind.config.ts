import type { Config } from 'tailwindcss';

export default {
	content: ['./src/**/*.{html,js,svelte,ts}'],

	theme: {
		extend: {
			colors: {
				'ide-bg': '#2B2D30',
				'ide-border': '#1E1F22',
				'ide-text': '#DFE1E5'
			}
		}
	},

	plugins: []
} satisfies Config;
