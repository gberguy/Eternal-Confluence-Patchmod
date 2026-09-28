package com.gberguy.ecpatches.runtime;

import com.witcherywalls.worldgen.VillageWallGenerator.StructureBounds;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraftforge.fml.common.Loader;

public final class WitcheryVillagesToroWallBounds {
    private static final int MARGIN = 8;
    private static final Set<String> TORO_PIECES = new HashSet<>(Arrays.asList(
            "net.torocraft.toroquest.generation.village.VillageHandlerKeep$VillagePieceKeep",
            "net.torocraft.toroquest.generation.village.VillageHandlerBarracks$VillagePieceBarracks",
            "net.torocraft.toroquest.generation.village.VillageHandlerGuardTower$VillagePieceGuardTower",
            "net.torocraft.toroquest.generation.village.VillageHandlerShop$VillagePieceShop",
            "net.torocraft.toroquest.generation.village.VillageHandlerWall$VillagePieceWall"));

    private WitcheryVillagesToroWallBounds() {
    }

    public static void extend(List<StructureBounds> roads, List<?> components) {
        if (!Loader.isModLoaded("toroquest") || roads == null || roads.isEmpty() || components == null) return;
        for (Object value : components) {
            if (!(value instanceof StructureComponent) || !TORO_PIECES.contains(value.getClass().getName())) continue;
            StructureBoundingBox building = ((StructureComponent) value).getBoundingBox();
            if (building == null) continue;
            int minX = building.minX - MARGIN;
            int maxX = building.maxX + MARGIN;
            int minZ = building.minZ - MARGIN;
            int maxZ = building.maxZ + MARGIN;
            StructureBounds nearest = null;
            long bestDistance = Long.MAX_VALUE;
            for (StructureBounds road : roads) {
                if (road == null) continue;
                long dx = Math.max(0L, Math.max((long) minX - road.maxX, (long) road.minX - maxX));
                long dz = Math.max(0L, Math.max((long) minZ - road.maxZ, (long) road.minZ - maxZ));
                long distance = dx * dx + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = road;
                }
            }
            if (nearest == null) continue;
            nearest.minX = Math.min(nearest.minX, minX);
            nearest.maxX = Math.max(nearest.maxX, maxX);
            nearest.minZ = Math.min(nearest.minZ, minZ);
            nearest.maxZ = Math.max(nearest.maxZ, maxZ);
        }
    }
}
