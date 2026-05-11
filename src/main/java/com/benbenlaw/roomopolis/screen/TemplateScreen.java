package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.loader.options.BlockTarget;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.network.packet.SyncPlacerStack;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
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
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Objects;

public class TemplateScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/template_gui.png");

    private final Identifier templateId;
    private final int imageWidth = 230;
    private final int imageHeight = 166;
    private GuiRenderer renderer;
    private Rotation rotation = Rotation.NONE;
    private final Screen parent;

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

        /*
        addRenderableWidget(Button.builder(
                                Component.literal("Rotate"),
                                btn -> rotation = switch (rotation) {
                                    case NONE -> Rotation.CLOCKWISE_90;
                                    case CLOCKWISE_90 -> Rotation.CLOCKWISE_180;
                                    case CLOCKWISE_180 -> Rotation.COUNTERCLOCKWISE_90;
                                    case COUNTERCLOCKWISE_90 -> Rotation.NONE;
                                }
                        )
                        .bounds(centerX + 60, centerY - 40, 80, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                                Component.literal("Apply"),
                                btn -> applyTemplate()
                        )
                        .bounds(centerX + 60, centerY + 40, 80, 20)
                        .build()
        );

         */

        addRenderableWidget(Button.builder(Component.literal("<"), b -> Minecraft.getInstance().setScreen(parent)).bounds(x + 6, y + 140, 20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("tooptip.rooms.blocklist.blocklist"),
                b -> Minecraft.getInstance().setScreen(new BlockListScreen(templateId, this))).bounds(x + 28, y + 140, 80, 20).build());


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

        //Title
        graphics.text(Minecraft.getInstance().font, Component.translatable(definition.translatableName()).withStyle(ChatFormatting.DARK_GRAY),
                x + 8, y + 6, 0xFFFFFFFF, false);

        //Settings
        graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.settings").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.UNDERLINE),
                x + 94, y + 18, 0xFFFFFFFF, false);
        int index = 12;

        //Block Target
        if (!definition.blockTarget().matches(Blocks.AIR.defaultBlockState())) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.block").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 10;

            Component displayName = definition.blockTarget().getDisplayName();
            graphics.text(Minecraft.getInstance().font,
                    Component.literal("- ").append(displayName).withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Door
        if (definition.door().isValid()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.door").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Blocks Required
        if (definition.flags().blocksRequired()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.blocks_required").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Top Only
        if (definition.flags().topOnlyPlacement()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.top_only").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Side Only
        if (definition.flags().sideOnlyPlacement()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.side_only").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Override Existing
        if (definition.flags().overrideExistingBlocks()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.override_existing").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        //Un Waterlogs
        if (definition.flags().replaceWaterLoggedBlocks()) {
            graphics.text(Minecraft.getInstance().font, Component.translatable("tooptip.rooms.template.un_waterlog").withStyle(ChatFormatting.DARK_GRAY),
                    x + 94, y + 18 + index, 0xFFFFFFFF, false);
            index += 12;
        }

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;

        GuiStructureRenderState previewState = GuiStructureRenderState.simpleGuiRenderState(
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

        GuiRenderState stateObject =((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
        renderer.prepare(previewState, stateObject, Minecraft.getInstance().getWindow().getGuiScale());

        graphics.text(Minecraft.getInstance().font, Component.literal(templateId.getPath()), x - 100, y - 80, 0xFFFFFFFF, false);


        /*
        raphics.text(Minecraft.getInstance().font, Component.literal("Rotation: " + rotation.name()), settingsX,settingsY + 20, 0xFFFFFFFF,false);

        var def = TemplateData.DATA.get(templateId);

        if (def != null) {

            graphics.text(Minecraft.getInstance().font,Component.literal("Max Height: " +def.placement().maxHeight()),
                    settingsX, settingsY + 40, 0xFFFFFFFF, false);
        }

         */
    }

    private void applyTemplate() {

        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        stack.set(RoomsDataComponents.TEMPLATE_ID, templateId);
        ClientPacketDistributor.sendToServer(new SyncPlacerStack(stack));
        onClose();
    }

    @Override
    public void onClose() {

        if (renderer != null) {
            renderer.close();
        }

        Minecraft.getInstance().setScreen(null);
    }
}