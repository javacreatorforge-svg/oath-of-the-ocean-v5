package com.redstonedev.oathoftheocean.entity;

import com.redstonedev.oathoftheocean.init.ModSounds;
import com.redstonedev.oathoftheocean.util.DeepWaterCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class ElGranMajaEntity extends WaterAnimal implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int soundCooldown;
    private int blockBreakCooldown = 60;
    private int aliveTicks = 0;

    public ElGranMajaEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.soundCooldown = 200 + this.random.nextInt(400);
        // Fast swimmer - higher move-control modifier than the slow salmon example (0.02F).
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.06F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100000.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.6D)   // fast swimmer
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1.0D, 40));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override public MobType getMobType() { return MobType.WATER; }

    // Smooth 3-D swimming, salmon/dolphin style. The 0.06F constant (vs 0.02F for a slow
    // fish) makes El Gran Maja a fast swimmer without breaking physics.
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.06F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) return;

        aliveTicks++;
        if (aliveTicks >= 1200) { // despawn after 1 minute
            this.discard();
            return;
        }

        if (soundCooldown > 0) soundCooldown--;
        if (blockBreakCooldown > 0) blockBreakCooldown--;

        if (soundCooldown <= 0) {
            Player nearest = this.level.getNearestPlayer(this, 64.0D);
            if (nearest != null && DeepWaterCheck.isPlayerNearDeepOcean(nearest)) {
                java.util.List<net.minecraftforge.registries.RegistryObject<net.minecraft.sounds.SoundEvent>> idles =
                        ModSounds.EL_GRAN_MAJA_IDLES;
                net.minecraft.sounds.SoundEvent s = idles.get(this.random.nextInt(idles.size())).get();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        s, SoundSource.HOSTILE, 1.4F, 1.0F);
            }
            soundCooldown = 400 + this.random.nextInt(800);
        }

        if (blockBreakCooldown <= 0) {
            tryBreakNearbyBlock();
            blockBreakCooldown = 100 + this.random.nextInt(140);
        }
    }

    private void tryBreakNearbyBlock() {
        BlockPos center = this.blockPosition();
        BlockPos target = null;
        scan:
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -1; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState bs = this.level.getBlockState(p);
                    if (canBreak(bs, p)) { target = p; break scan; }
                }
            }
        }
        if (target != null) this.level.destroyBlock(target, true, this);
    }

    private boolean canBreak(BlockState bs, BlockPos p) {
        if (bs.isAir()) return false;
        if (bs.getDestroySpeed(this.level, p) < 0) return false;
        Block b = bs.getBlock();
        return b != Blocks.BEDROCK && b != Blocks.BARRIER && b != Blocks.COMMAND_BLOCK
                && b != Blocks.STRUCTURE_BLOCK && b != Blocks.JIGSAW && b != Blocks.LIGHT
                && b != Blocks.END_PORTAL_FRAME && b != Blocks.END_PORTAL
                && b != Blocks.NETHER_PORTAL && b != Blocks.VOID_AIR && b != Blocks.WATER;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(
                new AnimationBuilder().loop("animation.sm_elgranmaja.swim_1"));
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return factory; }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.EL_GRAN_MAJA_IDLES.get(0).get(); }
    @Override protected SoundEvent getDeathSound()              { return ModSounds.EL_GRAN_MAJA_IDLES.get(0).get(); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AliveTicks", aliveTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        aliveTicks = tag.getInt("AliveTicks");
    }
}
