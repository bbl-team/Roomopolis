package com.benbenlaw.roomopolis.block;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.custom.KeyCrafterBlock;
import com.benbenlaw.roomopolis.block.custom.RoomBlock;
import com.benbenlaw.roomopolis.block.custom.RoomKeyBlock;
import com.benbenlaw.roomopolis.item.RoomopolisItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoomopolisBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Roomopolis.MOD_ID);

    public static final DeferredBlock<Block> KEY_CRAFTER = registerBlock("key_crafter",
            () -> new KeyCrafterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));
    public static final DeferredBlock<Block> ROOM_BLOCK = registerBlock("room_block",
            () -> new RoomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));
    public static final DeferredBlock<Block> ROOM_KEY_BLOCK = registerBlock("room_key_block",
            () -> new RoomKeyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }




    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        RoomopolisItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}