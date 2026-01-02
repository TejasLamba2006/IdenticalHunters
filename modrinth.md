# IdenticalHunters

For manhunt games. Makes all hunters look exactly the same so the speedrunner can't tell them apart.

## The problem

In a normal manhunt, the speedrunner can see who's chasing them. "That's Steve coming from the left, Alex from behind." They can track individual hunters and plan around them.

## The solution

This plugin makes every hunter look identical. Same skin, same armor, same name. The speedrunner just sees a group of identical threats.

## Three ways to do it

**Stealth Mode** - No extra plugins needed. Hunters become invisible with matching armor and player heads. Works out of the box.

**Disguise Mode** - Uses LibsDisguises for proper skin swapping. Hunters literally look like the same player.

**Auto Mode** - Uses Disguise mode if LibsDisguises is installed, Stealth mode if not.

## Setup

1. Set a speedrunner: `/ih runner set PlayerName`
2. Add hunters: `/ih hunter add PlayerName`
3. Start: `/ih start`

Hunters get a compass that tracks the runner. They'll all look the same. The runner won't know who's who.

## Works well for

- Regular manhunt games
- Recorded/streamed content
- Competitive events
- Any scenario where hunter identity matters

## Config options

- Disguise mode selection
- Armor color and type
- Player head texture
- Compass settings
- Custom messages

## Support

https://discord.gg/7fQPG4Grwt

Works with Minecraft 1.21+ on Paper or Spigot.
