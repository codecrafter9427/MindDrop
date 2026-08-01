"""Generates MindDrop's Play Store icon and feature graphic.

Reproduces the app's own launcher icon rather than inventing new artwork: the
same drop silhouette (tip at the top, rounding out to a circle) and the same
indigo gradient used in res/drawable/ic_launcher_background.xml, so the store
listing matches what users see on their home screen.
"""

from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# Brand colours, matching ui/theme/Color.kt and the adaptive icon background.
INDIGO_LIGHT = (90, 107, 192)   # #5A6BC0
INDIGO_DARK = (59, 74, 148)     # #3B4A94
WHITE = (255, 255, 255)


def linear_gradient(size, top_left, bottom_right):
    """Diagonal gradient, matching the launcher icon's 0,0 -> 108,108 sweep."""
    w, h = size
    base = Image.new("RGB", size)
    px = base.load()
    for y in range(h):
        for x in range(w):
            # Normalised distance along the diagonal.
            t = (x / max(w - 1, 1) + y / max(h - 1, 1)) / 2
            px[x, y] = tuple(
                int(top_left[i] + (bottom_right[i] - top_left[i]) * t) for i in range(3)
            )
    return base


def drop_path(cx, cy_tip, radius, tip_factor=2.35):
    """Points describing a teardrop: a point at the top easing into a circle.

    The straight edges are the true tangent lines from the tip to the circle,
    computed rather than approximated, so the silhouette meets the bulb smoothly
    instead of cutting a notch into it. The circle centre sits `tip_factor *
    radius` below the tip.
    """
    import math

    cy_centre = cy_tip + radius * tip_factor
    d = cy_centre - cy_tip

    # Angle between the centre->tip axis and centre->tangent-point.
    theta = math.acos(radius / d)

    # Tangent points, measured in image coordinates (y grows downward).
    a1 = math.atan2(-radius * math.cos(theta), radius * math.sin(theta))
    a2 = math.atan2(-radius * math.cos(theta), -radius * math.sin(theta))

    pts = [(cx, cy_tip)]

    # Sweep from the right tangent point, around the bottom, to the left one.
    sweep_end = a2 + 2 * math.pi
    steps = 240
    for i in range(steps + 1):
        a = a1 + (sweep_end - a1) * (i / steps)
        pts.append((cx + radius * math.cos(a), cy_centre + radius * math.sin(a)))

    return pts


def rounded_mask(size, radius):
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius, fill=255)
    return mask


def make_icon(px=512):
    """512x512 store icon. Full-bleed square: Play applies its own masking."""
    scale = 4  # supersample, then downscale for clean edges
    s = px * scale
    img = linear_gradient((s, s), INDIGO_LIGHT, INDIGO_DARK).convert("RGBA")

    layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)

    cx = s / 2
    radius = s * 0.165
    cy_tip = s * 0.255
    d.polygon(drop_path(cx, cy_tip, radius), fill=WHITE + (255,))

    img = Image.alpha_composite(img, layer)
    img = img.resize((px, px), Image.LANCZOS).convert("RGB")
    path = os.path.join(OUT, "icon-512.png")
    img.save(path, "PNG", optimize=True)
    return path


def load_font(names, size):
    for n in names:
        for d in (r"C:\Windows\Fonts",):
            p = os.path.join(d, n)
            if os.path.exists(p):
                try:
                    return ImageFont.truetype(p, size)
                except Exception:
                    pass
    return ImageFont.load_default()


def make_feature_graphic(w=1024, h=500):
    """1024x500 feature graphic.

    Play may overlay UI near the edges and can crop the sides on some surfaces,
    so the mark and wordmark stay well inside the middle band.
    """
    scale = 2
    W, H = w * scale, h * scale
    img = linear_gradient((W, H), INDIGO_LIGHT, INDIGO_DARK).convert("RGBA")

    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)

    # Drop mark on the left, vertically centred.
    radius = H * 0.145
    cx = W * 0.185
    cy_tip = H * 0.5 - radius * 1.68
    d.polygon(drop_path(cx, cy_tip, radius), fill=WHITE + (255,))

    img = Image.alpha_composite(img, layer)
    d = ImageDraw.Draw(img)

    title_font = load_font(["segoeuib.ttf", "arialbd.ttf"], int(H * 0.19))
    tag_font = load_font(["segoeui.ttf", "arial.ttf"], int(H * 0.083))

    tx = W * 0.36
    title = "MindDrop"
    tagline = "Notes that come back to you"

    tb = d.textbbox((0, 0), title, font=title_font)
    gb = d.textbbox((0, 0), tagline, font=tag_font)
    total = (tb[3] - tb[1]) + (gb[3] - gb[1]) + H * 0.06
    ty = (H - total) / 2

    d.text((tx, ty - tb[1]), title, font=title_font, fill=WHITE)
    d.text(
        (tx, ty + (tb[3] - tb[1]) + H * 0.06 - gb[1]),
        tagline,
        font=tag_font,
        fill=(226, 229, 255),
    )

    img = img.resize((w, h), Image.LANCZOS).convert("RGB")
    path = os.path.join(OUT, "feature-graphic-1024x500.png")
    img.save(path, "PNG", optimize=True)
    return path


if __name__ == "__main__":
    for p in (make_icon(), make_feature_graphic()):
        im = Image.open(p)
        print(f"{os.path.basename(p):38} {im.size[0]}x{im.size[1]}  "
              f"{os.path.getsize(p)/1024:.0f} KB  {im.mode}")
