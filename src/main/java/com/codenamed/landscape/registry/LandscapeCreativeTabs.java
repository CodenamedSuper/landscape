package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class LandscapeCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Landscape.MOD_ID);

    public static final Supplier<CreativeModeTab> LANDSCAPE = CREATIVE_MODE_TAB.register("landscape",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Blocks.GRASS_BLOCK))
                    .title(Component.translatable("creativetab.landscape.landscape"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(LandscapeBlocks.WHITE_MUSHROOM_BlOCK);
                        output.accept(LandscapeBlocks.WHITE_MUSHROOM);
                        output.accept(LandscapeBlocks.TRILLIUM);
                        output.accept(LandscapeItems.SONGBIRD_SPAWN_EGG);


                    }).build());

    public static void init(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
