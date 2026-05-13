package com.benbenlaw.roomopolis.item;

import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.screen.ClientScreens;
import com.benbenlaw.roomopolis.loader.options.BlockTarget;
import com.benbenlaw.roomopolis.util.DirectionUtil;
import com.benbenlaw.roomopolis.util.RoomopolisTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.*;

public class PlacerItem extends Item {

    public PlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ClientScreens.openPlacerScreen();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();

        TemplateDefinition definition = getDefinition(stack);

        if (definition == null) {
            player.sendOverlayMessage(Component.translatable("tooltip.rooms.missing_tempalte").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction face = context.getClickedFace();
        Direction facing = face.getOpposite();
        Rotation baseRotation = DirectionUtil.getRotationFromDirection(face);
        Rotation rotation = combineRotation(baseRotation, definition.placement().rotation());

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!hasEnoughBlocks(player, level, definition)) {
            return InteractionResult.FAIL;
        }

        BlockTarget target = definition.blockTarget();
        if (target != null && !(target instanceof BlockTarget.Any) && !target.matches(state)) {

            player.sendOverlayMessage(Component.translatable("item.key.requires_key_block").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (definition.placement().maxHeight() > 0 && pos.getY() >= definition.placement().maxHeight()) {

            player.sendOverlayMessage(Component.translatable("item.key.too_high", definition.placement().maxHeight()).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (definition.placement().topOnlyPlacement() && face != Direction.UP) {

            player.sendOverlayMessage(Component.translatable("item.key.top_only").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (definition.placement().sideOnlyPlacement() && face.getAxis().isVertical()) {

            player.sendOverlayMessage(Component.translatable("item.key.side_only").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        BlockPos placePosition = pos;

        if (face == Direction.UP) {

            placePosition = pos.above(3);
            baseRotation = DirectionUtil.getRotationFromDirection(context.getHorizontalDirection().getOpposite());
            rotation = combineRotation(baseRotation, definition.placement().rotation());
        }

        if (definition.restrictions().itemRequiredAndConsumed().isPresent()) {

            ItemStack required = definition.restrictions().itemRequiredAndConsumed().get().create();
            boolean found = false;

            for (ItemStack invStack : player.getInventory().getNonEquipmentItems()) {
                if (ItemStack.isSameItem(invStack, required) && invStack.getCount() >= required.getCount()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                player.sendOverlayMessage(Component.translatable("item.key.requires_item", required.getDisplayName()).withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }


        boolean placed = createTemplate(level, rotation, facing, placePosition, definition);

        if (!placed) {
            player.sendOverlayMessage(Component.translatable("item.key.area_not_empty").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        player.sendOverlayMessage(Component.translatable("item.key.placed").withStyle(ChatFormatting.GREEN));

        if (definition.door().isValid()) {

            Direction placementFacing = face.getAxis().isVertical() ? context.getHorizontalDirection().getOpposite(): face.getOpposite();
            removeDoor(level, pos, placementFacing, definition);
        }

        consumeBlocks(player, level, definition);
        return InteractionResult.SUCCESS;
    }

    private TemplateDefinition getDefinition(ItemStack stack) {
        Identifier id = stack.get(RoomsDataComponents.TEMPLATE_ID);
        if (id == null) return null;
        return TemplateData.DATA.get(id);
    }

    public boolean hasEnoughBlocks(Player player, Level level, TemplateDefinition definition) {

        if (!definition.restrictions().blocksRequired()) return true;
        if (player.isCreative()) return true;

        Map<Block, Integer> required = getRequiredBlocks(level, definition);

        Map<Block, Integer> playerBlocks = new HashMap<>();

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof BlockItem blockItem) {

                Block block = blockItem.getBlock();
                playerBlocks.put(block, playerBlocks.getOrDefault(block, 0) + stack.getCount());
            }
        }

        for (Map.Entry<Block, Integer> entry : required.entrySet()) {

            if (playerBlocks.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                player.sendOverlayMessage(Component.translatable("item.key.missing_blocks").withStyle(ChatFormatting.RED));
                return false;
            }
        }

        return true;
    }

    public Map<Block, Integer> getRequiredBlocks(Level level, TemplateDefinition definition) {

        Map<Block, Integer> counts = new HashMap<>();
        Map<Block, Integer> half = new HashMap<>();

        StructureTemplate.Palette palette;

        if (level.isClientSide()) {

            var optional =
                    FakeStructureTemplateManager.INSTANCE.get(definition.templateId());

            if (optional.isEmpty()) {
                return counts;
            }

            StructureTemplate template = optional.get();

            if (template.palettes.isEmpty()) {
                return counts;
            }

            palette = template.palettes.getFirst();

        } else {

            StructureTemplateManager manager =
                    Objects.requireNonNull(level.getServer())
                            .getStructureManager();

            Optional<StructureTemplate> optional =
                    manager.get(definition.templateId());

            if (optional.isEmpty()) {
                return counts;
            }

            StructureTemplate template = optional.get();

            if (template.palettes.isEmpty()) {
                return counts;
            }

            palette = template.palettes.getFirst();
        }

        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {

            Block block = info.state().getBlock();

            Map<Block, Block> activePalette =
                    TemplateData.getActivePalette(Minecraft.getInstance().player.getUUID(), definition.templateId());

            Block replacement = activePalette.get(block);

            if (replacement != null) {
                block = replacement;
            }

            if (block == Blocks.AIR) continue;
            if (block.defaultBlockState().typeHolder().is(RoomopolisTags.Blocks.NOT_NEEDED_FOR_BLOCK_REQUIREMENTS)) continue;
            if (block.defaultBlockState().typeHolder().is(RoomopolisTags.Blocks.DOUBLE_BLOCKS)) {
                half.put(block, half.getOrDefault(block, 0) + 1);
                continue;
            }

            counts.put(block, counts.getOrDefault(block, 0) + 1);
        }

        for (Map.Entry<Block, Integer> e : half.entrySet()) {

            counts.put(e.getKey(), (e.getValue() + 1) / 2);
        }

        return counts;
    }

    public void consumeBlocks(Player player, Level level, TemplateDefinition definition) {

        if (player.isCreative()) return;
        if (!definition.restrictions().blocksRequired()) return;

        Map<Block, Integer> required = getRequiredBlocks(level, definition);

        for (Map.Entry<Block, Integer> entry : required.entrySet()) {

            Block block = entry.getKey();
            int needed = entry.getValue();

            for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
                ItemStack stack = player.getInventory().getNonEquipmentItems().get(i);

                if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {

                    int count = stack.getCount();
                    if (needed >= count) {
                        player.getInventory().getNonEquipmentItems().set(i, ItemStack.EMPTY);
                        needed -= count;

                    } else {
                        stack.shrink(needed);
                        needed = 0;
                    }

                    if (needed <= 0) {
                        break;
                    }
                }
            }
        }
    }

    public boolean createTemplate(Level level, Rotation rotation, Direction facing, BlockPos pos, TemplateDefinition definition) {

        StructureTemplateManager manager = Objects.requireNonNull(level.getServer()).getStructureManager();
        Optional<StructureTemplate> optional = manager.get(definition.templateId());

        if (optional.isEmpty()) return false;

        StructureTemplate template = optional.get();
        if (template.palettes.isEmpty()) return false;

        StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setRotation(rotation)
                        .setMirror(Mirror.NONE)
                        .setIgnoreEntities(false);

        Vec3i size = template.getSize();

        BlockPos centerOffset = new BlockPos(-size.getX() / 2, -size.getY() / 2, -size.getZ() / 2);
        BlockPos adjusted = StructureTemplate.calculateRelativePosition(settings, centerOffset);
        int forward = Math.max(size.getX() / 2, 1) + 1 + definition.placement().frontAdjustment();
        BlockPos finalPos = pos.relative(facing, forward).offset(adjusted).above(definition.placement().heightAdjustment());
        boolean canPlace = true;

        if (!definition.restrictions().overrideExistingBlocks()) {

            StructurePlaceSettings testSettings = new StructurePlaceSettings()
                            .setRotation(rotation)
                            .setMirror(Mirror.NONE);

            for (StructureTemplate.StructureBlockInfo info : template.palettes.getFirst().blocks()) {

                if (info.state().isAir()) continue;
                BlockPos worldPos = StructureTemplate.calculateRelativePosition(testSettings, info.pos()).offset(finalPos);
                BlockState existing = level.getBlockState(worldPos);
                if (!existing.isAir() && !existing.canBeReplaced()) return false;
            }
        }

        if (!canPlace) {
            return false;
        }

        template.placeInWorld((ServerLevelAccessor) level, finalPos, finalPos, settings, level.getRandom(), Block.UPDATE_ALL);

        Map<Block, Block> palette = TemplateData.getActivePalette(Minecraft.getInstance().player.getUUID(), definition.templateId());

        if (!palette.isEmpty()) {

            for (int x = 0; x < size.getX(); x++) {
                for (int y = 0; y < size.getY(); y++) {
                    for (int z = 0; z < size.getZ(); z++) {

                        BlockPos rel = new BlockPos(x, y, z);
                        BlockPos rotated = StructureTemplate.calculateRelativePosition(settings, rel);
                        BlockPos worldPos = finalPos.offset(rotated);

                        BlockState state = level.getBlockState(worldPos);
                        Block block = state.getBlock();

                        Block replacement = palette.get(block);

                        if (replacement != null && replacement != block) {
                            level.setBlock(worldPos,
                                    replacement.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }

        if (definition.restrictions().replaceWaterLoggedBlocks()) {
            unWaterLogPlacedBlocks(level,finalPos,settings,definition);
        }

        if (definition.restrictions().itemRequiredAndConsumed().isPresent()) {
            ItemStack required = definition.restrictions().itemRequiredAndConsumed().get().create();

            for (ItemStack invStack : Objects.requireNonNull(level.getPlayerByUUID(Minecraft.getInstance().player.getUUID())).getInventory().getNonEquipmentItems()) {
                if (ItemStack.isSameItem(invStack, required) && invStack.getCount() >= required.getCount()) {
                    invStack.shrink(required.getCount());
                    break;
                }
            }
        }

        return true;
    }

    private void removeDoor(Level level, BlockPos centerPos, Direction facing, TemplateDefinition definition) {

        Direction left = facing.getCounterClockWise().getOpposite();

        for (int x = -definition.door().left();
             x <= definition.door().right();
             x++) {

            for (int y = -definition.door().down();
                 y <= definition.door().up();y++) {

                BlockPos offset = centerPos.relative(left, x).above(y);

                if (!level.getBlockState(offset).isAir()) {
                    level.setBlockAndUpdate(offset, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    public static Rotation combineRotation(Rotation base, Rotation extra) {

        int b = switch (base) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> 270;
        };

        int e = switch (extra) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> 270;
        };

        int total = (b + e) % 360;

        return switch (total) {
            case 90 -> Rotation.CLOCKWISE_90;
            case 180 -> Rotation.CLOCKWISE_180;
            case 270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private void unWaterLogPlacedBlocks(Level level, BlockPos placementPos, StructurePlaceSettings settings, TemplateDefinition definition) {

        Vec3i templateSize = TemplateSizeCache.getTemplateSize(definition.templateId());

        if (templateSize == null) return;

        for (int x = 0; x < templateSize.getX(); x++) {
            for (int y = 0; y < templateSize.getY(); y++) {
                for (int z = 0; z < templateSize.getZ(); z++) {

                    BlockPos relPos = new BlockPos(x, y, z);
                    BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(settings, relPos);
                    BlockPos worldPos = placementPos.offset(rotatedPos);
                    BlockState state = level.getBlockState(worldPos);

                    if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {

                        level.setBlock(worldPos,state.setValue(BlockStateProperties.WATERLOGGED,false),Block.UPDATE_ALL);
                    }
                }
            }
        }
    }
}