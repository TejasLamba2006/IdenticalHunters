# IdenticalHunters

Make all hunters look the same in manhunt games so the speedrunner can't tell them apart.

## About

In manhunt games, players hunting the speedrunner all look different which makes it easy to track who's where. This plugin makes all hunters appear identical - same skin, same armor, same name tag. The speedrunner won't know which hunter is which.

## How It Works

The plugin has three modes:

**AUTO Mode** - Uses LibsDisguises if installed, otherwise falls back to Stealth Mode

**DISGUISE Mode** - Requires LibsDisguises. Hunters get the same player skin and name.

**STEALTH Mode** - No dependencies needed. Hunters get:

- Invisibility effect
- Matching colored leather armor
- Player heads of a configurable skin
- Same display name

## Features

- Three disguise modes to choose from
- Compass tracking for hunters
- Configurable hunter armor color
- Custom player head skins
- Works with or without LibsDisguises
- Full permission control

## Commands

| Command | Description |
|---------|-------------|
| `/ih start` | Start a manhunt session |
| `/ih stop` | End the session |
| `/ih hunter add <player>` | Add a hunter |
| `/ih hunter remove <player>` | Remove a hunter |
| `/ih runner set <player>` | Set the speedrunner |
| `/ih compass` | Give compass to hunter |
| `/ih mode <mode>` | Change disguise mode |
| `/ih reload` | Reload config |

## Quick Start

1. Install the plugin
2. Use `/ih runner set <player>` to set the speedrunner
3. Use `/ih hunter add <player>` for each hunter
4. Use `/ih start` to begin

Hunters will be disguised and can track the runner with their compass.

## Configuration

- Choose disguise mode (AUTO/DISGUISE/STEALTH)
- Set armor color for stealth mode
- Configure player head skin
- Customize compass tracking behavior
- Set team colors and names

## Support

Discord: <https://discord.gg/7fQPG4Grwt>

## Requirements

- Minecraft 1.21+
- Paper or Spigot
- Java 21+
- LibsDisguises (optional, for DISGUISE mode)

## License

MIT License
