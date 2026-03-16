package com.benbenlaw.roomopolis.integration.jei;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.util.BlockTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.gui.widgets.ScrollGridRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RoomCategory implements IRecipeCategory<RoomRecipe> {

    public final static Identifier TEXTURE = Roomopolis.identifier("textures/gui/jei_room_category.png");
    static final IRecipeType<RoomRecipe> RECIPE_TYPE = IRecipeType.create(Roomopolis.MOD_ID, "room_category", RoomRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    @Override
    public @Nullable Identifier getIdentifier(RoomRecipe recipe) {
        return recipe.templateId();
    }

    public RoomCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, getWidth(), getHeight());
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.TRIAL_KEY.asItem()));
    }

    @Override
    public IRecipeType<RoomRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.roomopolis.room_category");
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RoomRecipe recipe, IFocusGroup iFocusGroup) {

        builder.addSlot(RecipeIngredientRole.INPUT,1 ,1).add(recipe.keyItem());

        BlockTarget target = recipe.blockTarget();

        if (target != null) {

            List<ItemStack> stacksToRender = new ArrayList<>();

            // Single block
            if (target instanceof BlockTarget.Single(BlockState blockState)) {
                stacksToRender.add(new ItemStack(blockState.getBlock()));
            }
            // Tag of blocks
            else if (target instanceof BlockTarget.Tag(TagKey<Block> tag)) {
                BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(block ->
                        stacksToRender.add(new ItemStack(block.value().asItem()))
                );
            }

            // Add a JEI slot to render these blocks
            if (!stacksToRender.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 37, 1)
                        .addItemStacks(stacksToRender);
            }
        }

        if (recipe.requiresBlocks()) {

            Map<ItemStack, Integer> requiredBlocks = recipe.requiredItems();

            for (Map.Entry<ItemStack, Integer> entry : requiredBlocks.entrySet()) {
                ItemStack stack = entry.getKey().copy();
                int count = entry.getValue();
                stack.setCount(count);

                builder.addInputSlot().add(stack);
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RoomRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
        List<IRecipeSlotDrawable> inputSlots = recipeSlots.getSlots(RecipeIngredientRole.INPUT);

        inputSlots.removeFirst();

        IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(inputSlots, 1, 5);
        scrollGridWidget.setPosition(103, 6);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RoomRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        double left = 1;
        double top = 20;
        double right = 115;
        double bottom = 100;

        if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {

            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(recipe.templateId());

            if (optionalTemplate.isPresent()) {
                StructureTemplate template = optionalTemplate.get();
                int sizeX = template.getSize().getX();
                int sizeY = template.getSize().getY();
                int sizeZ = template.getSize().getZ();

                tooltip.add(Component.translatable("roomopolis.jei.tooltip.rotate", sizeX, sizeY, sizeZ));
            }

        }
    }


    @Override
    public void draw(RoomRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        Minecraft mc = Minecraft.getInstance();
        Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(recipe.templateId());
        if (optionalTemplate.isEmpty()) {
            guiGraphics.drawString(mc.font, Component.literal("BROKEN: " + recipe.templateId()), 5, 5, 0x404040, false);
            return;
        }

        StructureTemplate template = optionalTemplate.get();

        // ⚠ Create a proper 3D PoseStack for block rendering
        PoseStack poseStack = new PoseStack();

        double centerX = template.getSize().getX() / 2.0;
        double centerY = template.getSize().getY() / 2.0;
        double centerZ = template.getSize().getZ() / 2.0;
        float scale = 70f / Math.max(template.getSize().getX(),
                Math.max(template.getSize().getY(), template.getSize().getZ()));

        poseStack.pushPose();
        // Move to JEI render area
        poseStack.translate(70.0, 60.0, 0.0);
        poseStack.scale(scale, -scale, scale);

        float angle = (System.currentTimeMillis() % 10000L) / 10000.0F * 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

        for (StructureTemplate.StructureBlockInfo blockInfo : template.palettes.getFirst().blocks()) {
            BlockState state = blockInfo.state();
            if (state.isAir()) continue;

            BlockPos pos = blockInfo.pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - centerX, pos.getY() - centerY, pos.getZ() - centerZ);

            dispatcher.renderSingleBlock(state, poseStack, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, null, pos);
            poseStack.popPose();
        }

        buffer.endBatch();
        poseStack.popPose();
    }

    private void renderStructure(StructureTemplate template, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        double centerX = template.getSize().getX() / 2.0;
        double centerY = template.getSize().getY() / 2.0;
        double centerZ = template.getSize().getZ() / 2.0;

        float scale = 70f / Math.max(template.getSize().getX(),
                Math.max(template.getSize().getY(), template.getSize().getZ()));

        poseStack.pushPose();
        poseStack.translate(50, 60, 0);
        poseStack.scale(scale, -scale, scale);

        float angle = (System.currentTimeMillis() % 10000L) / 10000.0F * 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        for (StructureTemplate.StructureBlockInfo blockInfo : template.palettes.getFirst().blocks()) {
            BlockState state = blockInfo.state();
            if (state.isAir()) continue;

            BlockPos pos = blockInfo.pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - centerX, pos.getY() - centerY, pos.getZ() - centerZ);

            dispatcher.renderSingleBlock(state, poseStack, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, null, pos);
            poseStack.popPose();
        }

        buffer.endBatch();
        poseStack.popPose();
    }
}
