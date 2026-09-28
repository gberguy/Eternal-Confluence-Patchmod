package com.gberguy.ecpatches.runtime;

import com.gberguy.ecpatches.EternalConfluenceTweaks;
import com.gberguy.ecpatches.core.PatchId;
import com.gberguy.ecpatches.core.PatchSettings;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.entities.EntityGuard;

public final class WitcheryVillagesToroGuardEvents {
    private final PatchSettings settings;

    public WitcheryVillagesToroGuardEvents(PatchSettings settings) {
        this.settings = settings;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityVillageGuard) || event.getWorld().isRemote) return;
        boolean enabled = settings.enabled(PatchId.WITCHERY_VILLAGES_TORO_GUARDS);
        if (!enabled) return;
        EntityVillageGuard original = (EntityVillageGuard) event.getEntity();
        World world = event.getWorld();
        BlockPos spawn = original.getPosition();
        Province province = CivilizationUtil.getProvinceAt(world, spawn.getX() >> 4, spawn.getZ() >> 4);
        WitcheryVillagesToroGuardDecision decision = WitcheryVillagesToroGuardDecision.forSpawn(
                enabled, province != null, province != null && province.hasLord);
        if (decision == WitcheryVillagesToroGuardDecision.LEAVE) return;
        if (decision == WitcheryVillagesToroGuardDecision.SUPPRESS) {
            event.setCanceled(true);
            return;
        }
        if (!valid(province)) return;

        try {
            EntityGuard replacement = new EntityGuard(world, province);
            replacement.setCivilization(province.getCiv());
            replacement.setUUID(province.getUUID());
            Province assigned = replacement.getHomeProvince();
            if (!sameProvince(province, assigned)) return;

            replacement.setLocationAndAngles(original.posX, original.posY, original.posZ,
                    original.rotationYaw, original.rotationPitch);
            replacement.rotationYawHead = original.rotationYawHead;
            replacement.renderYawOffset = original.renderYawOffset;
            replacement.motionX = original.motionX;
            replacement.motionY = original.motionY;
            replacement.motionZ = original.motionZ;
            replacement.onGround = original.onGround;
            replacement.fallDistance = original.fallDistance;
            replacement.onInitialSpawn(world.getDifficultyForLocation(replacement.getPosition()), (IEntityLivingData) null);
            if (decision.cancelsAfterSpawn(world.spawnEntity(replacement))) event.setCanceled(true);
        } catch (RuntimeException | LinkageError error) {
            EternalConfluenceTweaks.LOGGER.error("Could not replace a Witchery Villages guard with a ToroQuest guard", error);
        }
    }

    private static boolean valid(Province province) {
        return province != null && province.getCiv() != null && province.getName() != null && province.getUUID() != null;
    }

    private static boolean sameProvince(Province expected, Province actual) {
        return actual != null && expected.getUUID().equals(actual.getUUID())
                && expected.getCiv() == actual.getCiv() && expected.getName().equals(actual.getName());
    }
}
