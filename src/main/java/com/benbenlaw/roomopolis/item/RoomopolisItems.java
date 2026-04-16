package com.benbenlaw.roomopolis.item;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.api.KeyItemBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class RoomopolisItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Roomopolis.MOD_ID);

    public static final DeferredItem<Item> TEST = ITEMS.registerItem("test",
            properties -> new KeyItem(
                    properties.stacksTo(1),
                    KeyItemBuilder.create()
                            .template("rooms:plank")
                            .block("#minecraft:logs")
                            .door(1, 1, 2, 0)
                            .consumeKey(true)
                            .rotation(Rotation.CLOCKWISE_90)
                            .build()
            )
    );

    public static final DeferredItem<Item> TEST2 = ITEMS.registerItem("test2",
            properties -> new KeyItem(
                    properties.stacksTo(1),
                    KeyItemBuilder.create()
                            .template("rooms:world")
                            .block(Blocks.COAL_BLOCK) // or Blocks.STONE
                            .door(1, 1, 2, 0)
                            .consumeKey(true)
                            .blocksRequired(true)
                            .build()
            )
    );

}
