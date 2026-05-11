package com.benbenlaw.roomopolis.item;

import com.benbenlaw.Roomopolis;
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
            .icon(() -> RoomopolisItems.PLACER.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.roomopolis"))
            .displayItems((parameters, output) -> {

                output.accept(RoomopolisItems.PLACER.get());
            }).build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }


}
