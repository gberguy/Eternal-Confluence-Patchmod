package com.gberguy.ecpatches.runtime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WitcheryVillagesToroGuardDecisionTest {
    @Test
    void disabledTweakLeavesWitcheryGuard() {
        assertEquals(WitcheryVillagesToroGuardDecision.LEAVE,
                WitcheryVillagesToroGuardDecision.forSpawn(false, true, true));
        assertFalse(WitcheryVillagesToroGuardDecision.LEAVE.cancelsAfterSpawn(false));
    }

    @Test
    void missingProvinceLeavesWitcheryGuard() {
        assertEquals(WitcheryVillagesToroGuardDecision.LEAVE,
                WitcheryVillagesToroGuardDecision.forSpawn(true, false, false));
    }

    @Test
    void activeProvinceReplacesOnlyAfterSuccessfulSpawn() {
        WitcheryVillagesToroGuardDecision decision = WitcheryVillagesToroGuardDecision.forSpawn(true, true, true);
        assertEquals(WitcheryVillagesToroGuardDecision.REPLACE, decision);
        assertTrue(decision.cancelsAfterSpawn(true));
    }

    @Test
    void provinceWithoutLordSuppressesWitcheryGuard() {
        WitcheryVillagesToroGuardDecision decision = WitcheryVillagesToroGuardDecision.forSpawn(true, true, false);
        assertEquals(WitcheryVillagesToroGuardDecision.SUPPRESS, decision);
        assertTrue(decision.cancelsAfterSpawn(false));
    }

    @Test
    void failedReplacementLeavesWitcheryGuard() {
        WitcheryVillagesToroGuardDecision decision = WitcheryVillagesToroGuardDecision.forSpawn(true, true, true);
        assertFalse(decision.cancelsAfterSpawn(false));
    }
}
