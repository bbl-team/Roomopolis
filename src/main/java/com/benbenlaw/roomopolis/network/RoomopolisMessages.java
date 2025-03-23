package com.benbenlaw.roomopolis.network;

import com.benbenlaw.caveopolis.Caveopolis;
import com.benbenlaw.roomopolis.network.packet.GetStructureSizePacket;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoomopolisMessages {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Caveopolis.MOD_ID);

        //To Client From Server
        registrar.playToClient(GetStructureSizePayload.TYPE, GetStructureSizePayload.STREAM_CODEC, GetStructureSizePacket.get()::handle);
    }
}
