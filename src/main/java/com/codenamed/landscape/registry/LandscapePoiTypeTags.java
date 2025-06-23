package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;

public class LandscapePoiTypeTags {

    public static final TagKey<PoiType> ANT_HOME = create("ant_home");

    private static TagKey<PoiType> create(String name) {
        return TagKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, name));
    }
}
