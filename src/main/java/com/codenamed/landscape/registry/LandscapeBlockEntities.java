package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LandscapeBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Landscape.MOD_ID);

    public static void init(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
