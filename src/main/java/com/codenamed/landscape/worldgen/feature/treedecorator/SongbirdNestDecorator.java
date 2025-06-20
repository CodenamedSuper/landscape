package com.codenamed.landscape.worldgen.feature.treedecorator;

import com.codenamed.landscape.registry.LandscapeBlocks;
import com.codenamed.landscape.registry.LandscapeTreeDecoratorType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class SongbirdNestDecorator extends TreeDecorator {
    public static final MapCodec<SongbirdNestDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(SongbirdNestDecorator::new, (p_69971_) -> p_69971_.probability);
    private static final Direction WORLDGEN_FACING;
    private static final Direction[] SPAWN_DIRECTIONS;
    private final float probability;

    public SongbirdNestDecorator(float probability) {
        this.probability = probability;
    }

    protected TreeDecoratorType<?> type() {
        return (TreeDecoratorType<?>) LandscapeTreeDecoratorType.SONGBIRD_NEST;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();

        if (random.nextFloat() >= this.probability) {
            return;
        }

        List<BlockPos> leaves = context.leaves();

        leaves.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed());

        BlockPos nestPos = leaves.get(random.nextInt(0, leaves.size())).above();

        if (context.isAir(nestPos)) {

            if (context.isAir(nestPos)) {
                context.setBlock(nestPos, LandscapeBlocks.SONGBIRD_NEST.get().defaultBlockState());
            }

        }
    }




    static {
        WORLDGEN_FACING = Direction.SOUTH;
        SPAWN_DIRECTIONS = (Direction[])Plane.HORIZONTAL.stream().filter((p_202307_) -> p_202307_ != WORLDGEN_FACING.getOpposite()).toArray((x$0) -> new Direction[x$0]);
    }
}
