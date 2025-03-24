package com.benbenlaw.roomopolis.events;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSCommon;
import dev.latvian.mods.kubejs.bindings.event.StartupEvents;
import dev.latvian.mods.kubejs.core.ItemKJS;
import dev.latvian.mods.kubejs.ingredient.KubeJSIngredients;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.lwjgl.system.Platform;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = Roomopolis.MOD_ID)

public class UpdateKeyCache {

    @SubscribeEvent
    public static void updateKeyCache(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Objects.requireNonNull(serverPlayer.getServer()).execute(() -> {
            ServerLevel level = (ServerLevel) serverPlayer.level();
            StructureTemplateManager structureManager = level.getStructureManager();
            for (Item item : BuiltInRegistries.ITEM) {

                System.out.println("item: " + item);
                if (item instanceof KeyItem keyItem) {
                    ResourceLocation templateId = keyItem.templateId;
                    Optional<StructureTemplate> optionalTemplate = structureManager.get(templateId);

                    optionalTemplate.ifPresent(template -> {
                        Vec3i size = template.getSize();
                        KeyItemSizeCache.setTemplateSize(templateId, size);
                        System.out.println("added template size to cache server: " + templateId + " " + size);

                        PacketDistributor.sendToPlayer(serverPlayer, new GetStructureSizePayload(templateId.toString(), size));
                    });
                }
            }

        });
    }

}
