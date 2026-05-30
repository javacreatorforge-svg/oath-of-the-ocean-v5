package com.redstonedev.oathoftheocean.entity;

import com.redstonedev.oathoftheocean.init.ModSounds;
import com.redstonedev.oathoftheocean.util.DeepWaterCheck;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.FluidTags;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SeaEaterEntity extends Monster implements IAnimatable {

    public enum Mode { STALKING, CHASING }

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(SeaEaterEntity.class, EntityDataSerializers.INT);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int soundCooldown;

    public SeaEaterEntity(EntityType<? extends SeaEaterEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.maxUpStep = 1.0F;
        this.soundCooldown = 200 + this.random.nextInt(400);
        // 80% spawn as STALKING (per spec: "stalks a bunch"), 20% as CHASING ("randomly... chases").
        this.entityData.set(DATA_MODE, this.random.nextInt(100) < 80
                ? Mode.STALKING.ordinal() : Mode.CHASING.ordinal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000000.0D)
                .add(Attributes.ATTACK_DAMAGE, 100000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)   // slow walker
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MODE, Mode.STALKING.ordinal());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanFloat(false);     // doesn't swim - walks on water surface instead
        nav.setCanPassDoors(true);
        nav.setCanOpenDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 96.0F, 1.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // === Water-walking behaviour ==============================================

    /** Per spec: it CANNOT swim - it walks on top of water. */
    @Override
    public boolean canStandOnFluid(FluidState fluid) {
        return fluid.is(FluidTags.WATER);
    }

    @Override public boolean isPushedByFluid()      { return false; }

    /**
     * Buoyancy safety net: canStandOnFluid keeps it on the surface when it walks onto water
     * from above, but if it ever ends up submerged (spawned in the column, knocked in, etc.)
     * it would sink. Push it firmly up to the surface whenever it's touching water.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && this.isInWater()) {
            net.minecraft.world.phys.Vec3 d = this.getDeltaMovement();
            this.setDeltaMovement(d.x * 0.85D, Math.max(d.y, 0.0D) + 0.12D, d.z * 0.85D);
            this.fallDistance = 0;
            this.setOnGround(true);
        }
    }

    // === Mode accessors =======================================================

    public Mode getMode() {
        int idx = this.entityData.get(DATA_MODE);
        Mode[] vs = Mode.values();
        return vs[Math.max(0, Math.min(vs.length - 1, idx))];
    }

    public void setMode(Mode m) { this.entityData.set(DATA_MODE, m.ordinal()); }

    public boolean isStalking() { return getMode() == Mode.STALKING; }

    // === Tick =================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) return;

        if (soundCooldown > 0) soundCooldown--;

        Player nearest = this.level.getNearestPlayer(this, 96.0D);

        // STALKING mode: despawn if player looks at us.
        if (isStalking()) {
            if (nearest != null && isPlayerStaringAt(nearest)) {
                this.discard();
                return;
            }
            // While stalking, don't actively pathfind to player - hold position roughly.
            this.getNavigation().stop();
            if (nearest != null) lockYawTo(nearest);
        }

        // Gated idle sounds.
        if (soundCooldown <= 0) {
            if (nearest != null && DeepWaterCheck.isPlayerNearDeepOcean(nearest)) {
                java.util.List<net.minecraftforge.registries.RegistryObject<net.minecraft.sounds.SoundEvent>> idles =
                        ModSounds.SEA_EATER_IDLES;
                net.minecraft.sounds.SoundEvent s = idles.get(this.random.nextInt(idles.size())).get();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        s, SoundSource.HOSTILE, 1.3F, 1.0F);
            }
            soundCooldown = 400 + this.random.nextInt(800);
        }
    }

    private void lockYawTo(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    private boolean isPlayerStaringAt(Player p) {
        if (this.distanceTo(p) > 64.0D) return false;
        double dx = this.getX() - p.getX();
        double dy = this.getEyeY() - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001D) return false;
        dx /= len; dy /= len; dz /= len;
        Vec3 look = p.getViewVector(1.0F);
        double dot = look.x * dx + look.y * dy + look.z * dz;
        return dot > 0.9D && p.hasLineOfSight(this);
    }

    // === Damage / attack ======================================================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && source.getEntity() instanceof LivingEntity) {
            setMode(Mode.CHASING);
        }
        return result;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        boolean ok = super.doHurtTarget(target);
        if (ok && target instanceof Player && !target.isAlive()) {
            this.discard();
        }
        return ok;
    }

    // === Animations ===========================================================

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "loco",   2, this::locoPredicate));
        data.addAnimationController(new AnimationController<>(this, "attack", 0, this::attackPredicate));
    }

    private <E extends IAnimatable> PlayState locoPredicate(AnimationEvent<E> event) {
        AnimationController<?> controller = event.getController();
        controller.setAnimation(
                new AnimationBuilder().loop("animation.sm_seaeater.walk_1"));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        AnimationController<?> controller = event.getController();
        if (this.swinging && controller.getAnimationState() == software.bernie.geckolib3.core.AnimationState.Stopped) {
            controller.markNeedsReload();
            controller.setAnimation(new AnimationBuilder()
                    .addAnimation("animation.sm_seaeater.attack", EDefaultLoopTypes.PLAY_ONCE));
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return factory; }

    // === Sounds ===============================================================

    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.SEA_EATER_IDLES.get(0).get(); }
    @Override protected SoundEvent getDeathSound()              { return ModSounds.SEA_EATER_IDLES.get(0).get(); }

    // === NBT ==================================================================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Mode", this.entityData.get(DATA_MODE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_MODE, tag.getInt("Mode"));
    }
}
