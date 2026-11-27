"""Turn text into SVG path outlines, one path per glyph, so letters can be edited as shapes."""
import sys
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.boundsPen import BoundsPen

FONTS = "/home/claude/brand-work/node_modules/@fontsource"


def font_path(family, weight):
    return f"{FONTS}/{family}/files/{family}-latin-{weight}-normal.woff2"


def glyphs(family, weight, text, size, tracking=0.0):
    """Returns (list of dicts with d/x/advance/bounds/char, total width, ascent of caps)."""
    f = TTFont(font_path(family, weight))
    gs = f.getGlyphSet()
    cmap = f.getBestCmap()
    upm = f["head"].unitsPerEm
    s = size / upm
    out, x = [], 0.0
    for ch in text:
        name = cmap[ord(ch)]
        pen = SVGPathPen(gs)
        g = gs[name]
        g.draw(TransformPen(pen, (s, 0, 0, -s, x, 0)))
        bp = BoundsPen(gs)
        g.draw(bp)
        b = bp.bounds or (0, 0, 0, 0)
        out.append({"char": ch, "d": pen.getCommands(), "x": x, "adv": g.width * s,
                    "bounds": (x + b[0] * s, -b[3] * s, x + b[2] * s, -b[1] * s)})
        x += g.width * s + tracking * size
    cap = f["OS/2"].sCapHeight * s if hasattr(f["OS/2"], "sCapHeight") else size * 0.7
    xh = f["OS/2"].sxHeight * s if hasattr(f["OS/2"], "sxHeight") else size * 0.5
    return out, x - tracking * size, cap, xh


if __name__ == "__main__":
    gl, w, cap, xh = glyphs(sys.argv[1], sys.argv[2], sys.argv[3], 100)
    print(w, cap, xh, [g["char"] for g in gl])


def instance(family_file, loc, out):
    """Freeze a variable font at chosen settings (e.g. weight + roundness) into a normal font file."""
    from fontTools.varLib.instancer import instantiateVariableFont
    f = TTFont(family_file)
    inst = instantiateVariableFont(f, loc)
    inst.flavor = None
    inst.save(out)
    return out


def glyphs_file(path, text, size, tracking=0.0):
    """Same as glyphs(), but from any font file path."""
    f = TTFont(path)
    gs = f.getGlyphSet(); cmap = f.getBestCmap(); upm = f["head"].unitsPerEm; s = size / upm
    out, x = [], 0.0
    for ch in text:
        g = gs[cmap[ord(ch)]]
        pen = SVGPathPen(gs); g.draw(TransformPen(pen, (s, 0, 0, -s, x, 0)))
        bp = BoundsPen(gs); g.draw(bp); b = bp.bounds or (0, 0, 0, 0)
        out.append({"char": ch, "d": pen.getCommands(), "x": x, "adv": g.width * s,
                    "bounds": (x + b[0] * s, -b[3] * s, x + b[2] * s, -b[1] * s)})
        x += g.width * s + tracking * size
    return out, x - tracking * size
