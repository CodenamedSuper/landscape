package com.codenamed.landscape.block;

import com.codenamed.landscape.block.entity.AntNestBlockEntity;
import com.codenamed.landscape.entity.Ant;
import com.codenamed.landscape.registry.LandscapeBlockEntities;
import com.codenamed.landscape.registry.LandscapeItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;

public class AntNestBlock extends BaseEntityBlock {
    public static final MapCodec<AntNestBlock> CODEC = simpleCodec(AntNestBlock::new);
    public static final IntegerProperty NURSERY_AGE = IntegerProperty.create("nursery_age", 0, 5);;
    public static final int MAX_NURSERY_AGE = 3;

    public MapCodec<AntNestBlock> codec() {
        return CODEC;
    }

    public AntNestBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NURSERY_AGE, 0)));
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return (Integer)blockState.getValue(NURSERY_AGE);
    }

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @javax.annotation.Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, te, stack);
        if (!level.isClientSide && te instanceof AntNestBlockEntity antNestBlockEntity) {
            if (!EnchantmentHelper.hasTag(stack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
                antNestBlockEntity.emptyAllLivingFromHive(player, state, AntNestBlockEntity.AntReleaseStatus.EMERGENCY);
                level.updateNeighbourForOutputSignal(pos, this);
                this.angerNearbyAnts(level, pos);
            }

            CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)player, state, stack, antNestBlockEntity.getOccupantCount());
        }

    }

    private void angerNearbyAnts(Level level, BlockPos pos) {
        AABB aabb = (new AABB(pos)).inflate((double)8.0F, (double)6.0F, (double)8.0F);
        List<Ant> list = level.getEntitiesOfClass(Ant.class, aabb);
        if (!list.isEmpty()) {
            List<Player> list1 = level.getEntitiesOfClass(Player.class, aabb);
            if (list1.isEmpty()) {
                return;
            }

            for(Ant ant : list) {
                if (ant.getTarget() == null) {
                    Player player = (Player)Util.getRandom(list1, level.random);
                    ant.setTarget(player);
                }
            }
        }

    }

    public static void dropAntEggs(Level level, BlockPos pos) {
        popResource(level, pos, new ItemStack(LandscapeItems.ANT_EGGS.get(), 3));
    }

    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int i = (Integer) state.getValue(NURSERY_AGE);
        boolean flag = false;
        if (i >= 5) {
            Item item = stack.getItem();
            if (stack.canPerformAction(ItemAbilities.BRUSH_BRUSH)) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BRUSH_SAND, SoundSource.BLOCKS, 1.0F, 1.0F);
                dropAntEggs(level, pos);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                flag = true;
                level.gameEvent(player, GameEvent.SHEAR, pos);

                if (!level.isClientSide() && flag) {
                    player.awardStat(Stats.ITEM_USED.get(item));
                }
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);

    }

    private boolean hiveContainsAnts(Level level, BlockPos pos) {
        BlockEntity var4 = level.getBlockEntity(pos);
        boolean var10000;
        if (var4 instanceof AntNestBlockEntity anthiveblockentity) {
            var10000 = !anthiveblockentity.isEmpty();
        } else {
            var10000 = false;
        }

        return var10000;
    }

    public void releaseAntsAndResetNurseryLevel(Level level, BlockState state, BlockPos pos, @javax.annotation.Nullable Player player, AntNestBlockEntity.AntReleaseStatus antReleaseStatus) {
        this.resetNurseryAge(level, state, pos);
        BlockEntity var7 = level.getBlockEntity(pos);
        if (var7 instanceof AntNestBlockEntity anthiveblockentity) {
            anthiveblockentity.emptyAllLivingFromHive(player, state, antReleaseStatus);
        }

    }

    public void resetNurseryAge(Level level, BlockState state, BlockPos pos) {
        level.setBlock(pos, (BlockState)state.setValue(NURSERY_AGE, 0), 3);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{NURSERY_AGE});
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @javax.annotation.Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AntNestBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, LandscapeBlockEntities.ANT_NEST.get(), AntNestBlockEntity::serverTick);
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AntNestBlockEntity) {
                AntNestBlockEntity anthiveblockentity = (AntNestBlockEntity)blockEntity;
                int i = (Integer)state.getValue(NURSERY_AGE);
                boolean flag = !anthiveblockentity.isEmpty();
                if (flag || i > 0) {
                    ItemStack itemstack = new ItemStack(this);
                    itemstack.applyComponents(anthiveblockentity.collectComponents());
                    itemstack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(NURSERY_AGE, i));
                    ItemEntity itementity = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemstack);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Entity entity = (Entity)params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof WitherSkull || entity instanceof WitherBoss || entity instanceof MinecartTNT) {
            BlockEntity blockentity = (BlockEntity)params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (blockentity instanceof AntNestBlockEntity) {
                AntNestBlockEntity anthiveblockentity = (AntNestBlockEntity)blockentity;
                anthiveblockentity.emptyAllLivingFromHive((Player)null, state, AntNestBlockEntity.AntReleaseStatus.EMERGENCY);
            }
        }

        return super.getDrops(state, params);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (level.getBlockState(facingPos).getBlock() instanceof FireBlock) {
            BlockEntity var8 = level.getBlockEntity(currentPos);
            if (var8 instanceof AntNestBlockEntity) {
                AntNestBlockEntity anthiveblockentity = (AntNestBlockEntity)var8;
                anthiveblockentity.emptyAllLivingFromHive((Player)null, state, AntNestBlockEntity.AntReleaseStatus.EMERGENCY);
            }
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }




}