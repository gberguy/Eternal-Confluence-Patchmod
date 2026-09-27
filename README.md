# Eternal Confluence Tweaks

Designed for the Eternal Confluence modpack, this mod provides configurable fixes for Minecraft 1.12.2 and makes mobs from selected mods respect Corail Tombstone's Ghostly Shape effect. It can also be used in any other Minecraft 1.12.2 Forge installation with the relevant target mods.

## General fixes

- **Blood Arsenal** (`bloodArsenalSlateItem`): Fixes compressed slate variants having incorrect names, variant handling, and crafting recipe behavior.
- **Roots** (`rootsAirStateMatcher`): Fixes Wildwood disappearing instead of converting correctly to Runed Wildwood.
- **Lycanites Mobs** (`lycanitesMeleeLineOfSight`): Prevents Lycanites mobs from performing melee attacks through walls and other solid obstacles.
- **MoreChids** (`morechidsClassGeneration`): Fixes a startup crash when MoreChids generates custom Orechid classes.

## Corail Tombstone: Ghostly Shape compatibility

Makes mobs from enabled mods ignore players affected by Ghostly Shape, including players they already targeted. Each integration has its own setting:

- **Lycanites Mobs** (`lycanitesGhostlyShape`)
- **Electroblob's Wizardry** (`ebWizardryGhostlyShape`)
- **Ancient Spellcraft** (`ancientSpellcraftGhostlyShape`)
- **ToroQuest** (`toroQuestGhostlyShape`)

Corail Tombstone and the mod being patched must be installed for the corresponding compatibility fix to have an effect. If the Ghostly Shape potion is unavailable, the guarded checks do nothing.

## Configuration

On first launch, the mod creates `config/eternalconfluencetweaks.cfg`. Each fix can be set to `true` or `false` independently. Changes take effect after restarting Minecraft.

## Requirements

- Minecraft 1.12.2
- Forge for Minecraft 1.12.2

The target mods are optional; the mod does not require every listed mod to be installed.

## Installation

Place the JAR in the instance's `mods` folder.

## License

MIT; see [LICENSE](LICENSE).

## AI use disclosure

AI tools assisted with analyzing target-mod behavior and writing portions of this mod's source code and documentation. The project owner directed the work and tested the release in-game.
