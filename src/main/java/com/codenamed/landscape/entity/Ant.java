package com.codenamed.landscape.entity;

import com.codenamed.landscape.block.entity.AntNestBlockEntity;
import com.codenamed.landscape.registry.LandscapeBlockTags;
import com.codenamed.landscape.registry.LandscapeEntities;
import com.codenamed.landscape.registry.LandscapePoiTypeTags;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Ant extends Animal implements NeutralMob {
    public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966F);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final int LEAVES_TO_COLLECT_BEFORE_RETURN = 5;

    @Nullable
    private UUID persistentAngerTarget;
    private int collectedLeavesCount = 0;
    private float rollAmount;
    private float rollAmountO;
    private int timeSinceSting;
    int ticksWithoutLeavesSinceExitingNest;
    private int stayOutOfNestCountdown;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_NEST = 40; // was 200
    int remainingCooldownBeforeLocatingNewNest = 0;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
    int remainingCooldownBeforeLocatingNewLeaves = Mth.nextInt(this.random, 20, 60);
    @Nullable
    BlockPos savedLeavesPos;
    @Nullable
    BlockPos nestPos;
    Ant.AntCollectLeavesGoal antCollectLeavesGoal;
    Ant.AntGoToNestGoal goToNestGoal;
    private Ant.AntGoToKnownLeavesGoal goToKnownLeavesGoal;
    private int underWaterTicks;

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public Ant(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
        this.lookControl = new Ant.AntLookControl(this);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Ant.AntAttackGoal(this, 1.4F, true));
        this.goalSelector.addGoal(1, new Ant.AntEnterNestGoal());
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, p_335831_ -> p_335831_.is(ItemTags.LEAVES), false));
        this.antCollectLeavesGoal = new Ant.AntCollectLeavesGoal();
        this.goalSelector.addGoal(3, this.antCollectLeavesGoal);
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(4, new Ant.AntLocateNestGoal());
        this.goToNestGoal = new Ant.AntGoToNestGoal();
        this.goalSelector.addGoal(4, this.goToNestGoal);
        this.goToKnownLeavesGoal = new Ant.AntGoToKnownLeavesGoal();
        this.goalSelector.addGoal(5, this.goToKnownLeavesGoal);
        this.goalSelector.addGoal(7, new Ant.AntWanderGoal());
        this.goalSelector.addGoal(8, new FloatGoal(this));
        this.targetSelector.addGoal(1, new Ant.AntHurtByOtherGoal(this).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new Ant.AntBecomeAngryTargetGoal(this));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.hasNest()) {
            compound.put("nest_pos", NbtUtils.writeBlockPos(this.getNestPos()));
        }

        if (this.hasSavedLeavesPos()) {
            compound.put("leaves_pos", NbtUtils.writeBlockPos(this.getSavedLeavesPos()));
        }

        compound.putBoolean("HasEnoughLeaves", this.hasEnoughLeaves());
        compound.putInt("TicksSinceCollectLeaves", this.ticksWithoutLeavesSinceExitingNest);
        compound.putInt("CannotEnterNestTicks", this.stayOutOfNestCountdown);
        compound.putInt("CollectedLeaves", this.collectedLeavesCount);

        this.addPersistentAngerSaveData(compound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.nestPos = NbtUtils.readBlockPos(compound, "nest_pos").orElse(null);
        this.savedLeavesPos = NbtUtils.readBlockPos(compound, "leaves_pos").orElse(null);
        super.readAdditionalSaveData(compound);
        this.setHasEnoughLeaves(compound.getBoolean("HasEnoughLeaves"));
        this.setHasStung(compound.getBoolean("HasStung"));
        this.ticksWithoutLeavesSinceExitingNest = compound.getInt("TicksSinceCollectLeaves");
        this.stayOutOfNestCountdown = compound.getInt("CannotEnterNestTicks");
        this.collectedLeavesCount = compound.getInt("CollectedLeaves");
        this.readPersistentAngerSaveData(this.level(), compound);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }


    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    void pathfindRandomlyTowards(BlockPos pos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(pos);
        int i = 0;
        BlockPos blockpos = this.blockPosition();
        int j = (int)vec3.y - blockpos.getY();
        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int i1 = blockpos.distManhattan(pos);
        if (i1 < 15) {
            k = i1 / 2;
            l = i1 / 2;
        }

        Vec3 vec31 = AirRandomPos.getPosTowards(this, k, l, i, vec3, (float) (Math.PI / 10));
        if (vec31 != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(vec31.x, vec31.y, vec31.z, 1.0);
        }
    }

    @Nullable
    public BlockPos getSavedLeavesPos() {
        return this.savedLeavesPos;
    }

    public boolean hasSavedLeavesPos() {
        return this.savedLeavesPos != null;
    }

    public void setSavedLeavesPos(BlockPos savedLeavesPos) {
        this.savedLeavesPos = savedLeavesPos;
    }

    @VisibleForDebug
    public int getTravellingTicks() {
        return Math.max(this.goToNestGoal.travellingTicks, this.goToKnownLeavesGoal.travellingTicks);
    }

    @VisibleForDebug
    public List<BlockPos> getBlacklistedNests() {
        return this.goToNestGoal.blacklistedTargets;
    }

    private boolean isTiredOfLookingForLeaves() {
        return this.ticksWithoutLeavesSinceExitingNest > 3600;
    }

    boolean wantsToEnterNest() {
        if (this.stayOutOfNestCountdown <= 0 && !this.antCollectLeavesGoal.isCollectLeavesing() && !this.hasStung() && this.getTarget() == null) {
            boolean flag = this.isTiredOfLookingForLeaves() || this.level().isRaining() || this.level().isNight() || this.hasEnoughLeaves();
            return flag && !this.isNestNearFire();
        } else {
            return false;
        }
    }

    public void setStayOutOfNestCountdown(int stayOutOfNestCountdown) {
        this.stayOutOfNestCountdown = stayOutOfNestCountdown;
    }

    public float getRollAmount(float partialTick) {
        return Mth.lerp(partialTick, this.rollAmountO, this.rollAmount);
    }


    @Override
    protected void customServerAiStep() {
        boolean flag = this.hasStung();
        if (this.isInWaterOrBubble()) {
            this.underWaterTicks++;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }

        if (flag) {
            this.timeSinceSting++;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurt(this.damageSources().generic(), this.getHealth());
            }
        }

        if (!this.hasEnoughLeaves()) {
            this.ticksWithoutLeavesSinceExitingNest++;
        }

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), false);
        }
    }

    public void resetTicksWithoutLeavesSinceExitingNest() {
        this.ticksWithoutLeavesSinceExitingNest = 0;
    }

    private boolean isNestNearFire() {
        if (this.nestPos == null) {
            return false;
        } else {
            BlockEntity blockentity = this.level().getBlockEntity(this.nestPos);
            return blockentity instanceof AntNestBlockEntity && ((AntNestBlockEntity)blockentity).isFireNearby();
        }
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    private boolean doesNestHaveSpace(BlockPos nestPos) {
        BlockEntity blockentity = this.level().getBlockEntity(nestPos);
        return blockentity instanceof AntNestBlockEntity ? !((AntNestBlockEntity)blockentity).isFull() : false;
    }

    @VisibleForDebug
    public boolean hasNest() {
        return this.nestPos != null;
    }

    @Nullable
    @VisibleForDebug
    public BlockPos getNestPos() {
        return this.nestPos;
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.stayOutOfNestCountdown > 0) {
                this.stayOutOfNestCountdown--;
            }

            if (this.remainingCooldownBeforeLocatingNewNest > 0) {
                this.remainingCooldownBeforeLocatingNewNest--;
            }

            if (this.remainingCooldownBeforeLocatingNewLeaves > 0) {
                this.remainingCooldownBeforeLocatingNewLeaves--;
            }

            boolean flag = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
            this.setRolling(flag);
            if (this.tickCount % 20 == 0 && !this.isNestValid()) {
                this.nestPos = null;
            }
        }
    }

    boolean isNestValid() {
        if (!this.hasNest()) {
            return false;
        } else if (this.isTooFarAway(this.nestPos)) {
            return false;
        } else {
            BlockEntity blockentity = this.level().getBlockEntity(this.nestPos);
            return blockentity instanceof AntNestBlockEntity;
        }
    }

    public boolean hasEnoughLeaves() {
        return this.getFlag(8);
    }

    void setHasEnoughLeaves(boolean hasEnoughLeaves) {
        if (hasEnoughLeaves) {
            this.resetTicksWithoutLeavesSinceExitingNest();
        }

        this.setFlag(8, hasEnoughLeaves);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    private void setHasStung(boolean hasStung) {
        this.setFlag(4, hasStung);
    }

    private boolean isRolling() {
        return this.getFlag(2);
    }

    private void setRolling(boolean isRolling) {
        this.setFlag(2, isRolling);
    }

    boolean isTooFarAway(BlockPos pos) {
        return !this.closerThan(pos, 32);
    }

    private void setFlag(int flagId, boolean value) {
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | flagId));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~flagId));
        }
    }

    private boolean getFlag(int flagId) {
        return (this.entityData.get(DATA_FLAGS_ID) & flagId) != 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation groundNav = new GroundPathNavigation(this, level);
        groundNav.setCanOpenDoors(false);
        groundNav.setCanFloat(false);
        groundNav.setCanPassDoors(true);
        return groundNav;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    boolean isLeavesValid(BlockPos pos) {
        return this.level().isLoaded(pos) && this.level().getBlockState(pos).is(BlockTags.LEAVES);
    }


    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Nullable
    public Ant getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }
    
    public void dropOffLeaves() {
        this.setHasEnoughLeaves(false);
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.antCollectLeavesGoal.stopCollectLeavesing();
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    @Deprecated // FORGE: use jumpInFluid instead
    protected void jumpInLiquid(TagKey<Fluid> fluidTag) {
        this.jumpInLiquidInternal();
    }

    private void jumpInLiquidInternal() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
    }

    @Override
    public void jumpInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        this.jumpInLiquidInternal();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
    }

    boolean closerThan(BlockPos pos, int distance) {
        return pos.closerThan(this.blockPosition(), (double)distance);
    }

    public void setNestPos(BlockPos nestPos) {
        this.nestPos = nestPos;
    }

    abstract class BaseAntGoal extends Goal {
        public abstract boolean canAntUse();

        public abstract boolean canAntContinueToUse();

        @Override
        public boolean canUse() {
            return this.canAntUse() && !Ant.this.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canAntContinueToUse() && !Ant.this.isAngry();
        }
    }

    class AntAttackGoal extends MeleeAttackGoal {
        AntAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && Ant.this.isAngry() && !Ant.this.hasStung();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Ant.this.isAngry() && !Ant.this.hasStung();
        }
    }

    static class AntBecomeAngryTargetGoal extends NearestAttackableTargetGoal<Player> {
        AntBecomeAngryTargetGoal(Ant mob) {
            super(mob, Player.class, 10, true, false, mob::isAngryAt);
        }

        @Override
        public boolean canUse() {
            return this.beeCanTarget() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            boolean flag = this.beeCanTarget();
            if (flag && this.mob.getTarget() != null) {
                return super.canContinueToUse();
            } else {
                this.targetMob = null;
                return false;
            }
        }

        private boolean beeCanTarget() {
            Ant bee = (Ant)this.mob;
            return bee.isAngry() && !bee.hasStung();
        }
    }

    class AntEnterNestGoal extends Ant.BaseAntGoal {
        @Override
        public boolean canAntUse() {
            if (Ant.this.hasNest()
                    && Ant.this.wantsToEnterNest()
                    && Ant.this.nestPos.closerToCenterThan(Ant.this.position(), 2.0)
                    && Ant.this.level().getBlockEntity(Ant.this.nestPos) instanceof AntNestBlockEntity antNestBlockEntity) {
                if (!antNestBlockEntity.isFull()) {
                    return true;
                }

                Ant.this.nestPos = null;
            }

            return false;
        }

        @Override
        public boolean canAntContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            if (Ant.this.level().getBlockEntity(Ant.this.nestPos) instanceof AntNestBlockEntity antNestBlockEntity) {
                antNestBlockEntity.addOccupant(Ant.this);

                Ant.this.collectedLeavesCount = 0;
                Ant.this.setHasEnoughLeaves(false);
            }

        }
    }

    @VisibleForDebug
    public class AntGoToNestGoal extends Ant.BaseAntGoal {
        public static final int MAX_TRAVELLING_TICKS = 600;
        int travellingTicks = Ant.this.level().random.nextInt(10);
        private static final int MAX_BLACKLISTED_TARGETS = 3;
        final List<BlockPos> blacklistedTargets = Lists.newArrayList();
        @Nullable
        private Path lastPath;
        private static final int TICKS_BEFORE_HIVE_DROP = 60;
        private int ticksStuck;

        AntGoToNestGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canAntUse() {
            return Ant.this.nestPos != null
                    && !Ant.this.hasRestriction()
                    && Ant.this.wantsToEnterNest()
                    && !this.hasReachedTarget(Ant.this.nestPos)
                    && Ant.this.level().getBlockState(Ant.this.nestPos).is(LandscapeBlockTags.ANT_NESTS);
        }

        @Override
        public boolean canAntContinueToUse() {
            return this.canAntUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            Ant.this.navigation.stop();
            Ant.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Ant.this.nestPos != null) {
                this.travellingTicks++;

                if (this.travellingTicks > this.adjustedTickDelay(MAX_TRAVELLING_TICKS)) {
                    this.dropAndBlacklistNest();
                    return;
                }

                if (!Ant.this.navigation.isInProgress()) {
                    if (!Ant.this.closerThan(Ant.this.nestPos, 16)) {
                        if (Ant.this.isTooFarAway(Ant.this.nestPos)) {
                            this.dropNest();
                            return;
                        }
                        Ant.this.pathfindRandomlyTowards(Ant.this.nestPos);
                    } else {
                        boolean success = this.pathfindDirectlyTowards(Ant.this.nestPos);
                        if (!success) {
                            this.ticksStuck++;
                            if (this.ticksStuck > 100) { // was 60
                                this.dropNest();
                                this.ticksStuck = 0;
                            }
                        } else {
                            this.ticksStuck = 0;
                        }
                    }
                }
            }
        }


        private boolean pathfindDirectlyTowards(BlockPos pos) {
            Ant.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
            Ant.this.navigation.moveTo((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 2, 1.0);
            return Ant.this.navigation.getPath() != null && Ant.this.navigation.getPath().canReach();
        }

        boolean isTargetBlacklisted(BlockPos pos) {
            return this.blacklistedTargets.contains(pos);
        }

        private void blacklistTarget(BlockPos pos) {
            this.blacklistedTargets.add(pos);

            while (this.blacklistedTargets.size() > 3) {
                this.blacklistedTargets.remove(0);
            }
        }

        void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        private void dropAndBlacklistNest() {
            if (Ant.this.nestPos != null) {
                this.blacklistTarget(Ant.this.nestPos);
            }

            this.dropNest();
        }

        private void dropNest() {
            Ant.this.nestPos = null;
            Ant.this.remainingCooldownBeforeLocatingNewNest = 200;
        }

        private boolean hasReachedTarget(BlockPos pos) {
            if (Ant.this.closerThan(pos, 2)) {
                return true;
            } else {
                Path path = Ant.this.navigation.getPath();
                return path != null && path.getTarget().equals(pos) && path.canReach() && path.isDone();
            }
        }
    }

    public class AntGoToKnownLeavesGoal extends Ant.BaseAntGoal {
        private static final int MAX_TRAVELLING_TICKS = 600;
        int travellingTicks = Ant.this.level().random.nextInt(10);

        AntGoToKnownLeavesGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canAntUse() {
            return Ant.this.savedLeavesPos != null
                    && !Ant.this.hasRestriction()
                    && this.wantsToGoToKnownLeaves()
                    && Ant.this.isLeavesValid(Ant.this.savedLeavesPos)
                    && !Ant.this.closerThan(Ant.this.savedLeavesPos, 2);
        }

        @Override
        public boolean canAntContinueToUse() {
            return this.canAntUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            Ant.this.navigation.stop();
            Ant.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Ant.this.savedLeavesPos != null) {
                this.travellingTicks++;
                if (this.travellingTicks > this.adjustedTickDelay(600)) {
                    Ant.this.savedLeavesPos = null;
                } else if (!Ant.this.navigation.isInProgress()) {
                    if (Ant.this.isTooFarAway(Ant.this.savedLeavesPos)) {
                        Ant.this.savedLeavesPos = null;
                    } else {
                        Ant.this.pathfindRandomlyTowards(Ant.this.savedLeavesPos);
                    }
                }
            }
        }

        private boolean wantsToGoToKnownLeaves() {
            return Ant.this.ticksWithoutLeavesSinceExitingNest > 2400;
        }
    }

    class AntHurtByOtherGoal extends HurtByTargetGoal {
        AntHurtByOtherGoal(Ant mob) {
            super(mob);
        }

        @Override
        public boolean canContinueToUse() {
            return Ant.this.isAngry() && super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity target) {
            if (mob instanceof Ant && this.mob.hasLineOfSight(target)) {
                mob.setTarget(target);
            }
        }
    }

    class AntLocateNestGoal extends Ant.BaseAntGoal {
        @Override
        public boolean canAntUse() {
            return Ant.this.remainingCooldownBeforeLocatingNewNest == 0 && !Ant.this.hasNest() && Ant.this.wantsToEnterNest();
        }

        @Override
        public boolean canAntContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Ant.this.remainingCooldownBeforeLocatingNewNest = COOLDOWN_BEFORE_LOCATING_NEW_NEST;
            List<BlockPos> list = this.findNearbyNestsWithSpace();
            if (!list.isEmpty()) {
                for (BlockPos pos : list) {
                    if (!Ant.this.goToNestGoal.isTargetBlacklisted(pos)) {
                        Ant.this.nestPos = pos;
                        return;
                    }
                }
                Ant.this.goToNestGoal.clearBlacklist();
                Ant.this.nestPos = list.get(0);
            }
        }


        private List<BlockPos> findNearbyNestsWithSpace() {
            BlockPos blockpos = Ant.this.blockPosition();
            PoiManager poimanager = ((ServerLevel) Ant.this.level()).getPoiManager();
            Stream<PoiRecord> stream = poimanager.getInRange(p_218130_ -> p_218130_.is(LandscapePoiTypeTags.ANT_HOME), blockpos, 20, PoiManager.Occupancy.ANY);
            return stream.map(PoiRecord::getPos)
                    .filter(Ant.this::doesNestHaveSpace)
                    .sorted(Comparator.comparingDouble(p_148811_ -> p_148811_.distSqr(blockpos)))
                    .collect(Collectors.toList());
        }
    }

    class AntLookControl extends LookControl {
        AntLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (!Ant.this.isAngry()) {
                super.tick();
            }
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Ant.this.antCollectLeavesGoal.isCollectLeavesing();
        }
    }

    class AntCollectLeavesGoal extends Ant.BaseAntGoal {
        private static final int MIN_POLLINATION_TICKS = 400;
        private static final int MIN_FIND_FLOWER_RETRY_COOLDOWN = 20;
        private static final int MAX_FIND_FLOWER_RETRY_COOLDOWN = 60;
        private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = p_28074_ -> {
            if (p_28074_.hasProperty(BlockStateProperties.WATERLOGGED) && p_28074_.getValue(BlockStateProperties.WATERLOGGED)) {
                return false;
            } else if (p_28074_.is(BlockTags.LEAVES)) {
                return true;
            } else {
                return false;
            }
        };
        private static final double ARRIVAL_THRESHOLD = 0.1;
        private static final int POSITION_CHANGE_CHANCE = 25;
        private static final float SPEED_MODIFIER = 0.35F;
        private static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.6F;
        private static final float HOVER_POS_OFFSET = 0.33333334F;
        private int successfulCollectingLeavesTicks;
        private int lastSoundPlayedTick;
        private boolean collectingLeaves;
        @Nullable
        private Vec3 hoverPos;
        private int collectingLeavesTicks;
        private static final int MAX_POLLINATING_TICKS = 600;

        AntCollectLeavesGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canAntUse() {
            if (Ant.this.remainingCooldownBeforeLocatingNewLeaves > 0) {
                return false;
            } else if (Ant.this.hasEnoughLeaves()) {
                return false;
            } else if (Ant.this.level().isRaining()) {
                return false;
            } else {
                Optional<BlockPos> optional = this.findNearbyLeaves();
                if (optional.isPresent()) {
                    Ant.this.savedLeavesPos = optional.get();
                    Ant.this.navigation
                            .moveTo(
                                    (double) Ant.this.savedLeavesPos.getX() + 0.5,
                                    (double) Ant.this.savedLeavesPos.getY() + 0.5,
                                    (double) Ant.this.savedLeavesPos.getZ() + 0.5,
                                    1.2F
                            );
                    return true;
                } else {
                    Ant.this.remainingCooldownBeforeLocatingNewLeaves = Mth.nextInt(Ant.this.random, 20, 60);
                    return false;
                }
            }
        }

        @Override
        public boolean canAntContinueToUse() {
            if (!this.collectingLeaves) {
                return false;
            } else if (!Ant.this.hasSavedLeavesPos()) {
                return false;
            } else if (Ant.this.level().isRaining()) {
                return false;
            } else if (this.hasCollectedLeavesLongEnough()) {
                return Ant.this.random.nextFloat() < 0.2F;
            } else if (Ant.this.tickCount % 20 == 0 && !Ant.this.isLeavesValid(Ant.this.savedLeavesPos)) {
                Ant.this.savedLeavesPos = null;
                return false;
            } else {
                return true;
            }
        }

        private boolean hasCollectedLeavesLongEnough() {
            return this.successfulCollectingLeavesTicks > 400;
        }

        boolean isCollectLeavesing() {
            return this.collectingLeaves;
        }

        void stopCollectLeavesing() {
            this.collectingLeaves = false;
        }

        @Override
        public void start() {
            this.successfulCollectingLeavesTicks = 0;
            this.collectingLeavesTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.collectingLeaves = true;
            Ant.this.resetTicksWithoutLeavesSinceExitingNest();
        }

        @Override
        public void stop() {
            if (this.hasCollectedLeavesLongEnough()) {
                Ant.this.setHasEnoughLeaves(true);
                Ant.this.collectedLeavesCount++;

                if (Ant.this.savedLeavesPos != null && Ant.this.level().getBlockState(Ant.this.savedLeavesPos).is(BlockTags.LEAVES)) {
                    Ant.this.level().destroyBlock(Ant.this.savedLeavesPos, false);
                }

                if (Ant.this.collectedLeavesCount >= LEAVES_TO_COLLECT_BEFORE_RETURN) {
                    Ant.this.setHasEnoughLeaves(true);
                } else {
                    Ant.this.setHasEnoughLeaves(false);
                }
            }
            this.collectingLeaves = false;
            Ant.this.navigation.stop();
            Ant.this.remainingCooldownBeforeLocatingNewLeaves = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.collectingLeavesTicks++;

            if (Ant.this.savedLeavesPos == null) {
                this.collectingLeaves = false;
                return;
            }

            BlockState state = Ant.this.level().getBlockState(Ant.this.savedLeavesPos);
            if (!state.is(BlockTags.LEAVES)) {
                Ant.this.savedLeavesPos = null;
                this.collectingLeaves = false;
                return;
            }

            Vec3 targetVec = Vec3.atBottomCenterOf(Ant.this.savedLeavesPos).add(0, 0.6F, 0);

            Ant.this.getMoveControl().setWantedPosition(targetVec.x(), targetVec.y(), targetVec.z(), 0.35F);
            Ant.this.getLookControl().setLookAt(targetVec.x(), targetVec.y(), targetVec.z());

            if (Ant.this.position().distanceTo(targetVec) < 1.0D) {
                Ant.this.level().destroyBlock(Ant.this.savedLeavesPos, false);
                Ant.this.collectedLeavesCount++;

                if (Ant.this.collectedLeavesCount >= LEAVES_TO_COLLECT_BEFORE_RETURN) {
                    Ant.this.setHasEnoughLeaves(true);
                } else {
                    Ant.this.setHasEnoughLeaves(false);
                }

                Ant.this.savedLeavesPos = null;
                Ant.this.navigation.stop();
                this.collectingLeaves = false;
                Ant.this.remainingCooldownBeforeLocatingNewLeaves = 0;

                Ant.this.goalSelector.tick();
            }
        }



        private void setWantedPos() {
            Ant.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35F);
        }

        private float getOffset() {
            return (Ant.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
        }

        private Optional<BlockPos> findNearbyLeaves() {
            return this.findNearestBlock(this.VALID_POLLINATION_BLOCKS, 5.0);
        }

        private Optional<BlockPos> findNearestBlock(Predicate<BlockState> predicate, double distance) {
            BlockPos blockpos = Ant.this.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int i = 0; (double)i <= distance; i = i > 0 ? -i : 1 - i) {
                for (int j = 0; (double)j < distance; j++) {
                    for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                        for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                            blockpos$mutableblockpos.setWithOffset(blockpos, k, i - 1, l);
                            if (blockpos.closerThan(blockpos$mutableblockpos, distance)
                                    && predicate.test(Ant.this.level().getBlockState(blockpos$mutableblockpos))) {
                                return Optional.of(blockpos$mutableblockpos);
                            }
                        }
                    }
                }
            }

            return Optional.empty();
        }
    }


    class AntWanderGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        AntWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Ant.this.navigation.isDone() && Ant.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return Ant.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                Ant.this.navigation.moveTo(Ant.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3;
            if (Ant.this.isNestValid() && !Ant.this.closerThan(Ant.this.nestPos, 22)) {
                Vec3 vec31 = Vec3.atCenterOf(Ant.this.nestPos);
                vec3 = vec31.subtract(Ant.this.position()).normalize();
            } else {
                vec3 = Ant.this.getViewVector(0.0F);
            }

            int i = 8;
            Vec3 vec32 = HoverRandomPos.getPos(Ant.this, 8, 7, vec3.x, vec3.z, (float) (Math.PI / 2), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(Ant.this, 8, 4, -2, vec3.x, vec3.z, (float) (Math.PI / 2));
        }
    }
}

