package com.benbenlaw.roomopolis.block;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.RoomopolisItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class RoomopolisBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Roomopolis.MOD_ID);

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final DeferredBlock<Block> ROOM_BLOCK = registerBlock("room_block",
            properties -> new Block(properties
                    .strength(1.0F, 10000f)));

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final DeferredBlock<Block> ROOM_KEY_BLOCK = registerBlock("room_key_block",
            properties -> new Block(properties
                    .strength(1.0F, 10000f)));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        RoomopolisItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

}