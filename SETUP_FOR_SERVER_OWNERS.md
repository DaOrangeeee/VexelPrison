# VexelPrisonCore — No Coding Setup Guide

This guide is for server owners who do **not** code.

## 1) Download the plugin jar from GitHub Actions
1. Open your repository on GitHub.
2. Click the **Actions** tab.
3. Open the latest successful **Build VexelPrisonCore** workflow run.
4. Download artifact: **VexelPrisonCore-jar**.
5. Extract it and take the `.jar` file.

## 2) Install on your server
1. Stop your Minecraft server.
2. Upload the jar into your server `plugins/` folder.
3. Start the server once.

## 3) Basic configuration
Edit `plugins/VexelPrisonCore/config.yml`:
- Keep `database.type: SQLITE` unless you use MySQL.
- Set your mine region under `mines.starter` (`world`, `x1..z2`).

## 4) Reload and verify
In console or as OP:
- `/vexelcore reload`
- `/vexelcore status`

You are now ready. Players can use:
- `/pickaxe`
- `/prestige`
- `/rebirth`
- `/crates`
- `/help`
