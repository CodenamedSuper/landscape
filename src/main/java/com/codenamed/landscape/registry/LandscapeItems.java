package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LandscapeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Landscape.MOD_ID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}