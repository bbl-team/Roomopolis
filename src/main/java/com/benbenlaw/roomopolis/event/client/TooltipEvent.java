package com.benbenlaw.roomopolis.event.client;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.item.PlacerItem;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.loader.options.BlockTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

        if (!(stack.getItem() instanceof PlacerItem)) return;

        Identifier templateId = stack.get(RoomsDataComponents.TEMPLATE_ID);

        if (templateId == null) {
            tooltips.add(Component.literal("Shift Right Click to open!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        TemplateDefinition definition = TemplateData.DATA.get(templateId);

        if (definition == null) {
            tooltips.add(Component.literal("Invalid template").withStyle(ChatFormatting.RED));
            return;
        }

        if (!Minecraft.getInstance().hasShiftDown()) {
            tooltips.add(Component.translatable("tooltips.rooms.shift")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        Vec3i templateSize = TemplateSizeCache.getTemplateSize(templateId);

        if (templateSize != null) {
            tooltips.add(Component.translatable(
                    "tooltips.key.template_size",
                    templateSize.getX(),
                    templateSize.getY(),
                    templateSize.getZ()
            ).withStyle(ChatFormatting.GRAY));
        }

        BlockTarget blockTarget = definition.blockTarget();

        tooltips.add(Component.translatable(
                "tooltips.key.requires_key_block",
                BlockTarget.toString(blockTarget)
        ).withStyle(ChatFormatting.RED));

        if (definition.restrictions().overrideExistingBlocks()) {
            tooltips.add(Component.translatable("tooltips.key.override_existing_blocks").withStyle(ChatFormatting.GRAY));
        } else {
            tooltips.add(Component.translatable("tooltips.key.normal_checks").withStyle(ChatFormatting.GRAY));
        }

        if (definition.door().isValid()) {
            tooltips.add(Component.translatable(
                    "tooltips.key.remove_door_area",
                    definition.door().left(),
                    definition.door().right(),
                    definition.door().up(),
                    definition.door().down()
            ).withStyle(ChatFormatting.GRAY));
        }

        if (definition.placement().sideOnlyPlacement()) {
            tooltips.add(Component.translatable("tooltips.key.side_only").withStyle(ChatFormatting.GRAY));
        }

        if (definition.placement().topOnlyPlacement()) {
            tooltips.add(Component.translatable("tooltips.key.top_only").withStyle(ChatFormatting.GRAY));
        }

        if (!definition.restrictions().blocksRequired()) {
            return;
        }

        if (!Minecraft.getInstance().hasAltDown()) {
            tooltips.add(Component.translatable("tooltips.rooms.alt").withStyle(ChatFormatting.YELLOW));
            return;
        }

        if (player == null) return;

        Map<Block, Integer> requiredBlocks =
                ((PlacerItem) stack.getItem())
                        .getRequiredBlocks(player.level(), definition, templateId);

        if (requiredBlocks == null || requiredBlocks.isEmpty()) return;

        Map<Block, Integer> playerBlocks = new HashMap<>();

        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                playerBlocks.put(block,
                        playerBlocks.getOrDefault(block, 0) + itemStack.getCount());
            }
        }

        tooltips.add(Component.translatable("tooltips.key.required_blocks").withStyle(ChatFormatting.GRAY));

        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {

            Block block = entry.getKey();
            int required = entry.getValue();
            int has = playerBlocks.getOrDefault(block, 0);

            boolean ok = has >= required;

            String text = (ok ? "(✔) " : "(❌) ")
                    + required + "x "
                    + block.getName().getString();

            tooltips.add(Component.literal(text)
                    .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }
}