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
| `phone-4-settings.png` | 1080×1920 (9:16), ≤8 MB | 143 KB ✔ |
| 7″ / 10″ tablet screenshots | — | Not produced |

Play requires 2–8 phone screenshots; four are supplied.

## How these were made

- **Icon and feature graphic** — `generate_assets.py`, drawing the same drop
  silhouette and indigo gradient as the in-app launcher icon, so the listing
  matches what users see on their home screen. The drop's straight edges are the
  true tangent lines from the tip to the bulb, computed rather than eyeballed.
- **Screenshots** — genuine `adb screencap` captures of the running app, not
  mockups. Demo notes were seeded directly into the app database so the listing
  shows realistic content rather than test data.
- **Framing** — `frame_screenshots.py`. The device is 1080×2408 (≈9:20), taller
  than the 9:16 Play asks for, so each capture is scaled to fit and padded with
  the app's own background colour (`#121318`). Nothing is cropped; cropping to
  9:16 would have cut off the FAB and the last note.

Both scripts are re-runnable, so assets can be regenerated after UI changes.

## Known issues to address before publishing

1. **Tablet screenshots not supplied.** Optional to publish, but Play may mark
   the app as not optimised for large screens.
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
