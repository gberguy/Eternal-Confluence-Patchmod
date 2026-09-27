package com.gberguy.ecpatches.core;

public enum PatchId {
    BLOOD_ARSENAL("bloodArsenalSlateItem", "bloodarsenal", "Blood Arsenal", "Fixes compressed slate variants having incorrect names, variant handling, and crafting recipe behavior."),
    ROOTS("rootsAirStateMatcher", "roots", "Roots", "Fixes Wildwood disappearing instead of converting correctly to Runed Wildwood."),
    LYCANITES_MELEE("lycanitesMeleeLineOfSight", "lycanitesmobs", "Lycanites Mobs", "Prevents Lycanites mobs from performing melee attacks through walls and other solid obstacles."),
    MORECHIDS("morechidsClassGeneration", "morechids", "MoreChids", "Fixes a startup crash when MoreChids generates custom Orechid classes."),
    WAYSTONES_VILLAGE("waystonesVillageWeight", "waystones", "Waystones", "Set waystonesVillageWeight to false to disable this tweak."),
    LYCANITES_GHOST("lycanitesGhostlyShape", "lycanitesmobs", "Lycanites Mobs", ""),
    WIZARDRY_GHOST("ebWizardryGhostlyShape", "ebwizardry", "Electroblob's Wizardry", ""),
    ANCIENT_GHOST("ancientSpellcraftGhostlyShape", "ancientspellcraft", "Ancient Spellcraft", ""),
    TORO_GHOST("toroQuestGhostlyShape", "toroquest", "ToroQuest", ""),
    WITCHERY_VILLAGES_TORO_GUARDS("witcheryVillagesToroGuards", "witcherywalls", "Witchery Villages + ToroQuest", "Replaces Witchery Villages guards with ToroQuest guards affiliated with the local ToroQuest Province.");

    public final String key;
    public final String modId;
    public final String modName;
    public final String description;

    public boolean ghostly() {
        return this == LYCANITES_GHOST || this == WIZARDRY_GHOST || this == ANCIENT_GHOST || this == TORO_GHOST;
    }

    PatchId(String key, String modId, String modName, String description) {
        this.key = key;
        this.modId = modId;
        this.modName = modName;
        this.description = description;
    }
}
