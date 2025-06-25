package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.block.entity.SongbirdNestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LandscapeBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Landscape.MOD_ID);

    public static final Supplier<BlockEntityType<SongbirdNestBlockEntity>> SONGBIRD_NEST = BLOCK_ENTITIES.register("songbird_nest",
            () -> BlockEntityType.Builder.of(SongbirdNestBlockEntity::new, LandscapeBlocks.SONGBIRD_NEST.get()).build(null));

    public static void init(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
