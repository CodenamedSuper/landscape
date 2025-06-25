package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LandscapeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Landscape.MOD_ID);

    public static final DeferredItem<Item> SONGBIRD_SPAWN_EGG = ITEMS.register("songbird_spawn_egg",
            () -> new DeferredSpawnEggItem(LandscapeEntities.SONGBIRD, 0x24698c, 0xefdbb1,
                    new Item.Properties()));

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}