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

TARGET = (1080, 1920)          # exactly 9:16
PAD = (18, 19, 24)             # NeutralDark #121318 from ui/theme/Color.kt

OUT = os.path.dirname(os.path.abspath(__file__))


def frame(src, dst):
    im = Image.open(src).convert("RGB")
    scale = min(TARGET[0] / im.width, TARGET[1] / im.height)
    new = (max(1, round(im.width * scale)), max(1, round(im.height * scale)))
    im = im.resize(new, Image.LANCZOS)

    canvas = Image.new("RGB", TARGET, PAD)
    canvas.paste(im, ((TARGET[0] - new[0]) // 2, (TARGET[1] - new[1]) // 2))
    canvas.save(dst, "PNG", optimize=True)
    return dst


if __name__ == "__main__":
    pairs = [
        (sys.argv[1] + "/shot1.png", "phone-1-note-list.png"),
        (sys.argv[1] + "/shot3.png", "phone-2-reminder.png"),
        (sys.argv[1] + "/shot5.png", "phone-3-search-filter.png"),
        (sys.argv[1] + "/shot4.png", "phone-4-settings.png"),
    ]
    for src, name in pairs:
        if not os.path.exists(src):
            print(f"missing: {src}")
            continue
        dst = frame(src, os.path.join(OUT, name))
        im = Image.open(dst)
        w, h = im.size
        from math import gcd
        g = gcd(w, h)
        print(f"{name:30} {w}x{h}  ratio {w//g}:{h//g}  "
              f"{os.path.getsize(dst)/1024:.0f} KB")
