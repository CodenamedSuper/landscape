package com.codenamed.landscape.block.entity;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.block.AntNestBlock;
import com.codenamed.landscape.entity.Ant;
import com.codenamed.landscape.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;


public class AntNestBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_LEAVES_POS = "leaves_pos";
    private static final String ANTS = "ants";
    static final List<String> IGNORED_ANT_TAGS = Arrays.asList(
            "Air",
            "ArmorDropChances",
            "ArmorItems",
            "Brain",
            "CanPickUpLoot",
            "DeathTime",
            "FallDistance",
            "FallFlying",
            "Fire",
            "HandDropChances",
            "HandItems",
            "HurtByTimestamp",
            "HurtTime",
            "LeftHanded",
            "Motion",
            "NoGravity",
            "OnGround",
            "PortalCooldown",
            "Pos",
            "Rotation",
            "SleepingX",
            "SleepingY",
            "SleepingZ",
            "CannotEnterHiveTicks",
            "TicksSincePollination",
            "CropsGrownSincePollination",
            "hive_pos",
            "Passengers",
            "leash",
            "UUID"
    );
    public static final int MAX_OCCUPANTS = 3;
    private final List<AntNestBlockEntity.AntData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedLeavesPos;

    public AntNestBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandscapeBlockEntities.ANT_NEST.get(), pos, blockState);
    }

    @Override
    public void setChanged() {
        if (this.isFireNearby()) {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), AntNestBlockEntity.AntReleaseStatus.EMERGENCY);
        }

        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        } else {
            for (BlockPos blockpos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
                if (this.level.getBlockState(blockpos).getBlock() instanceof FireBlock) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player player, BlockState state, AntNestBlockEntity.AntReleaseStatus releaseStatus) {
        List<Entity> list = this.releaseAllOccupants(state, releaseStatus);
        if (player != null) {
            for (Entity entity : list) {
                if (entity instanceof Ant) {
                    Ant ant = (Ant)entity;
                    if (player.position().distanceToSqr(entity.position()) <= 16.0) {
                        if (!this.isSedated()) {
                            ant.setTarget(player);
                        } else {
                            ant.setStayOutOfNestCountdown(400);
                        }
                    }
                }
            }
        }
    }

    private List<Entity> releaseAllOccupants(BlockState state, AntNestBlockEntity.AntReleaseStatus releaseStatus) {
        List<Entity> list = Lists.newArrayList();
        this.stored
                .removeIf(p_330138_ -> releaseOccupant(this.level, this.worldPosition, state, p_330138_.toOccupant(), list, releaseStatus, this.savedLeavesPos));
        if (!list.isEmpty()) {
            super.setChanged();
        }

        return list;
    }

    @VisibleForDebug
    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getNurseryLevel(BlockState state) {
        return state.getValue(AntNestBlock.NURSERY_AGE);
    }

    @VisibleForDebug
    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public void addOccupant(Entity occupant) {
        if (this.stored.size() < 3) {
            occupant.stopRiding();
            occupant.ejectPassengers();
            this.storeAnt(AntNestBlockEntity.Occupant.of(occupant));
            if (this.level != null) {
                if (occupant instanceof Ant ant && ant.hasSavedLeavesPos() && (!this.hasSavedLeavesPos() || this.level.random.nextBoolean())) {
                    this.savedLeavesPos = ant.getSavedLeavesPos();
                }

                BlockPos blockpos = this.getBlockPos();
                this.level
                        .playSound(
                                null,
                                (double)blockpos.getX(),
                                (double)blockpos.getY(),
                                (double)blockpos.getZ(),
                                SoundEvents.BEEHIVE_ENTER,
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F
                        );
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(occupant, this.getBlockState()));
            }

            occupant.discard();
            super.setChanged();
        }
    }

    public void storeAnt(AntNestBlockEntity.Occupant occupant) {
        this.stored.add(new AntNestBlockEntity.AntData(occupant));
    }

    private static boolean releaseOccupant(
            Level level,
            BlockPos pos,
            BlockState state,
            AntNestBlockEntity.Occupant occupant,
            @Nullable List<Entity> storedInNests,
            AntNestBlockEntity.AntReleaseStatus releaseStatus,
            @Nullable BlockPos storedLeavesPos
    ) {
        if ((level.isNight() || level.isRaining()) && releaseStatus != AntNestBlockEntity.AntReleaseStatus.EMERGENCY) {
            return false;
        }

        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        BlockPos releasePos = null;

        for (Direction direction : directions) {
            BlockPos candidate = pos.relative(direction);
            if (level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()) {
                releasePos = candidate;
                break;
            }
        }

        if (releasePos == null && releaseStatus != AntNestBlockEntity.AntReleaseStatus.EMERGENCY) {
            return false;
        }

        if (releasePos == null) {
            releasePos = pos;
        }

        Entity entity = occupant.createEntity(level, pos);
        if (entity instanceof Ant ant) {
            if (storedLeavesPos != null && !ant.hasSavedLeavesPos() && level.random.nextFloat() < 0.9F) {
                ant.setSavedLeavesPos(storedLeavesPos);
            }

            if (releaseStatus == AntNestBlockEntity.AntReleaseStatus.LEAVES_FED) {
                ant.dropOffLeaves();
                if (state.is(LandscapeBlockTags.ANT_NESTS, p -> p.hasProperty(AntNestBlock.NURSERY_AGE))) {
                    int i = getNurseryLevel(state);
                    if (i < 5) {
                        int j = level.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) {
                            j--;
                        }
                        level.setBlockAndUpdate(pos, state.setValue(AntNestBlock.NURSERY_AGE, i + j));
                    }
                }
            }

            if (storedInNests != null) {
                storedInNests.add(ant);
            }
        }

        entity.moveTo(releasePos.getX() + 0.5, releasePos.getY(), releasePos.getZ() + 0.5, entity.getYRot(), entity.getXRot());



        level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, level.getBlockState(pos)));
        return level.addFreshEntity(entity);
    }


    private boolean hasSavedLeavesPos() {
        return this.savedLeavesPos != null;
    }

    private static void tickOccupants(
            Level level, BlockPos pos, BlockState state, List<AntNestBlockEntity.AntData> data, @Nullable BlockPos savedFlowerPos
    ) {
        boolean flag = false;
        Iterator<AntNestBlockEntity.AntData> iterator = data.iterator();

        while (iterator.hasNext()) {
            AntNestBlockEntity.AntData antNestblockentity$antdata = iterator.next();
            if (antNestblockentity$antdata.tick()) {
                AntNestBlockEntity.AntReleaseStatus antNestblockentity$antreleasestatus = antNestblockentity$antdata.hasEnoughLeaves()
                        ? AntNestBlockEntity.AntReleaseStatus.LEAVES_FED
                        : AntNestBlockEntity.AntReleaseStatus.BEE_RELEASED;
                if (releaseOccupant(
                        level, pos, state, antNestblockentity$antdata.toOccupant(), null, antNestblockentity$antreleasestatus, savedFlowerPos
                )) {
                    flag = true;
                    iterator.remove();
                }
            }
        }

        if (flag) {
            setChanged(level, pos, state);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AntNestBlockEntity antNest) {
        tickOccupants(level, pos, state, antNest.stored, antNest.savedLeavesPos);
        if (!antNest.stored.isEmpty() && level.getRandom().nextDouble() < 0.005) {
            double d0 = (double)pos.getX() + 0.5;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5;
            level.playSound(null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.stored.clear();
        if (tag.contains("ants")) {
            AntNestBlockEntity.Occupant.LIST_CODEC
                    .parse(NbtOps.INSTANCE, tag.get("ants"))
                    .resultOrPartial(p_330133_ -> LOGGER.error("Failed to parse ants: '{}'", p_330133_))
                    .ifPresent(p_330134_ -> p_330134_.forEach(this::storeAnt));
        }

        this.savedLeavesPos = NbtUtils.readBlockPos(tag, "leaves_pos").orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ants", AntNestBlockEntity.Occupant.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.getAnts()).getOrThrow());
        if (this.hasSavedLeavesPos()) {
            tag.put("leaves_pos", NbtUtils.writeBlockPos(this.savedLeavesPos));
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.stored.clear();
        List<AntNestBlockEntity.Occupant> list = componentInput.getOrDefault(LandscapeDataComponentTypes.ANTS, List.of());
        list.forEach(this::storeAnt);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(LandscapeDataComponentTypes.ANTS, this.getAnts());
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("ants");
    }

    private List<AntNestBlockEntity.Occupant> getAnts() {
        return this.stored.stream().map(AntNestBlockEntity.AntData::toOccupant).toList();
    }

    static class AntData {
        private final AntNestBlockEntity.Occupant occupant;
        private int ticksInHive;

        AntData(AntNestBlockEntity.Occupant occupant) {
            this.occupant = occupant;
            this.ticksInHive = occupant.ticksInNest();
        }

        public boolean tick() {
            return this.ticksInHive++ > this.occupant.minTicksInHive;
        }

        public AntNestBlockEntity.Occupant toOccupant() {
            return new AntNestBlockEntity.Occupant(this.occupant.entityData, this.ticksInHive, this.occupant.minTicksInHive);
        }

        public boolean hasEnoughLeaves() {
            return this.occupant.entityData.getUnsafe().getBoolean("HasEnoughLeaves");
        }
    }

    public static enum AntReleaseStatus {
        LEAVES_FED,
        BEE_RELEASED,
        EMERGENCY;
    }

    public static record Occupant(CustomData entityData, int ticksInNest, int minTicksInHive) {
        public static final Codec<AntNestBlockEntity.Occupant> CODEC = RecordCodecBuilder.create(
                p_337984_ -> p_337984_.group(
                                CustomData.CODEC.optionalFieldOf("entity_data", CustomData.EMPTY).forGetter(AntNestBlockEntity.Occupant::entityData),
                                Codec.INT.fieldOf("ticks_in_nest").forGetter(AntNestBlockEntity.Occupant::ticksInNest),
                                Codec.INT.fieldOf("min_ticks_in_nest").forGetter(AntNestBlockEntity.Occupant::minTicksInHive)
                        )
                        .apply(p_337984_, AntNestBlockEntity.Occupant::new)
        );
        public static final Codec<List<AntNestBlockEntity.Occupant>> LIST_CODEC = CODEC.listOf();
        public static final StreamCodec<ByteBuf, AntNestBlockEntity.Occupant> STREAM_CODEC = StreamCodec.composite(
                CustomData.STREAM_CODEC,
                AntNestBlockEntity.Occupant::entityData,
                ByteBufCodecs.VAR_INT,
                AntNestBlockEntity.Occupant::ticksInNest,
                ByteBufCodecs.VAR_INT,
                AntNestBlockEntity.Occupant::minTicksInHive,
                AntNestBlockEntity.Occupant::new
        );

        public static AntNestBlockEntity.Occupant of(Entity entity) {
            CompoundTag compoundtag = new CompoundTag();
            entity.save(compoundtag);
            AntNestBlockEntity.IGNORED_ANT_TAGS.forEach(compoundtag::remove);
            boolean flag = compoundtag.getBoolean("HasEnoughLeaves");
            return new AntNestBlockEntity.Occupant(CustomData.of(compoundtag), 0, flag ? 2400 : 600);
        }

        public static AntNestBlockEntity.Occupant create(int ticksInHive) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(LandscapeEntities.ANT.get()).toString());
            return new AntNestBlockEntity.Occupant(CustomData.of(compoundtag), ticksInHive, 600);
        }

        @Nullable
        public Entity createEntity(Level level, BlockPos pos) {
            CompoundTag compoundtag = this.entityData.copyTag();
            AntNestBlockEntity.IGNORED_ANT_TAGS.forEach(compoundtag::remove);
            Entity entity = EntityType.loadEntityRecursive(compoundtag, level, p_331097_ -> p_331097_);
            if (entity != null && entity.getType().is(LandscapeEntityTypeTags.ANT_NEST_INHABITORS)) {
                if (entity instanceof Ant ant) {
                    ant.setNestPos(pos);
                    setAntReleaseData(this.ticksInNest, ant);
                }
                return entity;
            } else {
                return null;
            }
        }

        private static void setAntReleaseData(int ticksInHive, Ant ant) {
            int i = ant.getAge();
            if (i < 0) {
                ant.setAge(Math.min(0, i + ticksInHive));
            } else if (i > 0) {
                ant.setAge(Math.max(0, i - ticksInHive));
            }

            ant.setInLoveTime(Math.max(0, ant.getInLoveTime() - ticksInHive));
        }
    }
}
