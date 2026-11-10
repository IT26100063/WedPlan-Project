/*
 * Tailwind (Play CDN) configuration shared by every WedPlan page.
 * Load it right AFTER the CDN script:
 *   <script src="https://cdn.tailwindcss.com"></script>
 *   <script src="../js/tailwind.config.js"></script>
 *
 * Colours point at the CSS variables in css/theme.css, so the palette
 * is defined in one place and utilities like bg-forest/90 still work.
 */
tailwind.config = {
  theme: {
    extend: {
      colors: {
        ivory:  'rgb(var(--c-ivory) / <alpha-value>)',
        paper:  'rgb(var(--c-paper) / <alpha-value>)',
        sand:   'rgb(var(--c-sand) / <alpha-value>)',
        line:   'rgb(var(--c-line) / <alpha-value>)',
        forest: {
          DEFAULT: 'rgb(var(--c-forest) / <alpha-value>)',
          700:     'rgb(var(--c-forest-700) / <alpha-value>)',
          900:     'rgb(var(--c-forest-900) / <alpha-value>)',
        },
        brass: {
          DEFAULT: 'rgb(var(--c-brass) / <alpha-value>)',
          light:   'rgb(var(--c-brass-light) / <alpha-value>)',
        },
        ink:   'rgb(var(--c-ink) / <alpha-value>)',
        muted: 'rgb(var(--c-muted) / <alpha-value>)',
        clay:  'rgb(var(--c-clay) / <alpha-value>)',
        sage:  'rgb(var(--c-sage) / <alpha-value>)',
        amber: 'rgb(var(--c-amber) / <alpha-value>)',
      },
      fontFamily: {
        display: ['"Cormorant Garamond"', 'Georgia', 'serif'],
        sans:    ['Manrope', 'system-ui', 'sans-serif'],
      },
      maxWidth: {
        site: '76rem',
      },
    },
  },
};
