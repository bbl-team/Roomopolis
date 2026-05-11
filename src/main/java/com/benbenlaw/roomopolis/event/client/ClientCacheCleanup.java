package com.benbenlaw.roomopolis.event.client;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.item.TemplatePaletteCache;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(
        modid = Roomopolis.MOD_ID

)
public class ClientCacheCleanup {

    @SubscribeEvent
    public static void onDisconnect(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {

        FakeStructureTemplateManager.INSTANCE.clear();

        TemplateSizeCache.clear();

        TemplatePaletteCache.clear();
    }


}