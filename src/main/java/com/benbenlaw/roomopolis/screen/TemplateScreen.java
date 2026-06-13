package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.network.packet.SyncPlacerStack;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TemplateScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/template_gui.png");

    private final Identifier templateId;
    private final int imageWidth = 230;
    private final int imageHeight = 166;

    private GuiRenderer renderer;
    private final Screen parent;
    private TemplateDefinition definition;

    public TemplateScreen(Identifier templateId, Screen parent) {
        super(Component.literal("Template Config"));
        this.templateId = templateId;
        this.parent = parent;
    }

    @Override
    protected void init() {

        renderer = new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource());

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        definition = TemplateData.DATA.get(templateId);

        addRenderableWidget(Button.builder(Component.literal("<"),
                        b -> Minecraft.getInstance().setScreen(parent))
                .bounds(x + 6, y + 140, 20, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("tooltip.rooms.blocklist"),
                        b -> Minecraft.getInstance().setScreen(new BlockListScreen(templateId, this)))
                .bounds(x + 28, y + 140, 60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Palette"),
                        b -> Minecraft.getInstance()
                                .setScreen(new PaletteScreen(templateId, this)))
                .bounds(x + 90, y + 140, 60, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("tooltip.rooms.apply"),
                        b -> applyTemplate())
                .bounds(x + 164, y + 140, 60, 20).build());
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

        if (definition == null) return;

        graphics.text(Minecraft.getInstance().font,
                Component.translatable(definition.translatableName())
                        .withStyle(ChatFormatting.DARK_GRAY),
                x + 8, y + 6,
                0xFFFFFFFF, false);

        graphics.text(Minecraft.getInstance().font,
                Component.translatable("tooltip.rooms.template.settings")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.UNDERLINE),
                x + 94, y + 18,
                0xFFFFFFFF, false);

        int index = 12;

        if (!definition.blockTarget().matches(Blocks.AIR.defaultBlockState())) {

            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.block")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);

            index += 8;

            graphics.text(Minecraft.getInstance().font,
                    Component.literal("- ").append(definition.blockTarget().getDisplayName())
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);

            index += 12;
        }

        if (definition.restrictions().itemRequiredAndConsumed().isPresent()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.needs_item_consumed", definition.restrictions().itemRequiredAndConsumed().get().create().getDisplayName())
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 8;            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.in_inventory_to_use")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.door().isValid()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.door")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.restrictions().blocksRequired()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.blocks_required")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.placement().topOnlyPlacement()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.top_only")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.placement().sideOnlyPlacement()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.side_only")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.restrictions().overrideExistingBlocks()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.override_existing")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        if (definition.restrictions().replaceWaterLoggedBlocks()) {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.template.un_waterlog")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index,
                    0xFFFFFFFF, false);
            index += 12;
        }

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;

        if (definition.restrictions().showInPlacer()) {

            GuiStructureRenderState previewState =
                    GuiStructureRenderState.simpleGuiRenderState(
                            null,
                            rotationTime,
                            x + 8,
                            y + 18,
                            x + 90,
                            y + 100,
                            1.0f,
                            templateId,
                            50.0f
                    );

            GuiRenderState stateObject = ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
            renderer.prepare(previewState, stateObject, Minecraft.getInstance().getWindow().getGuiScale());
        } else {
            graphics.text(Minecraft.getInstance().font,
                    Component.translatable("tooltip.rooms.placer.hidden"),
                    x + 12, y + 22, 0xFFAAAAAA, false);
        }
    }

    private void applyTemplate() {

        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        stack.set(RoomsDataComponents.TEMPLATE_ID, templateId);

        ClientPacketDistributor.sendToServer(new SyncPlacerStack(stack));

        onClose();
    }

    @Override
    public void onClose() {

        if (renderer != null) renderer.close();

        Minecraft.getInstance().setScreen(null);
    }
}