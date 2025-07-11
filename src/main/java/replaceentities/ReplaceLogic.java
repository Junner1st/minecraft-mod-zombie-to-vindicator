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
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;

import equip.EquipLogic;

public class ReplaceLogic implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> {
            // get chunck coordinates of the entity
            var chunkPos = entity.getChunkPos();
            int chunkX = chunkPos.x;
            int chunkZ = chunkPos.z;
            int chunkY = entity.getBlockPos().getY() >> 4; // 相當於 / 16
            
            // if chunk position x+y+z is odd, replace the entity
            if ((chunkX + chunkY + chunkZ) % 2 == 1) {
                if (entity instanceof ZombieEntity) {
                    replaceEntity(world, (LivingEntity)entity, EntityType.VINDICATOR);
                } else if (entity instanceof SkeletonEntity) {
                    replaceEntity(world, (LivingEntity)entity, EntityType.PILLAGER);
                }
            }
        });
    }

    private static <T extends LivingEntity> T replaceEntity(ServerWorld world, LivingEntity orig, EntityType<T> type) {
        var pos = orig.getPos();
        var yaw = orig.getYaw();
        var pitch = orig.getPitch();
        orig.discard();

        T ent = type.create(world, SpawnReason.NATURAL);
        if (ent == null) return null;

        ent.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
        
        // equip mobs
        if (ent instanceof VindicatorEntity vindicator) {
            equipVindicator(vindicator, world);
        } else if (ent instanceof PillagerEntity pillager) {
            equipPillager(pillager, world);
        }
        
        world.spawnEntity(ent);
        return ent;
    }
    
    private static void equipVindicator(VindicatorEntity vindicator, ServerWorld world) {
        ItemStack axe = EquipLogic.generateAxe(world, world.getRandom());
        vindicator.equipStack(EquipmentSlot.MAINHAND, axe);
        
        float dropChance = EquipLogic.getAxeDropChance(axe);
        vindicator.setEquipmentDropChance(EquipmentSlot.MAINHAND, dropChance);
    }
    
    private static void equipPillager(PillagerEntity pillager, ServerWorld world) {
        ItemStack crossbow = EquipLogic.generateCrossbow(world, world.getRandom());
        pillager.equipStack(EquipmentSlot.MAINHAND, crossbow);
        
        float dropChance = 0.3f;
        pillager.setEquipmentDropChance(EquipmentSlot.MAINHAND, dropChance);
    }
}