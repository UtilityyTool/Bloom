"""Logo marks, round 2: one simple shape, one smooth gradient, no background plate.
Shapes are built with real geometry (geo.py), so every mark is a single clean outline."""
import math
from geo import circle, petal_shape, polar, union, to_path

# Brighter versions of the app's purple and pink, blended like Google's gradient G.
STOPS = [(0, "#6E56F8"), (0.5, "#B48CFF"), (1, "#FF8DBF")]


def grad(pid, x1=12, y1=88, x2=88, y2=12):
    stops = "".join(f'<stop offset="{o}" stop-color="{c}"/>' for o, c in STOPS)
    return (f'<linearGradient id="{pid}g" gradientUnits="userSpaceOnUse" '
            f'x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}">{stops}</linearGradient>')


def _bloom_geom():
    petals = union(*[circle(x, y, 21.5) for x, y in [(50, 28), (72, 50), (50, 72), (28, 50)]], circle(50, 50, 22))
    return petals.difference(circle(50, 50, 8.5))


def _petals_geom():
    return union(*[petal_shape(50, 50, 40, 22, -90 + i * 60, base=8) for i in range(6)])


def _cookie_geom():
    return polar(50, 50, lambda a: 40 + 3.4 * math.cos(9 * a)).difference(circle(50, 50, 12))


def _bud_geom():
    mid = petal_shape(50, 86, 72, 32, -90)
    sides = union(petal_shape(50, 86, 60, 32, -124), petal_shape(50, 86, 60, 32, -56))
    # cut a clean gap around the middle petal, like Google's newer icons separate their parts
    return union(mid, sides.difference(mid.buffer(3.2, quad_segs=64)))


GEOMS = {"bloom": _bloom_geom, "petals": _petals_geom, "cookie": _cookie_geom, "bud": _bud_geom}
PATHS = {k: to_path(f()) for k, f in GEOMS.items()}
GRAD_DIR = {"bud": (50, 90, 50, 12)}


def mark(key, pid, color=None):
    """SVG for a 100x100 box. color=None uses the gradient; a hex color makes a one-color version."""
    d = PATHS[key]
    if color:
        return f'<path d="{d}" fill="{color}" fill-rule="evenodd"/>'
    return f'<defs>{grad(pid, *GRAD_DIR.get(key, (12, 88, 88, 12)))}</defs><path d="{d}" fill="url(#{pid}g)" fill-rule="evenodd"/>'


MARKS2 = [
    ("bloom", "Bloom", "Four petals fused into one shape. The closest to what you have now."),
    ("petals", "Petals", "Six slim petals with space between them. Lighter and more detailed."),
    ("cookie", "Cookie", "Material 3's own 'cookie' shape. Ties Bloom straight to the design language."),
    ("bud", "Bud", "A flower opening upward. The most illustrative of the four."),
]
