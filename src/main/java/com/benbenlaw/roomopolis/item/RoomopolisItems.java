package com.benbenlaw.roomopolis.item;

import com.benbenlaw.Roomopolis;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class RoomopolisItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Roomopolis.MOD_ID);


    public static final DeferredItem<Item> TEST = ITEMS.register("test", () -> new KeyItem(new Item.Properties(),
            "roomopolis:plank", 2, 0, "minecraft:diamond_block",
            false, false, true, false, true, false, 0, 0, 0, 0, 256));


    /*
    public static final DeferredItem<Item> WATER = ITEMS.register("water",
            () -> new KeyItem(new Item.Properties(), "minecraft:water", 0, 0, "minecraft:terracotta", true, false, true, false,  false, true, 0, 0, 0, 1, 256).replaceWaterLoggedBlocks());


    public static final DeferredItem<Item> WATERLOGGED = ITEMS.register("waterlogged",
            () -> new KeyItem(new Item.Properties(), "minecraft:water", 0, 0, "minecraft:terracotta", true, false, true, false,  false, true, 0, 0, 0, 1, 256));



     */

    /*
    public static final DeferredItem<Item> TINY_ROOM_KEY = ITEMS.register("tiny_room_key",
            () -> new KeyItem(new Item.Properties(), "roomopolis:tiny_room", 0, 0, "roomopolis:room_key_block", true, true, true, false,  false, true, 0, 0, 0, 1, 256));
    public static final DeferredItem<Item> SMALL_ROOM_KEY = ITEMS.register("small_room_key",
            () -> new KeyItem(new Item.Properties(),"roomopolis:small_room", 0, 0, "roomopolis:room_key_block", true, true, true, false, false, false, 0, 0, 0, 1, 256));
    public static final DeferredItem<Item> MEDIUM_ROOM_KEY = ITEMS.register("medium_room_key",
            () -> new KeyItem(new Item.Properties(), "roomopolis:medium_room", 1, 0,"roomopolis:room_key_block", true, true, true, false, false, false, 0, 0, 0, 1, 256));
    public static final DeferredItem<Item> LARGE_ROOM_KEY = ITEMS.register("large_room_key",
            () -> new KeyItem(new Item.Properties(), "roomopolis:large_room", 2, 0, "roomopolis:room_key_block", true, true, true, false,false, false, 1, 2, 2, 1, 256));;
    public static final DeferredItem<Item> INTERCONNECTED_WALL_REMOVING_KEY = ITEMS.register("interconnected_wall_removing_key",
            () -> new WallRemoverKeyItem(new Item.Properties()));

     */

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
