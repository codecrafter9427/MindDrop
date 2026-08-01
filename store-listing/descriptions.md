# MindDrop — Play Store listing copy

## Short description (max 80 characters)

**Primary:**
```
Capture thoughts fast. MindDrop resurfaces them when they matter.
```

**Alternatives:**
```
Offline notes that resurface themselves, with real alarms and reminders.
```
```
Private, offline notes and tasks that bring themselves back to you.
```

---

## Full description (max 4000 characters)

```
MindDrop is a fast, private place to put whatever is on your mind — and it decides when to bring it back to you.

Most notes apps are write-only. You capture something useful, it sinks down the list, and you never see it again. MindDrop scores every note on how urgent it looks, how long it has been since you read it, and how often you come back to it, then quietly resurfaces the ones that deserve your attention.


CAPTURE IN SECONDS

Open, type, save. No folders to pick, no fields to fill in. Set a type and priority if you want them, or skip them entirely.


IT FILES ITSELF

MindDrop reads what you wrote and sorts it into a task, idea, reminder, reference or general note. Words like "todo", "remind me" or "what if", or a pasted link, are enough for it to work out what kind of note you just made. Chose a type yourself? Your choice is always kept.


REMINDERS AND ALARMS

Give any note a date and time. Pick a quiet notification, or a proper alarm that rings on your alarm volume even when your phone is silenced. Repeat it daily or weekly when it is part of your routine. Tap the notification and you land straight on the note it came from.


RESURFACING THAT RESPECTS YOU

A background pass scores your notes on urgent wording, how stale they have gone, and how often you open them. The ones that rise to the top come back to you. Nothing is shouted at you, and nothing is sold to you.


FIND ANYTHING FAST

Search as you type, and narrow by type or priority. Sort newest first, oldest first, or by priority — your preference sticks between sessions.


BUILT TO STAY OUT OF THE WAY

• Works completely offline. There is no server.
• No account, no sign-in, no email address required.
• No ads, no tracking, no analytics.
• Your notes never leave your device.
• Material 3 design, with a full dark theme.
• Swipe to delete, edit anything in a tap.


WHY IT EXISTS

MindDrop is built around one idea: the value of a note is not in writing it, it is in seeing it again at the right moment. Everything in the app serves that, and nothing else.

Free, with no in-app purchases.
```

---

## Notes before you publish

- **Privacy policy is mandatory**, even for an app that collects nothing. A one-page
  "MindDrop stores all notes locally on your device and transmits no data" is enough.
  It needs a public URL — a GitHub Pages page on the MindDrop repo works.
- **Data safety form:** answer "no data collected" and "no data shared". That is accurate
  and is genuinely a selling point.
- **Permissions to justify:** `SCHEDULE_EXACT_ALARM` (reminders fire at the minute the
  user chose), `POST_NOTIFICATIONS` (delivering them), `RECEIVE_BOOT_COMPLETED`
  (re-arming reminders after a restart). All three follow directly from the reminder
  feature and are straightforward to explain if asked.
- **Deliberately not claimed:** the listing never says "AI" or "machine learning".
  The `:ml` module has TensorFlow Lite wired in, but no trained model ships with the
  app — the keyword classifier does all the real categorisation. Claiming ML in a
  store listing would be false advertising.
