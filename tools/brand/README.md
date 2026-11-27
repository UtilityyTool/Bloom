# Brand tools

Scripts that draw the Bloom logo from geometry, so it can be regenerated or tweaked exactly.

- `geo.py`: shapes (circles, petals), merging and cutting them, exporting clean SVG paths
- `marks2.py`: the logo designs, including **Bud**, the one Bloom uses
- `textpath.py`: turns text in a font into outlines (used for the wordmark), and freezes variable fonts at chosen settings
- `marks.py`: first-round sketches, kept for reference

Needs: `pip install fonttools brotli shapely cairosvg`, and the fonts from npm (`@fontsource-variable/google-sans-flex`).
