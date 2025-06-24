package com.codenamed.landscape.worldgen.feature;

import com.codenamed.landscape.block.entity.AntNestBlockEntity;
import com.codenamed.landscape.registry.LandscapeBlockEntities;
import com.codenamed.landscape.registry.LandscapeBlocks;
import com.codenamed.landscape.worldgen.feature.config.AntMoundConfiguration;
import com.codenamed.landscape.worldgen.feature.config.FallenTreeConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.List;

public class AntMoundFeature extends Feature<AntMoundConfiguration> {
    public AntMoundFeature(Codec<AntMoundConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AntMoundConfiguration> featurePlaceContext) {
        return placeMount(featurePlaceContext.config(), featurePlaceContext.origin(), featurePlaceContext.level(), featurePlaceContext.random());
    }

    private boolean placeMount(AntMoundConfiguration config, BlockPos origin, WorldGenLevel level, RandomSource random) {
        if (!isGroundSuitable(level, origin)) return false;

        int radius = config.size.sample(random);
        int maxHeight = config.height.sample(random);

        List<BlockPos> moundBlocks = new ArrayList<>();


        List<BlockPos> antSoils = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius + random.nextDouble() * 0.5) continue;

                int height = (int) ((1.0 - (distance / radius)) * maxHeight) + random.nextInt(2);
                BlockPos basePos = origin.offset(dx, 0, dz);
                antSoils = placeMoundColumn(level, basePos, height, random, antSoils, moundBlocks);
            }
        }

        List<BlockPos> exposedSpots = antSoils.stream()
                .filter(pos -> hasSideAir(level, pos))
                .toList();

        if (!exposedSpots.isEmpty()) {
            BlockPos nestPos = exposedSpots.get(random.nextInt(exposedSpots.size()));
            level.setBlock(nestPos, LandscapeBlocks.ANT_NEST.get().defaultBlockState(), 2);

            level.getBlockEntity(nestPos, LandscapeBlockEntities.ANT_NEST.get()).ifPresent(nest -> {
                int antCount = 2 + random.nextInt(2);
                for (int i = 0; i < antCount; i++) {
                    nest.storeAnt(AntNestBlockEntity.Occupant.create(random.nextInt(599)));
                }
            });
        }

        patchGroundWithCoarseDirt(level, origin, radius + 2, random);

        for (BlockPos pos : moundBlocks) {
            for (int depth = 1; depth <= 2 + random.nextInt(2); depth++) {
                BlockPos below = pos.below(depth);
                if (level.getBlockState(below).isAir() || level.getBlockState(below).is(BlockTags.REPLACEABLE)) {
                    level.setBlock(below, LandscapeBlocks.ANT_SOIL.get().defaultBlockState(), 2);
                } else {
                    break;
                }
            }
        }

        return true;
    }



    private List<BlockPos> placeMoundColumn(WorldGenLevel level, BlockPos basePos, int height, RandomSource random,
                                            List<BlockPos> antSoils, List<BlockPos> moundBlocks) {
        for (int y = 0; y <= height; y++) {
            BlockPos pos = basePos.above(y);
            if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.REPLACEABLE)) {
                level.setBlock(pos, LandscapeBlocks.ANT_SOIL.get().defaultBlockState(), 2);

                moundBlocks.add(pos);
                if (y == height || y == height - 1) {
                    antSoils.add(pos);
                }
            }
        }
        return antSoils;
    }

    private boolean isGroundSuitable(WorldGenLevel level, BlockPos origin) {
        Block ground = level.getBlockState(origin.below()).getBlock();
        return ground == Blocks.GRASS_BLOCK || ground == Blocks.DIRT || ground == Blocks.COARSE_DIRT;
    }

    private boolean hasSideAir(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos.north()).isAir()
                || level.getBlockState(pos.south()).isAir()
                || level.getBlockState(pos.east()).isAir()
                || level.getBlockState(pos.west()).isAir();
    }

    private void patchGroundWithCoarseDirt(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (random.nextFloat() > 0.1f) continue;

                BlockPos pos = center.offset(dx, -1, dz);
                if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK) || level.getBlockState(pos).is(Blocks.DIRT)) {
                    level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                }
            }
        }
    }


}
