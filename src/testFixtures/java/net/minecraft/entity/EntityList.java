package net.minecraft.entity;
import net.minecraft.util.ResourceLocation;
public class EntityList {
    public static ResourceLocation getKey(Entity entity) { return entity.registryKey; }
}
