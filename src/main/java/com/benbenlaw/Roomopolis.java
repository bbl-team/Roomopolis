package com.benbenlaw;

import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.item.RoomopolisCreativeTab;
import com.benbenlaw.roomopolis.item.RoomopolisItems;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.network.RoomopolisMessages;
import com.benbenlaw.roomopolis.renderer.GuiRenderState;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Roomopolis.MOD_ID)
public class Roomopolis {

    public static final String MOD_ID = "rooms";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Roomopolis (final IEventBus eventBus, final ModContainer modContainer) {

        RoomopolisItems.ITEMS.register(eventBus);
        RoomopolisCreativeTab.CREATIVE_MODE_TABS.register(eventBus);
        RoomsDataComponents.COMPONENTS.register(eventBus);

        NeoForge.EVENT_BUS.addListener(Roomopolis::onAddReloadListener);


        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            eventBus.addListener(this::onRegisterPipRenderers);
        }

        eventBus.addListener(this::networkingSetup);
    }

    public void networkingSetup(RegisterPayloadHandlersEvent event) {
        RoomopolisMessages.registerNetworking(event);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @EventBusSubscriber(modid = Roomopolis.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
            });
        }


        //@SubscribeEvent
        //public static void registerScreens(RegisterMenuScreensEvent event) {
        //    event.register(RoomsMenuTypes.PLACER_MENU.get(), PlacerScreen::new);
        //}
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Roomopolis.identifier("templates"), new TemplateData());
    }

    public void onRegisterPipRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(
                GuiRenderState.class,
                GuiRenderer::new
        );
    }
}

