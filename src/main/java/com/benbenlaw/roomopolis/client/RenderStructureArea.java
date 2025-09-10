package com.benbenlaw.roomopolis.client;

import com.benbenlaw.Roomopolis;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = Roomopolis.MOD_ID,value = Dist.CLIENT)
public class RenderStructureArea {


    @SubscribeEvent
    public static void renderStructureOutline(RenderHighlightEvent.Block event) {

    }

}
