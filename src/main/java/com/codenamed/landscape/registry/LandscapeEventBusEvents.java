package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.entity.client.model.SongbirdModel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Landscape.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class LandscapeEventBusEvents {

    @SubscribeEvent
    public  static  void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SongbirdModel.LAYER_LOCATION, SongbirdModel::createBodyLayer);

    }

    @SubscribeEvent
    public  static  void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LandscapeEntities.SONGBIRD.get(), Songbird.createAttributes().build());

    }
}
