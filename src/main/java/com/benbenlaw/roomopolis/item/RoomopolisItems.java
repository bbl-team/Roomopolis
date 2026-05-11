package com.benbenlaw.roomopolis.item;

import com.benbenlaw.Roomopolis;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class RoomopolisItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Roomopolis.MOD_ID);

    public static final DeferredItem<Item> PLACER = ITEMS.registerItem("placer",
            properties -> new PlacerItem(properties.stacksTo(1))
    );

}
