# IdenticalHunters

Make all hunters look **identical** for epic Minecraft manhunt games! Inspired by Dream's manhunt videos, this plugin disguises all hunters to look the same, making it harder for the speedrunner to identify who's who.

## ✨ Features

### 🎭 Dual Disguise Modes
- **Disguise Mode** - Full player skin disguise using LibsDisguises (optional)
- **Stealth Mode** - Fallback mode using invisibility + matching armor (no dependencies!)
- **Auto Mode** - Automatically picks the best method available

### 🧭 Built-in Compass Tracking
- Hunters get a tracking compass that points to the speedrunner
- Auto-updates location every second
- Shows distance to target in action bar
- Configurable compass name and lore

### 🎮 Easy Game Management
- Simple commands to add/remove hunters
- Set target (speedrunner) with one command
- Start/stop the hunt instantly
- Disguises apply/remove automatically

### ⚙️ Highly Configurable
- Choose disguise skin (any Minecraft player name)
- Customize hunter display names
- Configure matching armor material and color
- Toggle invisibility, player heads, and more
- Fully customizable messages

## 📥 Installation

1. Download `IdenticalHunters.jar`
2. Place in your `plugins` folder
3. Restart your server
4. (Optional) Install [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/) for full disguise mode

## 🎯 Quick Start

```
1. Set the target (speedrunner):     /ih settarget <player>
2. Add hunters:                       /ih sethunter <player>
3. Start the hunt:                    /ih start
4. When done:                         /ih stop
```

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ih start` | Start the hunt - apply all disguises | `identicalhunters.admin` |
| `/ih stop` | Stop the hunt - remove disguises | `identicalhunters.admin` |
| `/ih sethunter <player>` | Add a player as hunter | `identicalhunters.admin` |
| `/ih removehunter <player>` | Remove a hunter | `identicalhunters.admin` |
| `/ih settarget <player>` | Set the speedrunner | `identicalhunters.admin` |
| `/ih removetarget` | Remove current target | `identicalhunters.admin` |
| `/ih list` | List all hunters and target | `identicalhunters.admin` |
| `/ih compass [player]` | Give tracking compass | `identicalhunters.admin` |
| `/ih info` | Show plugin information | `identicalhunters.admin` |
| `/ih mode` | Show current disguise mode | `identicalhunters.admin` |
| `/ih reload` | Reload configuration | `identicalhunters.admin` |
| `/hunter add <player>` | Quick add hunter | `identicalhunters.admin` |
| `/hunter remove <player>` | Quick remove hunter | `identicalhunters.admin` |

**Aliases:** `/ih`, `/hunters`, `/manhunt`

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `identicalhunters.admin` | Access to all commands | OP |
| `identicalhunters.bypass` | Won't be disguised as hunter | false |

## ⚙️ Configuration

### Disguise Mode Settings
```yaml
# Mode: AUTO, DISGUISE, or STEALTH
mode: AUTO

# Skin to use for all hunters (any valid Minecraft username)
disguise-skin: "Dream"

# Display name for hunters
disguise-name: "&cHunter"
```

### Stealth Mode Settings (Fallback)
```yaml
stealth:
  apply-invisibility: true
  invisibility-duration: 60
  matching-armor:
    enabled: true
    material: LEATHER  # LEATHER, CHAINMAIL, IRON, DIAMOND, NETHERITE, GOLD
    leather-color: "255,0,0"  # Red color
    armor-name: "&cHunter Armor"
  player-head:
    enabled: true
    force-equip: true
```

### Compass Settings
```yaml
compass:
  enabled: true
  give-compass: true
  track-target: true
  update-interval: 20  # ticks
  compass-name: "&6Tracking Compass"
```

### Game Settings
```yaml
game:
  auto-apply-on-start: true
  auto-remove-on-end: true
  remove-on-death: false
  reapply-on-respawn: true
  min-hunters: 1
  broadcast-events: true
```

## 🔌 Dependencies

**Required:**
- Paper/Spigot 1.21+ (Paper recommended)
- Java 21

**Optional (for full disguise mode):**
- [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/)
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (required by LibsDisguises)

## 🎭 How Disguise Modes Work

### Disguise Mode (LibsDisguises)
When LibsDisguises is installed, hunters will:
- Have their skin completely changed to match the configured skin
- Show the same name above their head
- Appear identical to other players

### Stealth Mode (No Dependencies)
Without LibsDisguises, hunters will:
- Become invisible (with particle effects hidden)
- Wear matching colored leather armor
- Have a player head of the configured skin
- Still be trackable with the compass

## 🎮 Game Flow Example

1. **Setup:** Admin sets up the manhunt
   - `/ih settarget Dream` - Dream is the speedrunner
   - `/ih sethunter George` - George is a hunter
   - `/ih sethunter Sapnap` - Sapnap is a hunter
   - `/ih sethunter BadBoyHalo` - BadBoyHalo is a hunter

2. **Start:** Admin starts the hunt
   - `/ih start` - All hunters are now disguised!
   - Hunters receive tracking compasses

3. **During Hunt:**
   - Hunters use compass to track Dream
   - All hunters look identical, confusing Dream
   - If a hunter dies, their disguise is reapplied on respawn

4. **End:** When Dream wins or loses
   - `/ih stop` - All disguises removed

## 🤝 Support

- **Discord:** [Join our Discord](https://discord.gg/7fQPG4Grwt)
- **Issues:** Report bugs on GitHub

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ by TejasLamba2006
