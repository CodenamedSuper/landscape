package com.codenamed.landscape;


import com.codenamed.landscape.entity.client.renderer.SongbirdRenderer;
import com.codenamed.landscape.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Landscape.MOD_ID)
public class Landscape
{
    public static final String MOD_ID = "landscape";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Landscape(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        LandscapeItems.init(modEventBus);
        LandscapeBlocks.init(modEventBus);
        LandscapeDataComponentTypes.init(modEventBus);
        LandscapeBlockEntities.init(modEventBus);
        LandscapeEntities.init(modEventBus);
        LandscapePoiTypes.init(modEventBus);
        LandscapeSoundEvents.init(modEventBus);
        LandscapeCreativeTabs.init(modEventBus);
        LandscapeFeature.init(modEventBus);
        LandscapeTreeDecoratorType.init(modEventBus);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);




    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

            EntityRenderers.register(LandscapeEntities.SONGBIRD.get(), SongbirdRenderer::new);

        }
    }

}