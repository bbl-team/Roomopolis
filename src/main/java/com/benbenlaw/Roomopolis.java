package com.benbenlaw;

import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import com.benbenlaw.roomopolis.item.RoomopolisCreativeTab;
import com.benbenlaw.roomopolis.item.RoomopolisItems;
import com.benbenlaw.roomopolis.network.RoomopolisMessages;
import com.benbenlaw.roomopolis.screen.KeyCrafterScreen;
import com.benbenlaw.roomopolis.screen.RoomopolisMenuTypes;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Roomopolis.MOD_ID)
public class Roomopolis {

    public static final String MOD_ID = "roomopolis";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Roomopolis (final IEventBus eventBus, final ModContainer modContainer) {

        RoomopolisItems.ITEMS.register(eventBus);
        RoomopolisBlocks.BLOCKS.register(eventBus);
        RoomopolisCreativeTab.CREATIVE_MODE_TABS.register(eventBus);

        RoomopolisMenuTypes.MENUS.register(eventBus);

        eventBus.addListener(this::networkingSetup);
    }

    public void networkingSetup(RegisterPayloadHandlersEvent event) {
        RoomopolisMessages.registerNetworking(event);
    }


    @EventBusSubscriber(modid = Roomopolis.MOD_ID, bus = EventBusSubscriber.Bus.MOD ,value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(RoomopolisMenuTypes.KEY_CRAFTER_MENU.get(), KeyCrafterScreen::new);
        }
    }


}

