package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.worldgen.feature.AntMoundFeature;
import com.codenamed.landscape.worldgen.feature.FallenTreeFeature;
import com.codenamed.landscape.worldgen.feature.config.AntMoundConfiguration;
import com.codenamed.landscape.worldgen.feature.config.FallenTreeConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LandscapeFeature<FC extends FeatureConfiguration> {


    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Landscape.MOD_ID);
    public static final DeferredHolder FALLEN_TREE = FEATURES.register("fallen_tree", () ->
            new FallenTreeFeature(FallenTreeConfiguration.CODEC));

    public static final DeferredHolder ANT_MOUND = FEATURES.register("ant_mound", () ->
            new AntMoundFeature(AntMoundConfiguration.CODEC));

    public static void init(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
