package com.gberguy.ecpatches.core;

import com.gberguy.ecpatches.runtime.GhostlyEvents;
import com.gberguy.ecpatches.runtime.GhostlyShape;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class GhostlyBehaviorTest {
    @TempDir Path directory;

    private PatchSettings settings(PatchId only) throws Exception {
        StringBuilder content = new StringBuilder();
        for (PatchId id : PatchId.values()) content.append(id.key).append('=').append(id == only).append('\n');
        Path file = directory.resolve(only == null ? "none.cfg" : only.key + ".cfg");
        Files.write(file, content.toString().getBytes(StandardCharsets.UTF_8));
        return PatchSettings.read(file.toFile());
    }

    private EntityLiving mob(String namespace) {
        EntityLiving mob = new EntityLiving();
        mob.registryKey = new ResourceLocation(namespace + ":test");
        return mob;
    }

    @AfterEach
    void resetPotion() { Potion.registered = null; }

    @TestFactory
    Stream<DynamicTest> eachSwitchOnlyControlsItsOwnMobs() {
        return Stream.of(PatchId.values()).filter(PatchId::ghostly).map(enabled -> DynamicTest.dynamicTest(enabled.key, () -> {
            PatchSettings options = settings(enabled);
            Potion.registered = new Potion();
            EntityPlayer player = new EntityPlayer();
            player.activePotion = Potion.registered;
            for (PatchId target : PatchId.values()) {
                if (target.ghostly()) assertEquals(target == enabled, GhostlyShape.shouldIgnore(mob(target.modId), player, options));
            }
            assertFalse(GhostlyShape.shouldIgnore(mob("minecraft"), player, options));
            assertFalse(GhostlyShape.shouldIgnore(mob("othermod"), player, options));
            assertFalse(GhostlyShape.shouldIgnore(new EntityLiving(), player, options));
            assertFalse(GhostlyShape.shouldIgnore(player, player, options));
            assertFalse(GhostlyShape.shouldIgnore(mob(enabled.modId), new EntityLiving(), options));
            player.activePotion = null;
            assertFalse(GhostlyShape.shouldIgnore(mob(enabled.modId), player, options));
            player.activePotion = Potion.registered;
            Potion.registered = null;
            assertFalse(GhostlyShape.shouldIgnore(mob(enabled.modId), player, options));
        }));
    }

    @TestFactory
    Stream<DynamicTest> directAssignmentsExistingTargetsAndEffectExpiry() {
        return Stream.of(PatchId.values()).filter(PatchId::ghostly).map(id -> DynamicTest.dynamicTest(id.key, () -> {
            GhostlyEvents events = new GhostlyEvents(settings(id));
            EntityLiving mob = mob(id.modId);
            mob.listener = events::onTarget;
            EntityPlayer player = new EntityPlayer();
            Potion.registered = new Potion();
            player.activePotion = Potion.registered;
            mob.setAttackTarget(player);
            assertNull(mob.getAttackTarget());
            assertEquals(2, mob.assignments);
            assertEquals(1, mob.getNavigator().clears);
            player.activePotion = null;
            mob.setAttackTarget(player);
            assertSame(player, mob.getAttackTarget());
            player.activePotion = Potion.registered;
            events.onUpdate(new LivingEvent.LivingUpdateEvent(mob));
            assertNull(mob.getAttackTarget());
            assertEquals(2, mob.getNavigator().clears);
            player.activePotion = null;
            mob.setAttackTarget(player);
            assertSame(player, mob.getAttackTarget());
            events.onUpdate(new LivingEvent.LivingUpdateEvent(mob));
            assertSame(player, mob.getAttackTarget());
        }));
    }

    @Test
    void disabledAndClientTargetsAreUntouched() throws Exception {
        EntityPlayer player = new EntityPlayer();
        player.activePotion = Potion.registered = new Potion();
        EntityLiving mob = mob("toroquest");
        GhostlyEvents disabled = new GhostlyEvents(settings(null));
        mob.listener = disabled::onTarget;
        mob.setAttackTarget(player);
        disabled.onUpdate(new LivingEvent.LivingUpdateEvent(mob));
        assertSame(player, mob.getAttackTarget());
        GhostlyEvents enabled = new GhostlyEvents(settings(PatchId.TORO_GHOST));
        mob.listener = enabled::onTarget;
        mob.world.isRemote = true;
        mob.setAttackTarget(player);
        enabled.onUpdate(new LivingEvent.LivingUpdateEvent(mob));
        assertSame(player, mob.getAttackTarget());
    }
}
