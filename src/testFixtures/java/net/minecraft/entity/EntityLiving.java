package net.minecraft.entity;
import java.util.function.Consumer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
public class EntityLiving extends EntityLivingBase {
    private EntityLivingBase attackTarget;
    private final PathNavigate navigator = new PathNavigate();
    public Consumer<LivingSetAttackTargetEvent> listener;
    public int assignments;
    public EntityLivingBase getAttackTarget() { return attackTarget; }
    public void setAttackTarget(EntityLivingBase target) {
        attackTarget = target;
        assignments++;
        if (listener != null) listener.accept(new LivingSetAttackTargetEvent(this, target));
    }
    public PathNavigate getNavigator() { return navigator; }
    public EntitySenses getEntitySenses() { return new EntitySenses(); }
    public boolean canAttackClass(Class<?> type) { return true; }
}
