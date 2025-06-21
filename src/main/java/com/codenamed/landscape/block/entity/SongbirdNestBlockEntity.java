package com.codenamed.landscape.block.entity;

import com.codenamed.landscape.block.SongbirdNestBlock;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.registry.LandscapeBlockEntities;
import com.codenamed.landscape.registry.LandscapeEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SongbirdNestBlockEntity extends BlockEntity {

    public SongbirdData occupant = new SongbirdData();

    public SongbirdNestBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandscapeBlockEntities.SONGBIRD_NEST.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {

        if (state.getValue(SongbirdNestBlock.OCCUPIED)) {
            Songbird songbird = LandscapeEntities.SONGBIRD.get().create(level);
            if (songbird != null) {
                songbird.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.random.nextFloat() * 360F, 0F);
                level.addFreshEntity(songbird);

                level.setBlock(pos, state.setValue(SongbirdNestBlock.OCCUPIED, false), 2);
            }
        }
    }

    public static class SongbirdData {
        private Songbird songbird;

        public  void occupy(Songbird s, BlockPos nestPos) {
            songbird = s;
            songbird.nestPos = nestPos;
        }

        public void remove() {
            if (songbird != null) {
                songbird.nestPos = null;
                songbird = null;
            }
        }


        public boolean occupied() {
            return songbird != null;
        }

    }
}
