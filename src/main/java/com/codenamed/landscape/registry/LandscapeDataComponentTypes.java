package com.codenamed.landscape.registry;
import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.block.entity.AntNestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

public class LandscapeDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Landscape.MOD_ID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<AntNestBlockEntity.Occupant>>> ANTS
            = register("ants", (p_341849_) -> p_341849_.persistent(AntNestBlockEntity.Occupant.LIST_CODEC).networkSynchronized(AntNestBlockEntity.Occupant.STREAM_CODEC.apply(ByteBufCodecs.list())).cacheEncoding());


    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void init(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}