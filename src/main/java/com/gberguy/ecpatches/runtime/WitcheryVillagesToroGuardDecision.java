package com.gberguy.ecpatches.runtime;

enum WitcheryVillagesToroGuardDecision {
    LEAVE,
    REPLACE,
    SUPPRESS;

    static WitcheryVillagesToroGuardDecision forSpawn(boolean enabled, boolean provinceExists, boolean hasLord) {
        if (!enabled || !provinceExists) return LEAVE;
        return hasLord ? REPLACE : SUPPRESS;
    }

    boolean cancelsAfterSpawn(boolean replacementSpawned) {
        return this == SUPPRESS || this == REPLACE && replacementSpawned;
    }
}
