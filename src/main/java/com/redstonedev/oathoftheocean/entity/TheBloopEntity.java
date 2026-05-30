package com.redstonedev.oathoftheocean.entity;

import com.redstonedev.oathoftheocean.init.ModSounds;
import com.redstonedev.oathoftheocean.util.DeepWaterCheck;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class TheBloopEntity extends WaterAnimal implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int soundCooldown;
    private boolean pendingDespawn = false;

    public TheBloopEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.soundCooldown = 200 + this.random.nextInt(400);
        // Medium swimmer - between the slow salmon (0.02F) and fast El Gran Maja (0.06F).
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.04F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100000.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.1D)   // medium swimmer
                .add(Attributes.FOLLOW_RANGE, 96.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, true));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 40));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public MobType getMobType() { return MobType.WATER; }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.04F, travelVector);
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
        // Despawn cleanly BEFORE any AI/navigation ticks, so a removed entity never gets
        // re-ticked (which would throw and crash with removeErroringEntities=false).
        if (!this.level.isClientSide && pendingDespawn) {
            this.discard();
            return;
        }
        super.aiStep();
        if (this.level.isClientSide) return;

        if (soundCooldown > 0) soundCooldown--;
        if (soundCooldown <= 0) {
            Player nearest = this.level.getNearestPlayer(this, 64.0D);
            if (nearest != null && DeepWaterCheck.isPlayerNearDeepOcean(nearest)) {
                java.util.List<net.minecraftforge.registries.RegistryObject<net.minecraft.sounds.SoundEvent>> idles =
                        ModSounds.THE_BLOOP_IDLES;
                net.minecraft.sounds.SoundEvent s = idles.get(this.random.nextInt(idles.size())).get();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        s, SoundSource.HOSTILE, 1.5F, 1.0F);
            }
            soundCooldown = 400 + this.random.nextInt(800);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ok = super.doHurtTarget(target);
        // Flag for despawn next tick (NOT discard now - that crashes mid-tick).
        if (ok && target instanceof Player && !target.isAlive()) {
            pendingDespawn = true;
        }
        return ok;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(
                new AnimationBuilder().loop("animation.sm_bloop.swim_1"));
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return factory; }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.THE_BLOOP_IDLES.get(0).get(); }
    @Override protected SoundEvent getDeathSound()              { return ModSounds.THE_BLOOP_IDLES.get(0).get(); }
}
