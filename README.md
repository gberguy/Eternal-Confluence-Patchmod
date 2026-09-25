# Eternal Confluence Patchmod

Configurable fixes for Minecraft 1.12.2, plus compatibility that makes mobs from selected mods respect Corail Tombstone's Ghostly Shape effect.

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

Corail Tombstone and the mod being patched must be installed for the corresponding compatibility fix to apply.

## Configuration

On first launch, the mod creates `config/eternalconfluencepatchmod.cfg`. Each setting can be set to `true` or `false` independently. Changes take effect after restarting Minecraft.

## Requirements

- Minecraft 1.12.2
- Forge for Minecraft 1.12.2

The target mods are optional; the patchmod does not require every listed mod to be installed.

## Installation

Place the JAR in the instance's `mods` folder.

## License

MIT; see [LICENSE](LICENSE).

## AI use disclosure

AI tools assisted with analyzing target-mod behavior and writing portions of this mod's source code and documentation. The project owner directed the work and tested the release in-game.
