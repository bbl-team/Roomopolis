package com.benbenlaw.roomopolis.data;

import com.benbenlaw.Roomopolis;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RoomopolisItemModelProvider extends ItemModelProvider {

    public RoomopolisItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Roomopolis.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}

