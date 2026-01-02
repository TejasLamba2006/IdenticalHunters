# Changelog

All notable changes to IdenticalHunters will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-XX

### Added

- Initial release of IdenticalHunters
- **Dual Disguise Modes:**
  - Disguise Mode using LibsDisguises for full skin changes
  - Stealth Mode using invisibility + matching armor (no dependencies)
  - Auto Mode for automatic method selection
- **Compass Tracking System:**
  - Tracking compass for hunters
  - Auto-updates to point at speedrunner
  - Distance display in action bar
  - Configurable update interval
- **Game Management:**
  - Start/stop hunt commands
  - Add/remove hunters
  - Set/remove target (speedrunner)
  - List all participants
- **Stealth Mode Features:**
  - Configurable armor material (leather, chainmail, iron, diamond, netherite, gold)
  - Custom leather armor colors (RGB or color names)
  - Player head with configured skin
  - Auto-refreshing invisibility
- **Configuration Options:**
  - Customizable disguise skin (any Minecraft player)
  - Custom hunter display names
  - Toggle various features
  - Full message customization
- **Events:**
  - Auto-apply disguise on join (if game active)
  - Auto-reapply disguise on respawn
  - Optional disguise removal on death
- **Commands:**
  - `/identicalhunters` (aliases: `/ih`, `/hunters`, `/manhunt`)
  - `/hunter` for quick hunter management
- **Permissions:**
  - `identicalhunters.admin` - Full access
  - `identicalhunters.bypass` - Bypass being disguised
- Updated for Minecraft 1.21+
- MIT License
