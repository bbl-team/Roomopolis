package com.benbenlaw.roomopolis.network;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.network.packet.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoomopolisMessages {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Roomopolis.MOD_ID);

        //Server -> Client
        registrar.playToClient(GetStructureSizePacket.TYPE, GetStructureSizePacket.STREAM_CODEC, GetStructureSizePacket.HANDLER);
        registrar.playToClient(GetStructurePalettePacket.TYPE, GetStructurePalettePacket.STREAM_CODEC, GetStructurePalettePacket.HANDLER);
        registrar.playToClient(StructureTemplatePacket.TYPE, StructureTemplatePacket.STREAM_CODEC, StructureTemplatePacket.HANDLER);
        registrar.playToClient(TemplateDefinitionPacket.TYPE, TemplateDefinitionPacket.STREAM_CODEC, TemplateDefinitionPacket.HANDLER);

        //Client -> Server
        registrar.playToServer(SyncPlacerStack.TYPE, SyncPlacerStack.STREAM_CODEC, SyncPlacerStack.HANDLER);
        registrar.playToServer(SyncPaletteSelection.TYPE, SyncPaletteSelection.STREAM_CODEC, SyncPaletteSelection.HANDLER);
    }
}
