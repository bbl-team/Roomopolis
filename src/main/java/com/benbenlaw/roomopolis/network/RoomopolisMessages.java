package com.benbenlaw.roomopolis.network;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.network.packet.GetStructurePalettePacket;
import com.benbenlaw.roomopolis.network.packet.GetStructureSizePacket;
import com.benbenlaw.roomopolis.network.packet.StructureTemplatePacket;
import com.benbenlaw.roomopolis.network.payload.GetStructurePalettePayload;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import com.benbenlaw.roomopolis.network.payload.StructureTemplatePayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoomopolisMessages {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Roomopolis.MOD_ID);

        //To Client From Server
        registrar.playToClient(GetStructureSizePayload.TYPE, GetStructureSizePayload.STREAM_CODEC, GetStructureSizePacket.get()::handle);
        registrar.playToClient(GetStructurePalettePayload.TYPE, GetStructurePalettePayload.STREAM_CODEC, GetStructurePalettePacket.get()::handle);
        registrar.playToClient(StructureTemplatePayload.TYPE, StructureTemplatePayload.STREAM_CODEC, StructureTemplatePacket.get()::handle);
    }
}
