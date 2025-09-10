package com.benbenlaw.roomopolis.data;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class RoomopolisBlockStateProvider extends BlockStateProvider {


    public RoomopolisBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Roomopolis.MOD_ID, existingFileHelper);

    }

    @Override
    protected void registerStatesAndModels() {

        blockWithItem(RoomopolisBlocks.ROOM_BLOCK);
        blockWithItem(RoomopolisBlocks.ROOM_KEY_BLOCK);

    }

    private void blockWithItem(DeferredBlock<Block> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
    @Override
    public String getName() {
        return Roomopolis.MOD_ID + " Block States";
    }
}


