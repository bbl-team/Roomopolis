package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoomopolisMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, Roomopolis.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<KeyCrafterMenu>> KEY_CRAFTER_MENU;


    static {
        KEY_CRAFTER_MENU = MENUS.register("key_crafter_menu", () ->
                IMenuTypeExtension.create(KeyCrafterMenu::new));

    }


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }


}
