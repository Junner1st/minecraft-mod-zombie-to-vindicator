package replaceentities;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.SpawnReason;

public class ReplaceLogic implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> {
            if (entity instanceof ZombieEntity) {
                replaceEntity(world, (LivingEntity)entity, EntityType.VINDICATOR);
            } else if (entity instanceof SkeletonEntity) {
                replaceEntity(world, (LivingEntity)entity, EntityType.PILLAGER);
            }
        });
    }

    private void replaceEntity(ServerWorld world, LivingEntity original, EntityType<? extends LivingEntity> type) {
        Vec3d pos = original.getPos();
        float yaw = original.getYaw(), pitch = original.getPitch();
        original.discard();

		LivingEntity repl = type.create(world, SpawnReason.NATURAL);
        if (repl == null) return;

        repl.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
        world.spawnEntity(repl);
    }
}