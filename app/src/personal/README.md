# The `personal` flavor source set

Agent Hina has two product flavors (`app/build.gradle.kts`, dimension `distribution`), same
`applicationId` for both:

| Flavor | Who | What it does |
|---|---|---|
| `personal` | Fihad only | Seeds his own instruction pack into the Linux environment on first setup (`HinaInstructionsSeeder`) and adds a short standing-rules entry to every agent through `GlobalContext`. |
| `shareable` | Friends, anyone | Seeds nothing. The public repo builds this by default. |

## Where the personal files live

The personal flavor reads two assets, both in `app/src/personal/assets/`:

- `hina-default-instructions.md` — the full instruction pack, seeded to `~/.claude/CLAUDE.md` and `~/.config/opencode/AGENTS.md` (never overwriting an existing file).
- `hina-global-context.md` — the short (~30-line) standing-rules entry every agent gets, including `agy`.

**Neither file is in this repo, and neither may ever be committed here.** `app/src/personal/assets/`
is in `.gitignore` on purpose: the pack is Fihad's personal working rules, and this repo is public.
The files live only in the private repo (`fxfihad16/AgentHina-personal`), which is this repo plus
one commit adding the assets. The seeding code no-ops cleanly when an asset is absent, so a
personal build from the public checkout simply seeds nothing.

## Building

- Public repo / CI: `./scripts/build_all_apks.sh --release` (shareable, the default).
- Personal APK (private repo only): `./scripts/build_all_apks.sh --release --personal`.
