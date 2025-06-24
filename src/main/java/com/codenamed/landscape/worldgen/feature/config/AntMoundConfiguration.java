package com.codenamed.landscape.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

import java.util.ArrayList;
import java.util.List;

public class AntMoundConfiguration implements FeatureConfiguration {

    public static final Codec<AntMoundConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            IntProvider.codec(0, 16).fieldOf("size").forGetter(config -> config.size),
                            IntProvider.codec(0, 32).fieldOf("height").forGetter(config -> config.height)

                    )
                    .apply(instance, AntMoundConfiguration::new)
    );


    public final IntProvider size;
    public final IntProvider height;

    public AntMoundConfiguration(IntProvider size, IntProvider height) {
        this.size = size;
        this.height = height;
    }

    public static class AntMoundConfigurationBuilder {
        private final IntProvider size;
        private final IntProvider height;


        public AntMoundConfigurationBuilder(IntProvider size, IntProvider height) {
            this.size = size;
            this.height = height;
        }


        public AntMoundConfiguration build() {
            return new AntMoundConfiguration(this.size, this.height);
        }
    }
}
