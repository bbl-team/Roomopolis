package com.benbenlaw.roomopolis.event.client;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.core.util.TooltipUtil;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.util.BlockTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Roomopolis.MOD_ID)
public class TooltipEvent {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        Player player = event.getEntity();

        if (stack.getItem() instanceof KeyItem keyItem) {

            Map<Block, Integer> blockMap = KeyItemPaletteCache.getTemplatePalette(keyItem.definition().templateId());
            Vec3i templateSize = KeyItemSizeCache.getTemplateSize(keyItem.definition().templateId());

            if (templateSize == null) {
                System.err.println("Template size missing for " + keyItem.definition().templateId());
                return;
            }

            if (Minecraft.getInstance().hasShiftDown()) {

                if (keyItem.definition().consumeKey()) {
                    tooltips.add(Component.translatable("tooltips.key.consume_key").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltips.add(Component.translatable("tooltips.key.retain_key").withStyle(ChatFormatting.GRAY));
                }

                if (keyItem.definition().overrideExistingBlocks()) {
                    tooltips.add(Component.translatable("tooltips.key.override_existing_blocks").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltips.add(Component.translatable("tooltips.key.normal_checks").withStyle(ChatFormatting.GRAY));
                }

                if (keyItem.definition().removeDoorArea()) {
                    tooltips.add(Component.translatable("tooltips.key.remove_door_area",
                            keyItem.definition().doorLeft(), keyItem.definition().doorRight(),
                            keyItem.definition().doorUp(), keyItem.definition().doorDown()).withStyle(ChatFormatting.GRAY));
                }

                if (keyItem.definition().sideOnlyPlacement()) {
                    tooltips.add(Component.translatable("tooltips.key.side_only").withStyle(ChatFormatting.GRAY));
                }

                if (keyItem.definition().topOnlyPlacement()) {
                    tooltips.add(Component.translatable("tooltips.key.top_only").withStyle(ChatFormatting.GRAY));
                }

                Component templateSizeText = Component.translatable("tooltips.key.template_size",
                        templateSize.getX(), templateSize.getY(), templateSize.getZ()).withStyle(ChatFormatting.GRAY);
                tooltips.add(templateSizeText);

                BlockTarget blockTarget = keyItem.definition().blockTarget();
                String blockTargetString = BlockTarget.toString(blockTarget);

                tooltips.add(Component.translatable("tooltips.key.requires_key_block", blockTargetString).withStyle(ChatFormatting.RED));

            } else {
                tooltips.add(Component.translatable("tooltips.roomopolis.shift").withStyle(ChatFormatting.YELLOW));
            }

            // Add List
            if (keyItem.definition().blocksRequired()) {
                if (Minecraft.getInstance().hasAltDown()) {
                    if (blockMap != null && player != null) {
                        Map<Block, Integer> playerBlocks = new HashMap<>();

                        // Count blocks in the player's inventory
                        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
                            if (itemStack.getItem() instanceof BlockItem blockItem) {
                                Block block = blockItem.getBlock();
                                playerBlocks.put(block, playerBlocks.getOrDefault(block, 0) + itemStack.getCount());
                            }
                        }

                        tooltips.add(Component.translatable("tooltips.key.required_blocks").withStyle(ChatFormatting.GRAY));

                        for (Map.Entry<Block, Integer> entry : blockMap.entrySet()) {
                            Block block = entry.getKey();
                            int requiredCount = entry.getValue();
                            int playerCount = playerBlocks.getOrDefault(block, 0);

                            ChatFormatting color = (playerCount >= requiredCount) ? ChatFormatting.GREEN : ChatFormatting.RED;

                            String tooltipText = requiredCount + "x " + block.getName().getString();

                            if (playerCount >= requiredCount) {
                                tooltipText = "(✔) " + tooltipText;
                            } else {
                                tooltipText = "(❌) " + tooltipText;
                            }

                            tooltips.add(Component.literal(tooltipText)
                                    .withStyle(color));
                        }

                    }

                } else {
                    tooltips.add(Component.translatable("tooltips.roomopolis.alt").withStyle(ChatFormatting.YELLOW));
                }
            }

        }
    }
}