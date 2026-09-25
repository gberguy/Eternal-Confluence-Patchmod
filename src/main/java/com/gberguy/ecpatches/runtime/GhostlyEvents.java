package com.gberguy.ecpatches.runtime;

import com.gberguy.ecpatches.core.PatchSettings;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class GhostlyEvents {
    private final PatchSettings settings;

    public GhostlyEvents(PatchSettings settings) {
        this.settings = settings;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() != null) clearTarget(event.getEntityLiving());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        clearTarget(event.getEntityLiving());
    }

    private void clearTarget(EntityLivingBase entity) {
        if (!(entity instanceof EntityLiving) || entity.world.isRemote) return;
        EntityLiving mob = (EntityLiving) entity;
        if (!GhostlyShape.shouldIgnore(mob, mob.getAttackTarget(), settings)) return;
        mob.setAttackTarget(null);
        mob.getNavigator().clearPath();
    }
}
