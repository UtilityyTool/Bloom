"""Clean vector geometry for the logo: build shapes, merge/cut them, export one SVG path."""
import math
from shapely.geometry import Point, Polygon
from shapely.ops import unary_union
from shapely import affinity

RES = 128  # segments per quarter circle: smooth at any size


def circle(x, y, r):
    return Point(x, y).buffer(r, quad_segs=RES)


def petal_shape(cx, cy, length, width, angle_deg, base=0.0):
    """Teardrop petal: a round tip joined to a point near the centre, with plump curved sides."""
    r = width / 2
    a = math.radians(angle_deg)
    ux, uy = math.cos(a), math.sin(a)
    px, py = -uy, ux
    bx, by = cx + base * ux, cy + base * uy
    tx, ty = cx + (base + length - r) * ux, cy + (base + length - r) * uy
    pts = []
    # left side: quadratic curve from base to the tip circle's left tangent
    k = (length - r) * 0.55
    c = (bx + ux * k + px * r * 0.95, by + uy * k + py * r * 0.95)
    l = (tx + px * r, ty + py * r)
    for i in range(0, 65):
        t = i / 64
        pts.append(((1 - t) ** 2 * bx + 2 * (1 - t) * t * c[0] + t * t * l[0],
                    (1 - t) ** 2 * by + 2 * (1 - t) * t * c[1] + t * t * l[1]))
    # tip: half circle
    start = math.atan2(py, px)
    for i in range(1, 129):
        ang = start - math.pi * i / 128
        pts.append((tx + r * math.cos(ang), ty + r * math.sin(ang)))
    # right side back to base
    c2 = (bx + ux * k - px * r * 0.95, by + uy * k - py * r * 0.95)
    rr = (tx - px * r, ty - py * r)
    for i in range(1, 65):
        t = i / 64
        pts.append(((1 - t) ** 2 * rr[0] + 2 * (1 - t) * t * c2[0] + t * t * bx,
                    (1 - t) ** 2 * rr[1] + 2 * (1 - t) * t * c2[1] + t * t * by))
    return Polygon(pts).buffer(0)


def polar(cx, cy, rfun, steps=1440):
    return Polygon([(cx + rfun(2 * math.pi * i / steps) * math.cos(2 * math.pi * i / steps),
                     cy + rfun(2 * math.pi * i / steps) * math.sin(2 * math.pi * i / steps)) for i in range(steps)])


def union(*shapes):
    return unary_union(list(shapes))


def to_path(geom, simplify=0.02):
    """SVG path data (use fill-rule="evenodd"); holes come out as real holes."""
    geom = geom.simplify(simplify, preserve_topology=True)
    polys = list(geom.geoms) if hasattr(geom, "geoms") else [geom]
    out = []
    for p in polys:
        for ring in [p.exterior, *p.interiors]:
            cs = list(ring.coords)
            out.append("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in cs[:-1]) + "Z")
    return "".join(out)
