package com.codenamed.landscape.registry;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.block.SongbirdNestBlock;
import com.codenamed.landscape.block.WhiteMushroomBlock;
import com.codenamed.landscape.block.WhiteMushroomBlockBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
public class LandscapeBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Landscape.MOD_ID);

    public static final DeferredBlock<Block> WHITE_MUSHROOM = registerBlock("white_mushroom",
            () -> new WhiteMushroomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)));

    public static final DeferredBlock<Block> POTTED_WHITE_MUSHROOM = registerBlock("potted_white_mushroom",
            () -> flowerPot(WHITE_MUSHROOM.get()));

    public static final DeferredBlock<Block> WHITE_MUSHROOM_BlOCK = registerBlock("white_mushroom_block",
            () -> new WhiteMushroomBlockBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK)));

    public static final DeferredBlock<Block> TRILLIUM = registerBlock("trillium",
            () -> new FlowerBlock(SuspiciousStewEffects.EMPTY,BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_OF_THE_VALLEY)));

    public static final DeferredBlock<Block> POTTED_TRILLIUM = registerBlock("potted_trillium",
            () -> flowerPot(TRILLIUM.get()));

    public static final DeferredBlock<Block> SONGBIRD_NEST = registerBlock("songbird_nest",
            () -> new SongbirdNestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK).sound(SoundType.AZALEA)));

    private static Block stair(DeferredBlock<Block> baseBlock) {
        return new StairBlock(baseBlock.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(baseBlock.get()));
    }

    private static Block flowerPot(Block potted) {
        return new FlowerPotBlock(potted, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        LandscapeItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}