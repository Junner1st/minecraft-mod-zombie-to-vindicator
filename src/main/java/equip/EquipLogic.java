package equip;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registry;
import net.minecraft.enchantment.Enchantment;

public class EquipLogic {
    
    // config for axe (appear chance/drop rate)
    private static final AxeConfig[] AXE_CONFIGS = {
        new AxeConfig(Items.WOODEN_AXE,   1,  20),
        new AxeConfig(Items.COOKED_BEEF,  4,  80),
        new AxeConfig(Items.STONE_AXE,    5,  20),
        new AxeConfig(Items.IRON_AXE,     65, 30),
        new AxeConfig(Items.DIAMOND_AXE,  20,  2),
        new AxeConfig(Items.NETHERITE_AXE, 5,  1)
    };
    
    // axe enchantment configurations
    // (enchantment/max level/appear chance/level decay factor α)
    private static final AxeEnchantConfig[] AXE_ENCHANT_CONFIGS = {
        // new AxeEnchantConfig(Enchantments.CURSE_OF_VANISHING, 1, 2, 0.1),
        new AxeEnchantConfig(Enchantments.EFFICIENCY,   5,  20, 0.1),
        new AxeEnchantConfig(Enchantments.FIRE_ASPECT,  5,  40, 0.2),
        new AxeEnchantConfig(Enchantments.SHARPNESS,   20,  30, 0.2),
        new AxeEnchantConfig(Enchantments.UNBREAKING,   3,  60, 0.1),
        // new AxeEnchantConfig(Enchantments.CLEAVING, 20, 50, 1000)
    };
    
    // crossbow enchantment configurations
    private static final CrossbowEnchantConfig[] CROSSBOW_ENCHANT_CONFIGS = {
        // new CrossbowEnchantConfig(Enchantments.CURSE_OF_VANISHING, 1, 2, 0.1),
        new CrossbowEnchantConfig(Enchantments.MULTISHOT,    10, 10, 0.4),
        new CrossbowEnchantConfig(Enchantments.PIERCING,      4, 80, 0.2),
        new CrossbowEnchantConfig(Enchantments.QUICK_CHARGE, 10, 30, 0.6)
    };

    // generate a random axe item stack
    public static ItemStack generateAxe(ServerWorld world, Random random) {
        // select axe type based on weighted chance
        AxeConfig selectedAxe = selectAxeType(random);
        ItemStack axe = new ItemStack(selectedAxe.item);
        
        // add enchantments to the axe
        addAxeEnchantments(world, axe, random);
        
        return axe;
    }
    
    // generate a random crossbow item stack
    public static ItemStack generateCrossbow(ServerWorld world, Random random) {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        
        // add enchantments to the crossbow
        addCrossbowEnchantments(world, crossbow, random);
        
        return crossbow;
    }

    // get the drop rate of the axe
    public static float getAxeDropChance(ItemStack axe) {
        for (AxeConfig config : AXE_CONFIGS) {
            if (axe.getItem() == config.item) {
                return config.dropChance / 100.0f;
            }
        }
        return 0.0f; // default drop rate if not found
    }

    // select axe type based on weighted chance
    private static AxeConfig selectAxeType(Random random) {
        int totalWeight = 0;
        for (AxeConfig config : AXE_CONFIGS) {
            totalWeight += config.spawnChance;
        }
        
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (AxeConfig config : AXE_CONFIGS) {
            currentWeight += config.spawnChance;
            if (randomValue < currentWeight) {
                return config;
            }
        }
        
        return AXE_CONFIGS[0];
    }
    

    // add axe enchantments
    private static void addAxeEnchantments(ServerWorld world, ItemStack axe, Random random) {
        Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
            axe.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
        );
        
        for (AxeEnchantConfig config : AXE_ENCHANT_CONFIGS) {
            if (random.nextFloat() * 100 < config.appearChance) {
                int level = calculateEnchantmentLevel(random, config.maxLevel, config.levelDecayAlpha);
                var enchantmentEntry = enchantmentRegistry.getEntry(config.enchantment.getValue());
                if (enchantmentEntry.isPresent()) {
                    builder.add(enchantmentEntry.get(), level);
                }
            }
        }
        
        axe.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }
    
    // add crossbow enchantments
    private static void addCrossbowEnchantments(ServerWorld world, ItemStack crossbow, Random random) {
        Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
            crossbow.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
        );
        
        for (CrossbowEnchantConfig config : CROSSBOW_ENCHANT_CONFIGS) {
            if (random.nextFloat() * 100 < config.appearChance) {
                int level = calculateEnchantmentLevel(random, config.maxLevel, config.levelDecayAlpha);
                var enchantmentEntry = enchantmentRegistry.getEntry(config.enchantment.getValue());
                if (enchantmentEntry.isPresent()) {
                    builder.add(enchantmentEntry.get(), level);
                }
            }
        }
        
        crossbow.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }

    // calculate enchantment level (exponential decay)
    private static int calculateEnchantmentLevel(Random random, int maxLevel, double alpha) {
        for (int level = 1; level <= maxLevel; level++) {
            double chance = 100.0 / Math.pow(alpha, level - 1);
            if (random.nextFloat() * 100 < chance) {
                continue;
            } else {
                return level;
            }
        }
        return maxLevel;
    }
    
    // define configuration classes
    private static class AxeConfig {
        final net.minecraft.item.Item item;
        final int spawnChance;
        final int dropChance;
        
        AxeConfig(net.minecraft.item.Item item, int spawnChance, int dropChance) {
            this.item = item;
            this.spawnChance = spawnChance;
            this.dropChance = dropChance;
        }
    }
    
    private static class AxeEnchantConfig {
        final RegistryKey enchantment;
        final int maxLevel;
        final int appearChance;
        final double levelDecayAlpha;
        
        AxeEnchantConfig(RegistryKey enchantment, int maxLevel, int appearChance, double levelDecayAlpha) {
            this.enchantment = enchantment;
            this.maxLevel = maxLevel;
            this.appearChance = appearChance;
            this.levelDecayAlpha = levelDecayAlpha;
        }
    }
    
    private static class CrossbowEnchantConfig {
        final RegistryKey enchantment;
        final int maxLevel;
        final int appearChance;
        final double levelDecayAlpha;
        
        CrossbowEnchantConfig(RegistryKey enchantment, int maxLevel, int appearChance, double levelDecayAlpha) {
            this.enchantment = enchantment;
            this.maxLevel = maxLevel;
            this.appearChance = appearChance;
            this.levelDecayAlpha = levelDecayAlpha;
        }
    }
}