# VexelPrisonCore

Production-ready modular OP Prison core plugin for Purpur/Paper 1.21.11.

## Features
- Auto-bound prison pickaxe in hotbar slot 1 with strict movement/drop/container protections.
- Pickaxe XP + leveling to 500 with configurable formula.
- 15 scalable upgrade tracks (1-500).
- Rebirth system (up to config cap, default 50) with permanent bonuses.
- Prestige system with support for 1-1000 progression and milestone rewards.
- Virtual key crate system (no physical keys) including monthly key grant.
- Persistent boosters (XP/Sell/Key find).
- Custom GUI help override for `/help` and `/?`.
- Async SQL persistence with SQLite default and MySQL optional.
- Soft dependency auto-detection + health check status output.

## Build
```bash
./gradlew shadowJar
```

Output jar: `build/libs/VexelPrisonCore-1.0.0.jar`

## No-code install (recommended)
If you do not code, use GitHub Actions artifacts:
1. Open **Actions** in this repo.
2. Open the latest successful **Build VexelPrisonCore** run.
3. Download **VexelPrisonCore-jar** artifact.
4. Upload the jar to your server `plugins/` folder and restart.

Detailed guide: `SETUP_FOR_SERVER_OWNERS.md`

## Setup
1. Place jar in `/plugins`.
2. Start server once to generate configs.
3. Configure `config.yml` database + mine regions.
4. (Optional) Install Vault/LuckPerms/PlaceholderAPI/WorldGuard/ItemsAdder.
5. Run `/vexelcore reload`.

## Commands
### Player
- `/pickaxe`
- `/prestige`
- `/rebirth`
- `/crates`
- `/boosters`
- `/help` and `/?` (custom GUI)

### Admin
- `/vexelcore givekey <player> <crate> <amount>`
- `/vexelcore setprestige <player> <value>`
- `/vexelcore setrebirth <player> <value>`
- `/vexelcore setpxlvl <player> <value>`
- `/vexelcore reload`
- `/vexelcore status`


## Gameplay status (implemented)
- Upgrade purchases now consume Pickaxe XP wallet and enforce affordability checks.
- Prestige uses a paginated GUI with +1 / +/-page / +/-10 controls and reward claiming per level.
- Rebirth uses a confirmation GUI with reset preview and bonus preview.
- Crates use weighted config rewards with delayed opening (spin timing) and virtual key consumption.
- Help GUI is category-based and config-driven from `help-gui` config section.

## Notes
- Plugin runs with zero soft-dependencies installed.
- Modules are dependency aware and can degrade gracefully.
- Autosave runs every 5 minutes and player data is saved on quit/shutdown.
