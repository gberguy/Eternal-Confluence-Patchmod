package net.minecraftforge.event.entity.living;
import net.minecraft.entity.EntityLivingBase;
public class LivingEvent {
    private final EntityLivingBase entity;
    public LivingEvent(EntityLivingBase entity) { this.entity = entity; }
    public EntityLivingBase getEntityLiving() { return entity; }
    public static class LivingUpdateEvent extends LivingEvent {
        public LivingUpdateEvent(EntityLivingBase entity) { super(entity); }
    }
}
