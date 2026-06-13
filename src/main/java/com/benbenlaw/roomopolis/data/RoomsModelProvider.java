package com.benbenlaw.roomopolis.data;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.RoomopolisItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class RoomsModelProvider extends ModelProvider {

    public RoomsModelProvider(PackOutput output) {
        super(output, Roomopolis.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        //Items
        itemModels.generateFlatItem(RoomopolisItems.PLACER.get(), ModelTemplates.FLAT_ITEM);
    }
}
