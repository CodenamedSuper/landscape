package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class LandscapeItemTags {

    public static final TagKey<Item> SONGBIRD_FOOD = create("songbird_food");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, name));
    }
}
