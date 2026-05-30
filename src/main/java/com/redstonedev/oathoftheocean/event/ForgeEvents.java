package com.redstonedev.oathoftheocean.event;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import com.redstonedev.oathoftheocean.entity.ElGranMajaEntity;
import com.redstonedev.oathoftheocean.entity.SeaEaterEntity;
import com.redstonedev.oathoftheocean.entity.TheBloopEntity;
import com.redstonedev.oathoftheocean.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class ForgeEvents {

    private static final Random RNG = new Random();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter % 100 != 0) return; // ~5s cadence
        if (event.getServer() == null) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            trySpawnAll(level);
        }
    }

    private void trySpawnAll(ServerLevel level) {
        List<? extends ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        boolean isNight = !level.isDay();

        for (ServerPlayer player : players) {
            if (!com.redstonedev.oathoftheocean.util.DeepWaterCheck.isPlayerNearDeepOcean(player)) continue;

            // Each entity rolls independently each tick. One-per-dimension cap each.
            if (!hasOf(level, ModEntities.EL_GRAN_MAJA.get())) {
                // Mostly night, sometimes day.
                int chance = isNight ? 220 : 600;
                if (RNG.nextInt(chance) == 0) {
                    spawnUnderwater(level, player, ModEntities.EL_GRAN_MAJA.get());
                }
            }
            if (!hasOf(level, ModEntities.SEA_EATER.get())) {
                // Mostly day, sometimes night.
                int chance = isNight ? 600 : 220;
                if (RNG.nextInt(chance) == 0) {
                    spawnOnWaterSurface(level, player, ModEntities.SEA_EATER.get());
                }
            }
            if (!hasOf(level, ModEntities.THE_BLOOP.get())) {
                int chance = isNight ? 220 : 600;
                if (RNG.nextInt(chance) == 0) {
                    spawnUnderwater(level, player, ModEntities.THE_BLOOP.get());
                }
            }
        }
    }

    private boolean hasOf(ServerLevel level, EntityType<?> type) {
        return !level.getEntities(type, e -> !e.isRemoved()).isEmpty();
    }

    /** Find a position fully submerged in water near the player. */
    private BlockPos pickUnderwaterPos(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 32; attempt++) {
            int dx = RNG.nextInt(33) - 16;
            int dz = RNG.nextInt(33) - 16;
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            // Top of water column
            int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            // Place a few blocks above the floor, hopefully in water
            int y = surface + 2 + RNG.nextInt(8);
            BlockPos candidate = new BlockPos(x, y, z);
            if (level.getFluidState(candidate).is(Fluids.WATER)
                    && level.getFluidState(candidate.above()).is(Fluids.WATER)) {
                return candidate;
            }
        }
        return null;
    }

    /** Find a position right at the water surface near the player. */
    private BlockPos pickWaterSurfacePos(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 32; attempt++) {
            int dx = RNG.nextInt(33) - 16;
            int dz = RNG.nextInt(33) - 16;
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            BlockState below = level.getBlockState(candidate.below());
            // Surface of water = water block right below, air at candidate level
            if (level.getBlockState(candidate).isAir() && below.getFluidState().is(Fluids.WATER)) {
                return candidate;
            }
        }
        return null;
    }

    private <T extends Mob> void spawnUnderwater(ServerLevel level, ServerPlayer player, EntityType<T> type) {
        BlockPos pos = pickUnderwaterPos(level, player);
        if (pos == null) return;
        T entity = type.create(level);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360F, 0);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                MobSpawnType.EVENT, null, null);
        level.addFreshEntity(entity);
        OathOfTheOcean.LOGGER.debug("Spawned {} underwater near {}",
                type.getDescriptionId(), player.getName().getString());
    }

    private <T extends Mob> void spawnOnWaterSurface(ServerLevel level, ServerPlayer player, EntityType<T> type) {
        BlockPos pos = pickWaterSurfacePos(level, player);
        if (pos == null) return;
        T entity = type.create(level);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360F, 0);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                MobSpawnType.EVENT, null, null);
        level.addFreshEntity(entity);
        OathOfTheOcean.LOGGER.debug("Spawned {} on water surface near {}",
                type.getDescriptionId(), player.getName().getString());
    }
}
