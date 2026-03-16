package com.benbenlaw.roomopolis.item;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoomopolisCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Roomopolis.MOD_ID);

    public static final Supplier<CreativeModeTab> ROOMS_TAB = CREATIVE_MODE_TABS.register(Roomopolis.MOD_ID, () -> CreativeModeTab.builder()
            .icon(() -> RoomopolisBlocks.ROOM_BLOCK.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.roomopolis"))
            .displayItems((parameters, output) -> {

                output.accept(RoomopolisBlocks.ROOM_KEY_BLOCK.get());
                output.accept(RoomopolisBlocks.ROOM_BLOCK.get());
                //output.accept(RoomopolisItems.TINY_ROOM_KEY.get());
                //output.accept(RoomopolisItems.SMALL_ROOM_KEY.get());
                //output.accept(RoomopolisItems.MEDIUM_ROOM_KEY.get());
                //output.accept(RoomopolisItems.LARGE_ROOM_KEY.get());
            }).build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }


}
