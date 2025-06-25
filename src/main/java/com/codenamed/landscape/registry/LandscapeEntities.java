package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Songbird;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LandscapeEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Landscape.MOD_ID);

    public static final Supplier<EntityType<Songbird>> SONGBIRD =
            ENTITY_TYPES.register("songbird", () -> EntityType.Builder.of(Songbird::new, MobCategory.CREATURE)
                    .sized(0.35f, 0.5f).build("songbird"));


    public static void init(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}