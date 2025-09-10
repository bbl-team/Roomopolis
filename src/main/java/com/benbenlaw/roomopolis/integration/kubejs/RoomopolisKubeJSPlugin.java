package com.benbenlaw.roomopolis.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;

public class RoomopolisKubeJSPlugin implements KubeJSPlugin {


    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {

        registry.of(Registries.ITEM,
                r -> r.add("roomopolis_key", KeyItemBuilder.class, KeyItemBuilder::new));

        KubeJSPlugin.super.registerBuilderTypes(registry);
    }
}
