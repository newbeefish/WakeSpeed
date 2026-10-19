# CurseForge Listing — English Pack

All fields needed to publish on CurseForge, in English.

---

## 1. Project Name

```
WakeSpeed
```

## 2. Summary (one line, ≤150 chars)

```
Turn a vanilla boat into a race car: rally LED tachometer, EV power meter, gasoline & electric engine audio, and a proper racing camera.
```

Shorter alternative (≤100 chars):

```
Turn vanilla boats into race cars: rally tach, EV meter, engine audio and a racing camera.
```

## 3. Project Slug

```
wakespeed
```

## 4. Categories

| Field | Value |
|---|---|
| **Game** | Minecraft |
| **Section** | Mods |
| **Category** | Addons / Cosmetic |
| **Sub-category (if asked)** | Map and Information |

Suggested if multi-select is allowed:

```
Addons, Cosmetic, Map and Information, Utility & QoL
```

## 5. Tags / Search terms

```
hud, speedometer, tachometer, engine sound, racing, boat, drift, camera, minimap, fabric
```

---

## 6. Description (paste into the editor)

```markdown
# WakeSpeed

**Turn a vanilla Minecraft boat into a race car.**

Three things in one jar: gauges, engine audio, and camera.

Supports **Fabric 26.1.2 and 26.2**.

---

## Gauges

Three dashboards you can switch between at any time:

- **Bar** — rally-style segmented LED strip, green → yellow → red, with a white turbo bar underneath
- **Dial** — classic needle gauge; a small disc above the hub fills like a pie slice to show turbo pressure
- **EV Meter** — square panel showing power (kW), current (A), voltage (120 V) and motor RPM

Gasoline mode has a rev-limiter jitter at the top of the range; EV mode is PID-stable and barely moves. Turbo readout is hidden automatically in EV mode.

## Audio

**Gasoline:** engine, turbo whistle, blow-off valve, clutch and gear-shift sounds — each with its **own volume slider (0–200%)**.

**Electric:** a real motor recording. Full spool in 0.23 s, silent within 0.1 s of lifting off, completely quiet at standstill. No idle, no regen whine, no gear shifts.

One toggle switches between the two drivetrains.

## Racing Camera

The camera follows the boat's heading with smooth interpolation, so fast corners no longer throw your view around. On by default, can be turned off, smoothness is adjustable.

## Extras

- **Minimap**, **drift timer** with burnout detection, **lap timer**
- **Ice recolour**: ice → orange-yellow, packed ice → light grey, blue ice → dark grey. Three states: always on / always off / auto-on-boat (turns on when you board, restores when you step off)
- Every HUD element can be toggled and positioned by percentage
- Unified settings screen on **F9** (also **F10**), bilingual labels

---

## Installation

1. Install **Fabric Loader** and **Fabric API**
2. Install **Cloth Config API** (required)
3. Drop the jar for your Minecraft version into `.minecraft/mods/`
4. Launch the game and press **F9**

**Required dependencies:** Fabric API, Cloth Config API
**Optional:** ModMenu (only for showing a config button in the mod list)

---

## Recommended Resource Pack

For the best look, pair with [Boat Skyline R34 GTR](https://modrinth.com/resourcepack/r34-gtr) — it replaces the boat with a Skyline R34 GTR.

- Requires **Entity Model Features (EMF)** and **Entity Texture Features (ETF)**
- Made by **6GHz**. All rights reserved — please download it from the original page and **do not redistribute it**

Other packs by the same author: [AE86](https://modrinth.com/resourcepack/ae86), [F40](https://modrinth.com/resourcepack/f40), [Cybertruck](https://modrinth.com/resourcepack/cybertruck)

---

## License & Credits

**GPL-3.0** — full source code: https://github.com/newbeefish/WakeSpeed

Based on:
- **BoatHud (NFS)** by jewtvet (MIT) — HUD foundation
- **BoatCam** by Bstn1802 and Pigalala (GPL-3.0) — racing camera

Audio: public domain and CC-BY 3.0 assets, full attribution in `CREDITS.md`.
Icons and ice textures are original, generated programmatically. No Minecraft assets are redistributed.
```

---

## 7. Links

| Field | Value |
|---|---|
| **Source** | `https://github.com/newbeefish/WakeSpeed` |
| **Issues** | `https://github.com/newbeefish/WakeSpeed/issues` |
| **Wiki / Docs** | `https://github.com/newbeefish/WakeSpeed#readme` |
| **Discord** | *(leave empty unless you create one)* |

## 8. License

```
GNU General Public License v3.0
```

## 9. Relations (Dependencies)

| Type | Project | Reason |
|---|---|---|
| **Required** | Fabric API | Networking and lifecycle events |
| **Required** | Cloth Config API | Settings screen |
| **Optional** | ModMenu | Config button in the mod list |
| **Optional** | Entity Model Features | Needed for CEM car resource packs |
| **Optional** | Entity Texture Features | Needed for CEM car resource packs |

## 10. File Upload Metadata

| Field | 26.1.2 | 26.2 |
|---|---|---|
| **File** | `WakeSpeed-1.0.0-26.1.2.jar` | `WakeSpeed-1.0.0-26.2.jar` |
| **Display name** | `WakeSpeed 1.0.0 (Fabric 26.1.2)` | `WakeSpeed 1.0.0 (Fabric 26.2)` |
| **Release type** | Release | Release |
| **Game version** | 26.1.2 | 26.2 |
| **Mod loader** | Fabric | Fabric |
| **Java version** | Java 25 | Java 25 |

## 11. Changelog (English)

```markdown
## 1.0.0 — First stable release

### Gauges
- Bar, dial and EV meter styles
- Turbo indicator: strip under the bar, pie-slice disc in the dial
- Turbo hidden automatically in EV mode
- Rev-limiter jitter (gas), PID-stable (EV), idle flutter

### Audio
- Gas: engine, turbo, blow-off, clutch, gear shift, each with independent volume
- EV: real motor recording, no idle, no regen, no shifting
- One toggle for drivetrain mode

### Racing camera
- BoatCam integrated, follows heading with smooth interpolation, toggleable

### Ice style
- Ice / packed ice / blue ice recoloured as solid high-contrast blocks
- Three states: on / off / auto-on-boat

### Extras
- Minimap, drift timer with burnout detection, lap timer
- Unified F9 settings screen, bilingual
```

## 12. Avatar

Upload `icon-400.png` (400×400 PNG, 64.8 KB).

---

## Notes

- CurseForge may not list `26.1.2` / `26.2` in its version dropdown yet. If the option is missing, pick the closest available and mention the tested versions in the summary — or wait until CurseForge adds them.
- Do **not** use "Need for Speed", "NFS" or "Skyline" in the project name or summary; those are third-party trademarks.
