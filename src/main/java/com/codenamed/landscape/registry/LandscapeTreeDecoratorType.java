package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.worldgen.feature.FallenTreeFeature;
import com.codenamed.landscape.worldgen.feature.config.FallenTreeConfiguration;
import com.codenamed.landscape.worldgen.feature.treedecorator.AttachedToLogsDecorator;
import com.codenamed.landscape.worldgen.feature.treedecorator.SongbirdNestDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LandscapeTreeDecoratorType {

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, Landscape.MOD_ID);

    public static final Supplier<TreeDecoratorType<AttachedToLogsDecorator>> ATTACHED_TO_LOGS =
            TREE_DECORATORS.register("attached_to_logs", () ->
                    new TreeDecoratorType<>(AttachedToLogsDecorator.CODEC));

    public static final Supplier<TreeDecoratorType<SongbirdNestDecorator>> SONGBIRD_NEST =
            TREE_DECORATORS.register("songbird_nest", () ->
                    new TreeDecoratorType<>(SongbirdNestDecorator.CODEC));


    public static void init(IEventBus eventBus) {
        TREE_DECORATORS.register(eventBus);
    }
}
