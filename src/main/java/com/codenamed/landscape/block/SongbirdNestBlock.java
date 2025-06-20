package com.codenamed.landscape.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class SongbirdNestBlock extends Block {

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Stream.of(
                Block.box(0, 0, 0, 16, 2, 16),
                Block.box(0, 2, 14, 16, 6, 16),
                Block.box(14, 2, 2, 16, 6, 14),
                Block.box(0, 2, 0, 16, 6, 2),
                Block.box(0, 2, 2, 2, 6, 14)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    public SongbirdNestBlock(Properties properties) {
        super(properties);
    }
}
