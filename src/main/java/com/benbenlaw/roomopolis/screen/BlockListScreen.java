package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.PlacerItem;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockListScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/blocklist_gui.png");

    private final Identifier templateId;
    private final int imageWidth = 230;
    private final int imageHeight = 166;
    private GuiRenderer renderer;
    private final Screen parent;

    private int currentPage = 0;
    private static final int ENTRIES_PER_PAGE = 10;
    private Button nextButton;
    private Button prevButton;

    public BlockListScreen(Identifier templateId, Screen parent) {
        super(Component.literal("Template Config"));
        this.templateId = templateId;
        this.parent = parent;
    }

    @Override
    protected void init() {
        renderer = new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource());

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> Minecraft.getInstance().setScreen(parent))
                .bounds(x + 6, y + 140, 20, 20).build());

        prevButton = addRenderableWidget(Button.builder(Component.literal("↑"), b -> {
            if (currentPage > 0) currentPage--;
        }).bounds(x + 200, y + 18, 20, 20).build());

        nextButton = addRenderableWidget(Button.builder(Component.literal("↓"), b -> {
            currentPage++;
        }).bounds(x + 200, y + 42, 20, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        TemplateDefinition definition = TemplateData.DATA.get(templateId);

        if (definition == null) return;
        if (!definition.restrictions().blocksRequired()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooltip.rooms.blocklist_not_required")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 8, y + 6, 0xFFFFFFFF, false);
            return;

        }

        graphics.text(Minecraft.getInstance().font, Component.translatable("tooltip.rooms.blocklist")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.UNDERLINE),
                x + 8, y + 6, 0xFFFFFFFF, false);

        Map<Block, Integer> playerBlocks = new HashMap<>();
        assert Minecraft.getInstance().player != null;
        for (ItemStack itemStack : Minecraft.getInstance().player.getInventory().getNonEquipmentItems()) {
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                playerBlocks.put(block, playerBlocks.getOrDefault(block, 0) + itemStack.getCount());
            }
        }

        if (definition == null) return;
        ItemStack held =Minecraft.getInstance().player.getMainHandItem();
        if (!(held.getItem() instanceof PlacerItem placerItem)) return;
        Map<Block, Integer> requiredBlocksMap = placerItem.getRequiredBlocks(Minecraft.getInstance().player.level(), definition);
        List<Map.Entry<Block, Integer>> fullList = new ArrayList<>(requiredBlocksMap.entrySet());

        int maxPages = (int) Math.ceil((double) fullList.size() / ENTRIES_PER_PAGE);
        if (currentPage >= maxPages && maxPages > 0) currentPage = maxPages - 1;

        prevButton.active = currentPage > 0;
        nextButton.active = (currentPage + 1) * ENTRIES_PER_PAGE < fullList.size();

        int start = currentPage * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, fullList.size());
        int drawYOffset = 18;

        for (int i = start; i < end; i++) {
            Map.Entry<Block, Integer> entry = fullList.get(i);
            Block block = entry.getKey();
            int required = entry.getValue();
            int has = playerBlocks.getOrDefault(block, 0);

            boolean ok = has >= required;

            Component statusIcon = Component.literal(ok ? "✔ " : "❌ ").withStyle(ok ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);
            Component lineText = Component.literal(required + "x ").append(block.getName()).withStyle(ChatFormatting.DARK_GRAY);

            graphics.text(Minecraft.getInstance().font, statusIcon.copy().append(lineText),
                    x + 8, y + drawYOffset, 0xFFFFFFFF, false);

            drawYOffset += 12;
        }

        if (maxPages > 1) {
            String pageText = (currentPage + 1) + "/" + maxPages;
            graphics.text(Minecraft.getInstance().font, pageText, x + 202, y + 66, 0xFFAAAAAA, false);
        }
    }

    @Override
    public void onClose() {
        if (renderer != null) {
            renderer.close();
        }
        Minecraft.getInstance().setScreen(null);
    }
}