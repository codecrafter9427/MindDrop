"""Frames raw device screenshots to Play's 9:16 phone requirement.

The device is 1080x2408 (roughly 9:20), taller than the 9:16 Play asks for.
Scaling to fit and padding the sides preserves the whole screen -- cropping to
9:16 would cut the FAB and the last note off the list. The pad colour is the
app's own dark background, so the added strips read as part of the UI rather
than as letterboxing.
"""

from PIL import Image
import os
import sys

PAD = (18, 19, 24)             # NeutralDark #121318 from ui/theme/Color.kt

# Every target is an exact 9:16 multiple, and each is at least as large as the
# capture it holds, so screenshots are padded rather than upscaled -- resampling
# would visibly soften the text.
PHONE = (1080, 1920)           # holds 1080x2408 scaled to fit
TABLET_7 = (1350, 2400)        # holds a native 1200x1920 capture
TABLET_10 = (1620, 2880)       # holds a native 1600x2560 capture

OUT = os.path.dirname(os.path.abspath(__file__))


def frame(src, dst, target):
    im = Image.open(src).convert("RGB")

    # Only ever scale down. A capture that already fits is placed at native size.
    scale = min(target[0] / im.width, target[1] / im.height, 1.0)
    if scale < 1.0:
        im = im.resize(
            (max(1, round(im.width * scale)), max(1, round(im.height * scale))),
            Image.LANCZOS,
        )

    canvas = Image.new("RGB", target, PAD)
    canvas.paste(im, ((target[0] - im.width) // 2, (target[1] - im.height) // 2))
    canvas.save(dst, "PNG", optimize=True)
    return dst


if __name__ == "__main__":
    src_dir = sys.argv[1]
    jobs = [
        ("shot1.png", "phone-1-note-list.png", PHONE),
        ("shot3.png", "phone-2-reminder.png", PHONE),
        ("shot5.png", "phone-3-search-filter.png", PHONE),
        ("shot4.png", "phone-4-settings.png", PHONE),
        ("t7-1.png", "tablet7-1-note-list.png", TABLET_7),
        ("t7-2.png", "tablet7-2-reminder.png", TABLET_7),
        ("t7-3.png", "tablet7-3-settings.png", TABLET_7),
        ("t10-1.png", "tablet10-1-note-list.png", TABLET_10),
        ("t10-2.png", "tablet10-2-reminder.png", TABLET_10),
        ("t10-3.png", "tablet10-3-settings.png", TABLET_10),
    ]

    from math import gcd
    for src_name, out_name, target in jobs:
        src = os.path.join(src_dir, src_name)
        if not os.path.exists(src):
            print(f"missing (skipped): {src_name}")
            continue
        dst = frame(src, os.path.join(OUT, out_name), target)
        w, h = Image.open(dst).size
        g = gcd(w, h)
        print(f"{out_name:30} {w}x{h}  ratio {w//g}:{h//g}  "
              f"{os.path.getsize(dst)/1024:.0f} KB")
