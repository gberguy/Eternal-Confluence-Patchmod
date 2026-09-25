package net.minecraft.entity;
import net.minecraft.potion.Potion;
public class EntityLivingBase extends Entity {
    public Potion activePotion;
    public boolean isPotionActive(Potion potion) { return potion != null && potion == activePotion; }
    public boolean isEntityAlive() { return true; }
}
