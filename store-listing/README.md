# Play Store listing assets

Generated 2026-08-02. Copy for the listing lives in [descriptions.md](descriptions.md).

## Assets

| File | Spec | Status |
|---|---|---|
| `icon-512.png` | 512×512 PNG, ≤1 MB | 14 KB ✔ |
| `feature-graphic-1024x500.png` | 1024×500 PNG, ≤15 MB | 56 KB ✔ |
| `phone-1-note-list.png` | 1080×1920 (9:16), ≤8 MB | 254 KB ✔ |
| `phone-2-reminder.png` | 1080×1920 (9:16), ≤8 MB | 223 KB ✔ |
| `phone-3-search-filter.png` | 1080×1920 (9:16), ≤8 MB | 152 KB ✔ |
| `phone-4-settings.png` | 1080×1920 (9:16), ≤8 MB | 148 KB ✔ |
| `tablet7-1-note-list.png` | 1350×2400 (9:16), ≤8 MB | 110 KB ✔ |
| `tablet7-2-reminder.png` | 1350×2400 (9:16), ≤8 MB | 98 KB ✔ |
| `tablet7-3-settings.png` | 1350×2400 (9:16), ≤8 MB | 70 KB ✔ |
| `tablet10-1-note-list.png` | 1620×2880 (9:16), ≤8 MB | 117 KB ✔ |
| `tablet10-2-reminder.png` | 1620×2880 (9:16), ≤8 MB | 105 KB ✔ |
| `tablet10-3-settings.png` | 1620×2880 (9:16), ≤8 MB | 76 KB ✔ |

Play requires 2–8 screenshots per form factor; four phone and three of each
tablet size are supplied. Tablet sides satisfy the stricter 10-inch rule too
(each side between 1,080 px and 7,680 px).

## How these were made

- **Icon and feature graphic** — `generate_assets.py`, drawing the same drop
  silhouette and indigo gradient as the in-app launcher icon, so the listing
  matches what users see on their home screen. The drop's straight edges are the
  true tangent lines from the tip to the bulb, computed rather than eyeballed.
- **Screenshots** — genuine `adb screencap` captures of the running app, not
  mockups. Demo notes were seeded directly into the app database so the listing
  shows realistic content rather than test data.
- **Tablet screenshots** — no tablet hardware was available, so the phone was
  temporarily driven at tablet geometry with `adb shell wm size` / `wm density`
  (7″: 1200×1920 @ 320dpi → 600×960dp; 10″: 1600×2560 @ 320dpi → 800×1280dp),
  captured, then reset to its original 1080×2408 @ 450dpi. These are the real
  app laying itself out at tablet width — not upscaled phone screenshots.
- **Framing** — `frame_screenshots.py`. Every target is an exact 9:16 multiple
  sized to hold its capture, so tablet shots are padded at native resolution
  rather than upscaled. Phone captures (1080×2408, ≈9:20) are scaled to fit.
  Padding uses the app's own background colour (`#121318`); cropping to 9:16
  would have cut off the FAB and the last note.

Both scripts are re-runnable, so assets can be regenerated after UI changes.

## Known issues to address before publishing

1. **The app has no tablet-specific layout.** The tablet screenshots are honest
   but show a single-column list stretched across a wide screen, with noticeable
   empty space below the fold. Nothing is broken, and the chip rows do reflow
   (all five type chips fit one row at 10″, unlike on phone), but a reviewer
   will see a phone layout on a tablet. A two-pane list/detail layout at
   sw600dp would be the real fix.
2. **Privacy policy URL is mandatory** and does not exist yet. A one-page
   "all notes stay on your device, nothing is transmitted" served from GitHub
   Pages satisfies this.
3. **Release build has never been run.** R8 minification was enabled on Day 16
   but only the debug build has been exercised on a device. Install and smoke-test
   a signed release build before uploading — R8 problems typically only appear at
   runtime.
4. **Deliberately not claimed anywhere:** "AI" or "machine learning". TensorFlow
   Lite is wired into `:ml` but no trained model ships; the keyword classifier
   does the real categorisation. Claiming ML would be false advertising.
