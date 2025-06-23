package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.worldgen.feature.FallenTreeFeature;
import com.codenamed.landscape.worldgen.feature.config.FallenTreeConfiguration;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LandscapePoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, Landscape.MOD_ID);

    public static final Holder<PoiType> ANT_NEST = POI_TYPES.register("ant_nest",
            () -> new PoiType(ImmutableSet.copyOf(LandscapeBlocks.ANT_NEST.get().getStateDefinition().getPossibleStates()),
                    0, 1));

    public static void init(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}