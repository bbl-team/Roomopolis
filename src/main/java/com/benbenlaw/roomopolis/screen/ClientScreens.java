package com.benbenlaw.roomopolis.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientScreens {

    public static void openPlacerScreen() {
        Minecraft.getInstance().setScreen(new PlacerScreen(Component.literal("Room Placer")));
    }
}