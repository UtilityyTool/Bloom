"""Logo mark drafts. Each returns SVG markup for a 100x100 box."""
import math

P = "#D0BCFF"      # primary (lavender)
PC = "#4F378B"     # primary container (deep purple)
ON = "#381E72"     # on-primary (darkest purple)
T = "#EFB8C8"      # tertiary (pink)
TC = "#633B48"     # tertiary container


def fmt(v):
    return f"{v:.2f}".rstrip("0").rstrip(".")


def polar_path(cx, cy, rfun, steps=360):
    pts = []
    for i in range(steps):
        a = 2 * math.pi * i / steps
        r = rfun(a)
        pts.append(f"{fmt(cx + r * math.cos(a))},{fmt(cy + r * math.sin(a))}")
    return "M" + " L".join(pts) + " Z"


def petal(cx, cy, length, width, angle_deg, base=0):
    """A rounded petal pointing outward from (cx,cy): circle-tipped lens shape."""
    a = math.radians(angle_deg)
    # tip circle centre and base point
    tx, ty = cx + (base + length - width / 2) * math.cos(a), cy + (base + length - width / 2) * math.sin(a)
    bx, by = cx + base * math.cos(a), cy + base * math.sin(a)
    r = width / 2
    # tangent points on the tip circle, perpendicular to the petal axis
    px, py = -math.sin(a), math.cos(a)
    l1 = (tx + px * r, ty + py * r)
    l2 = (tx - px * r, ty - py * r)
    # control points pull the sides out so the petal is plump
    k = (length - r) * 0.55
    c1 = (bx + math.cos(a) * k + px * r * 0.9, by + math.sin(a) * k + py * r * 0.9)
    c2 = (bx + math.cos(a) * k - px * r * 0.9, by + math.sin(a) * k - py * r * 0.9)
    return (f"M{fmt(bx)},{fmt(by)} Q{fmt(c1[0])},{fmt(c1[1])} {fmt(l1[0])},{fmt(l1[1])} "
            f"A{fmt(r)},{fmt(r)} 0 0 0 {fmt(l2[0])},{fmt(l2[1])} "
            f"Q{fmt(c2[0])},{fmt(c2[1])} {fmt(bx)},{fmt(by)} Z")


def mark_classic():
    """1. The current four-petal flower, refined: fuller petals, smaller centre."""
    s = ""
    for (x, y) in [(50, 27), (73, 50), (50, 73), (27, 50)]:
        s += f'<circle cx="{x}" cy="{y}" r="20" fill="{P}"/>'
    s += f'<circle cx="50" cy="50" r="11" fill="{ON}"/>'
    return s


def mark_twotone():
    """2. Six petals alternating lavender and pink, deep centre."""
    s = ""
    for i in range(6):
        s += f'<path d="{petal(50, 50, 42, 26, -90 + i * 60)}" fill="{P if i % 2 == 0 else T}"/>'
    s += f'<circle cx="50" cy="50" r="10" fill="{ON}"/>'
    return s


def mark_cookie():
    """3. Material 3 'cookie' shape as a flower: 9 soft scallops, a ring in the middle."""
    outer = polar_path(50, 50, lambda a: 38 + 3.2 * math.cos(9 * a))
    return (f'<path d="{outer}" fill="{P}"/>'
            f'<circle cx="50" cy="50" r="15" fill="{ON}"/>'
            f'<circle cx="50" cy="50" r="7" fill="{T}"/>')


def mark_spin():
    """4. Pinwheel: four petals that each tuck under the next, so it looks like it's turning open."""
    cs = []
    for i in range(4):
        a = math.radians(-90 + i * 90)
        cs.append((50 + 18 * math.cos(a) - 9 * math.sin(a), 50 + 18 * math.sin(a) + 9 * math.cos(a), P if i % 2 == 0 else T))
    s = "".join(f'<circle cx="{fmt(x)}" cy="{fmt(y)}" r="19" fill="{c}"/>' for x, y, c in cs)
    # redraw the first petal where it meets the last one, so every petal overlaps the next (a true swirl)
    x3, y3, _ = cs[3]
    x0, y0, c0 = cs[0]
    s += (f'<clipPath id="spinclip"><circle cx="{fmt(x3)}" cy="{fmt(y3)}" r="19"/></clipPath>'
          f'<circle cx="{fmt(x0)}" cy="{fmt(y0)}" r="19" fill="{c0}" clip-path="url(#spinclip)"/>')
    s += f'<circle cx="50" cy="50" r="9" fill="{ON}"/>'
    return s


def mark_bud():
    """5. An opening bud: three petals forming a cup, the middle one in pink."""
    left = petal(50, 84, 62, 34, -122)
    right = petal(50, 84, 62, 34, -58)
    mid = petal(50, 84, 70, 34, -90)
    return (f'<path d="{left}" fill="{P}"/><path d="{right}" fill="{P}"/>'
            f'<path d="{mid}" fill="{T}"/>')


MARKS = [
    ("classic", "Classic", "Your current flower, refined", mark_classic),
    ("twotone", "Two-tone bloom", "Five petals, lavender and pink", mark_twotone),
    ("cookie", "Cookie flower", "Built from Material 3's 'cookie' shape", mark_cookie),
    ("spin", "Pinwheel", "Petals swept like it\'s opening", mark_spin),
    ("bud", "Bud", "A flower just opening", mark_bud),
]


def tile(inner, bg="#2B2930", radius=24):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">'
            f'<rect width="100" height="100" rx="{radius}" fill="{bg}"/>{inner}</svg>')
