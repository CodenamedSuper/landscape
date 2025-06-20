package com.codenamed.landscape.entity;

import com.codenamed.landscape.block.entity.SongbirdNestBlockEntity;
import com.codenamed.landscape.entity.client.animation.SongbirdAnimations;
import com.codenamed.landscape.registry.LandscapeBlocks;
import com.codenamed.landscape.registry.LandscapeEntities;
import com.codenamed.landscape.registry.LandscapeItemTags;
import com.codenamed.landscape.registry.LandscapeSoundEvents;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.ParrotImitation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class Songbird extends Animal implements FlyingAnimal {

    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;

    public final AnimationState sleepIdleState = new AnimationState();
    public final AnimationState flyIdleState = new AnimationState();
    public final AnimationState idleState = new AnimationState();

    public  BlockPos nestPos;

    public boolean sleeping = false;

    @javax.annotation.Nullable
    private BlockPos jukebox;

    public Songbird(EntityType<? extends Songbird> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);

    }

    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Songbird.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public void setSleeping(boolean value) {
        this.entityData.set(SLEEPING, value);
        this.sleeping = value; // still set your local var for logic
    }

    @javax.annotation.Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnGroupData) {

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(LandscapeItemTags.SONGBIRD_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new Songbird.SongbirdWanderGoal(this, (double)1.0F));
        this.goalSelector.addGoal(7, new SongbirdSleepAtNestGoal(this, 1.0D));

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, (double)6.0F)
                .add(Attributes.FLYING_SPEED, (double)0.4F)
                .add(Attributes.MOVEMENT_SPEED, (double)0.2F)
                .add(Attributes.ATTACK_DAMAGE, (double)3.0F)
                .add(Attributes.FOLLOW_RANGE, 24D);
    }

    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isSleeping()) {
            if (!sleepIdleState.isStarted()) sleepIdleState.start(this.tickCount);
        } else {
            sleepIdleState.stop();
        }

        if (this.isFlying() && !this.isSleeping()) {
            if (!flyIdleState.isStarted()) flyIdleState.start(this.tickCount);
        } else {
            flyIdleState.stop();
        }

        if (!this.isFlying() && !this.isSleeping()) {
            if (!idleState.isStarted()) idleState.start(this.tickCount);
        } else {
            idleState.stop();
        }

        this.calculateFlapping();
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (float)(!this.onGround() && !this.isPassenger() ? 4 : -1) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < (double)0.0F) {
            this.setDeltaMovement(vec3.multiply((double)1.0F, 0.6, (double)1.0F));
        }

        this.flap += this.flapping * 2.0F;
    }


    public boolean isFood(ItemStack stack) {
        return stack.is(LandscapeItemTags.SONGBIRD_FOOD);
    }

    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @javax.annotation.Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return LandscapeEntities.SONGBIRD.get().create(level);
    }


    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAlive() ? LandscapeSoundEvents.SONGBIRD_AMBIENT.get() : null;
    }

    @Override
    protected float getSoundVolume() {
        return 1f;
    }

    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
    }

    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    protected void onFlap() {
        this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    public float getVoicePitch() {
        return getPitch(this.random);
    }

    public static float getPitch(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public @NotNull SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }
    public boolean isPushable() {
        return true;
    }

    protected void doPush(Entity entity) {
        if (!(entity instanceof Player)) {
            super.doPush(entity);
        }

    }

    public boolean isFlying() {
        return !this.onGround();
    }

    public Vec3 getLeashOffset() {
        return new Vec3((double)0.0F, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    static class SongbirdSleepAtNestGoal extends Goal {
        private final Songbird songbird;
        private final double speed;
        private BlockPos targetNest;
        private int cooldownTicks = 0;


        public SongbirdSleepAtNestGoal(Songbird bird, double speed) {
            this.songbird = bird;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (cooldownTicks > 0) {
                cooldownTicks--;
                return false;
            }

            if (songbird.sleeping) return false;
            Level level = songbird.level();
            if (!(level instanceof ServerLevel) || !level.dimension().equals(Level.OVERWORLD)) return false;
            if (!level.isNight()) return false;

            targetNest = findNearbyUnoccupiedNest();
            return targetNest != null;
        }

        @Override
        public void start() {
            if (targetNest != null) {
                songbird.getNavigation().moveTo(targetNest.getX() + 0.5, targetNest.getY() + 0.1, targetNest.getZ() + 0.5, speed);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return songbird.sleeping && songbird.level().isNight();
        }

        @Override
        public void tick() {
            Level level = songbird.level();

            // If it's no longer night or nest is gone, wake up
            if (!level.isNight() || targetNest == null || level.getBlockEntity(targetNest) == null) {
                if (songbird.isSleeping()) {
                    songbird.setSleeping(false);

                    // Clear occupancy from the nest if valid
                    if (targetNest != null) {
                        BlockEntity be = level.getBlockEntity(targetNest);
                        if (be instanceof SongbirdNestBlockEntity nestBe) {
                            nestBe.occupant.remove();
                        }
                    }
                }
                return;
            }

            if (!songbird.isSleeping() && songbird.position().closerThan(Vec3.atCenterOf(targetNest), 0.5)) {
                songbird.getNavigation().stop();
                songbird.setDeltaMovement(Vec3.ZERO);
                songbird.setSleeping(true);
                songbird.setPos(targetNest.getX() + 0.5, targetNest.getY() + 0.01, targetNest.getZ() + 0.5); // Y offset keeps it sitting nicely
            }

            if (songbird.isSleeping()) {
                songbird.setDeltaMovement(Vec3.ZERO);
                songbird.getNavigation().stop();
                songbird.setPos(targetNest.getX() + 0.5, targetNest.getY() + 0.01, targetNest.getZ() + 0.5);
            }
        }


        private void wakeUp() {
            if (!songbird.sleeping) return;

            songbird.sleeping = false;
            cooldownTicks = 200; // 10 seconds of no sleep behavior

            if (targetNest != null) {
                BlockEntity be = songbird.level().getBlockEntity(targetNest);
                if (be instanceof SongbirdNestBlockEntity nestBe) {
                    nestBe.occupant.remove();
                }
            }

            targetNest = null;
        }

        @Override
        public void stop() {

        }

        private BlockPos findNearbyUnoccupiedNest() {
            BlockPos origin = songbird.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 8, 16))) {
                if (songbird.level().getBlockState(pos).is(LandscapeBlocks.SONGBIRD_NEST)) {
                    BlockEntity be = songbird.level().getBlockEntity(pos);
                    if (be instanceof SongbirdNestBlockEntity nestBe && !nestBe.occupant.occupied()) {
                        return pos.immutable();
                    }
                }
            }
            return null;
        }
    }

    static class SongbirdWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public SongbirdWanderGoal(PathfinderMob pathfinderMob, double d) {

            super(pathfinderMob, d);
        }

        @javax.annotation.Nullable
        protected Vec3 getPosition() {
            Vec3 vec3 = null;
            if (this.mob.isInWater()) {
                vec3 = LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3 = this.getTreePos();
            }

            return vec3 == null ? super.getPosition() : vec3;
        }

        @Nullable
        private Vec3 getTreePos() {
            BlockPos blockpos = this.mob.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

            for(BlockPos blockpos1 : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - (double)3.0F), Mth.floor(this.mob.getY() - (double)6.0F), Mth.floor(this.mob.getZ() - (double)3.0F), Mth.floor(this.mob.getX() + (double)3.0F), Mth.floor(this.mob.getY() + (double)6.0F), Mth.floor(this.mob.getZ() + (double)3.0F))) {
                if (!blockpos.equals(blockpos1)) {
                    BlockState blockstate = this.mob.level().getBlockState(blockpos$mutableblockpos1.setWithOffset(blockpos1, Direction.DOWN));
                    boolean flag = blockstate.is(LandscapeBlocks.SONGBIRD_NEST) || blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
                    if (flag && this.mob.level().isEmptyBlock(blockpos1) && this.mob.level().isEmptyBlock(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.UP))) {
                        return Vec3.atBottomCenterOf(blockpos1);
                    }
                }
            }

            return null;
        }
    }

}

