package com.gberguy.ecpatches.runtime;

import com.gberguy.ecpatches.core.PatchId;
import com.gberguy.ecpatches.core.PatchSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public final class GhostlyShape {
    private GhostlyShape() {
    }

    public static PatchId integration(Entity mob) {
        if (!(mob instanceof EntityLiving)) return null;
        ResourceLocation key = EntityList.getKey(mob);
        if (key == null) return null;
        switch (key.getNamespace()) {
            case "lycanitesmobs": return PatchId.LYCANITES_GHOST;
            case "ebwizardry": return PatchId.WIZARDRY_GHOST;
            case "ancientspellcraft": return PatchId.ANCIENT_GHOST;
            case "toroquest": return PatchId.TORO_GHOST;
            default: return null;
        }
    }

    public static boolean shouldIgnore(Object mob, Object target) {
        return shouldIgnore(mob, target, PatchSettings.current());
    }

    public static boolean shouldIgnore(Object mob, Object target, PatchSettings settings) {
        if (!(mob instanceof EntityLiving) || !(target instanceof EntityPlayer)) return false;
        PatchId id = integration((Entity) mob);
        if (id == null || !settings.enabled(id)) return false;
        Potion potion = Potion.getPotionFromResourceLocation("tombstone:ghostly_shape");
        return potion != null && ((EntityPlayer) target).isPotionActive(potion);
    }
}
