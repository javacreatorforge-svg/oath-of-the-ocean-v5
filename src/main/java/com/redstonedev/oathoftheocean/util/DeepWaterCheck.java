package com.redstonedev.oathoftheocean.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * Helper to determine whether a player is "near deep ocean" - used by all three entities
 * to gate their idle sounds. Per spec: sounds only play if the player is in or near a deep
 * ocean (including being on an island in deep ocean). They do NOT play if the player is
 * near shallow water, on a land biome, or generally not near deep water.
 */
public final class DeepWaterCheck {
    private DeepWaterCheck() {}

    private static final int SCAN_RADIUS = 32;
    private static final int SCAN_STEP   = 8;

    /** Samples a 65x65 grid (9 steps each axis = 81 samples) around the player. True if any
     *  sample lies in a deep-ocean biome. */
    public static boolean isPlayerNearDeepOcean(Player player) {
        // A player out on a raft counts as "in the deep" even if the biome grid misses it.
        if (isOnRaft(player)) return true;
        Level level = player.level;
        BlockPos origin = player.blockPosition();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx += SCAN_STEP) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz += SCAN_STEP) {
                BlockPos check = origin.offset(dx, 0, dz);
                Holder<Biome> biome = level.getBiome(check);
                if (biome.is(BiomeTags.IS_DEEP_OCEAN)) return true;
            }
        }
        return false;
    }

    /**
     * True if the player is standing on a raft - oak planks floating on water. We look for the
     * player standing on an oak plank that has water beneath it, and require at least a small
     * platform of planks around them (a real raft, not a single block).
     */
    public static boolean isOnRaft(Player player) {
        Level level = player.level;
        BlockPos below = player.blockPosition().below();
        if (level.getBlockState(below).getBlock() != Blocks.OAK_PLANKS) return false;

        // Water somewhere just beneath the planks -> it's floating on the ocean.
        boolean overWater = false;
        for (int i = 1; i <= 4; i++) {
            if (level.getFluidState(below.below(i)).is(Fluids.WATER)) { overWater = true; break; }
        }
        if (!overWater) return false;

        // Count plank blocks in the 3x3 under/around the player; need a real platform.
        int planks = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getBlockState(below.offset(dx, 0, dz)).getBlock() == Blocks.OAK_PLANKS) {
                    planks++;
                }
            }
        }
        return planks >= 5; // most of a 3x3 raft present
    }

    /** Convenience overload for when only the entity's own position is relevant. */
    public static boolean isEntityInDeepOcean(Entity e) {
        Holder<Biome> biome = e.level.getBiome(e.blockPosition());
        return biome.is(BiomeTags.IS_DEEP_OCEAN);
    }
}
