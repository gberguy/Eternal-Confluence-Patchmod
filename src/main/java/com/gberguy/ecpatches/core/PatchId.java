package com.gberguy.ecpatches.core;

public enum PatchId {
    BLOOD_ARSENAL("bloodArsenalSlateItem", "bloodarsenal", "Blood Arsenal", "Uses BlockSlate's existing subtype-aware item instead of a generic ItemBlock. Tested with 1.12.2-2.2.2-31."),
    ROOTS("rootsAirStateMatcher", "roots", "Roots", "Rejects an air block returned by the block registry when resolving a state matcher. Tested with 1.12.2-3.1.9.2."),
    LYCANITES_MELEE("lycanitesMeleeLineOfSight", "lycanitesmobs", "Lycanites Mobs", "Requires the melee target to be visible before a melee attack can proceed. Tested with 1.12.2-2.0.8.10."),
    LYCANITES_GHOST("lycanitesGhostlyShape", "lycanitesmobs", "Lycanites Mobs / Corail Tombstone", "Prevents targeting players with tombstone:ghostly_shape. Does nothing special when that potion is absent. Tested with Lycanites Mobs 1.12.2-2.0.8.10."),
    MORECHIDS("morechidsClassGeneration", "morechids", "MoreChids", "Fixes invalid ASM descriptors when generating custom Orechid classes by using internal-name-aware type construction. Tested with 1.3.0.");

    public final String key;
    public final String modId;
    public final String modName;
    public final String description;

    PatchId(String key, String modId, String modName, String description) {
        this.key = key;
        this.modId = modId;
        this.modName = modName;
        this.description = description;
    }
}
