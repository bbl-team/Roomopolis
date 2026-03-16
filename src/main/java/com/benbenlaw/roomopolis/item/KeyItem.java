package com.benbenlaw.roomopolis.item;

import com.benbenlaw.roomopolis.api.RoomKeyDefinition;
import com.benbenlaw.roomopolis.util.BlockTarget;
import com.benbenlaw.roomopolis.util.DirectionUtil;
import com.benbenlaw.roomopolis.util.RoomopolisTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class KeyItem extends Item {

    private final RoomKeyDefinition definition;

    boolean isPlaced = false;

    public KeyItem(Properties properties, RoomKeyDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public RoomKeyDefinition definition() {
        return definition;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        Direction face = context.getClickedFace();
        Direction facing = face.getOpposite();

        Rotation baseRotation = DirectionUtil.getRotationFromDirection(face);
        Rotation rotation = combineRotation(baseRotation, definition.rotation());

        InteractionHand hand = context.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!hasEnoughBlocks(player, level)) {
            return InteractionResult.FAIL;
        }
        
        if (!stack.is(this)) {
            return InteractionResult.PASS;
        }

        //Is Correct Block
        BlockTarget target = definition.blockTarget();

        if (target != null && !target.matches(state)) {

            player.displayClientMessage(
                    Component.translatable("item.key.requires_key_block")
                            .withStyle(ChatFormatting.RED),
                    false
            );

            return InteractionResult.FAIL;
        }

        //Height Check
        int clickedY = pos.getY();

        if (definition.maxHeight() > 0 && clickedY >= definition.maxHeight()) {

            player.displayClientMessage(
                    Component.translatable(
                            "item.key.too_high",
                            definition.maxHeight()
                    ).withStyle(ChatFormatting.RED),
                    false
            );

            return InteractionResult.FAIL;
        }

        //Face checks
        if (definition.topOnlyPlacement() && face != Direction.UP) {

            player.displayClientMessage(
                    Component.translatable("item.key.top_only")
                            .withStyle(ChatFormatting.RED),
                    false
            );

            return InteractionResult.FAIL;
        }

        if (definition.sideOnlyPlacement() &&
                (face == Direction.UP || face == Direction.DOWN)) {

            player.displayClientMessage(
                    Component.translatable("item.key.side_only")
                            .withStyle(ChatFormatting.RED),
                    false
            );

            return InteractionResult.FAIL;
        }

        //Position tweaks
        BlockPos placePosition = pos;

        if (face == Direction.UP) {
            placePosition = pos.above(3);

            baseRotation = DirectionUtil.getRotationFromDirection(
                    context.getHorizontalDirection().getOpposite()
            );

            rotation = combineRotation(baseRotation, definition.rotation());
        }

        //Place
        createTemplate(level, rotation, facing, placePosition);

        if (!isPlaced) {

            player.displayClientMessage(
                    Component.translatable("item.key.area_not_empty")
                            .withStyle(ChatFormatting.RED),
                    false
            );

            return InteractionResult.FAIL;
        }

        player.displayClientMessage(
                Component.translatable("item.key.placed")
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        if (definition.removeDoorArea()) {

            Direction placementFacing =
                    (face.getAxis().isVertical())
                            ? context.getHorizontalDirection().getOpposite()
                            : face.getOpposite();

            removeDoor(level, pos, placementFacing);
        }

        consumeBlocks(player, level);

        if (definition.consumeKey()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    public boolean hasEnoughBlocks(Player player, Level level) {

        if (!definition.blocksRequired()) {
            return true;
        }

        if (player.isCreative()) {
            return true;
        }

        Map<Block, Integer> requiredBlocks = getRequiredBlocks(level);
        Map<Block, Integer> playerBlocks = new HashMap<>();
        Map<Block, Integer> missingBlocks = new HashMap<>();

        // Player Inventory
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                playerBlocks.put(block, playerBlocks.getOrDefault(block, 0) + stack.getCount());
            }
        }

        // Player Block amount check
        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
            Block block = entry.getKey();
            int requiredAmount = entry.getValue();
            int availableAmount = playerBlocks.getOrDefault(block, 0);

            if (availableAmount < requiredAmount) {
                int missingAmount = requiredAmount - availableAmount;
                missingBlocks.put(block, missingAmount);
            }
        }

        if (!missingBlocks.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.key.missing_blocks").withStyle(ChatFormatting.RED), false);

            for (Map.Entry<Block, Integer> entry : missingBlocks.entrySet()) {
                Block block = entry.getKey();
                int missingAmount = entry.getValue();

                // Stack Size checks
                int stacks = missingAmount / block.asItem().getDefaultInstance().getMaxStackSize();
                int remaining = missingAmount % block.asItem().getDefaultInstance().getMaxStackSize();

                // Construct the message
                MutableComponent message = Component.literal("- ")
                        .append(block.getName())
                        .append(": ")
                        .append(Component.literal(String.valueOf(missingAmount)));

                // Only append stack information if there are stacks
                if (stacks > 0) {
                    message = message.append(Component.literal(" ("))
                            .append(Component.literal(String.valueOf(stacks)))
                            .append(Component.literal(" stack"))
                            .append(stacks > 1 ? Component.literal("s") : Component.empty()) // Handle plural for stacks
                            .append(remaining > 0 ? Component.literal(" + " + remaining) : Component.empty()) // Add remaining items if any
                            .append(Component.literal(")"));
                }

                message = message.withStyle(ChatFormatting.YELLOW);

                player.displayClientMessage(message, false);
            }

            return false;
        }
        return true;
    }

    private void removeDoor(Level level, BlockPos centerPos, Direction horizontalFacing) {
        if (!horizontalFacing.getAxis().isHorizontal()) {
            // Fallback: assume NORTH if invalid
            horizontalFacing = Direction.NORTH;
        }

        Direction leftDir = horizontalFacing.getCounterClockWise().getOpposite();

        for (int x = -definition.doorLeft(); x <= definition.doorRight(); x++) {
            for (int y = -definition.doorDown(); y <= definition.doorUp(); y++) {
                BlockPos offset = centerPos.relative(leftDir, x).above(y);
                BlockState current = level.getBlockState(offset);
                if (!current.isAir()) {
                    level.setBlockAndUpdate(offset, Blocks.AIR.defaultBlockState());
                    level.sendBlockUpdated(offset, current, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    public Map<Block, Integer> getRequiredBlocks(Level level) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        Map<Block, Integer> halfCountMap = new HashMap<>();

        StructureTemplateManager structureManager = Objects.requireNonNull(level.getServer()).getStructureManager();
        Optional<StructureTemplate> optionalTemplate = structureManager.get(definition.templateId());

        if (optionalTemplate.isPresent()) {
            StructureTemplate.Palette palette = optionalTemplate.get().palettes.getFirst();

            for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                Block block = blockInfo.state().getBlock();
                if (block == Blocks.AIR) continue;

                if (block.builtInRegistryHolder().is(RoomopolisTags.Blocks.NOT_NEEDED_FOR_BLOCK_REQUIREMENTS)) {
                    continue;
                }

                if (block.builtInRegistryHolder().is(RoomopolisTags.Blocks.DOUBLE_BLOCKS)) {
                    halfCountMap.put(block, halfCountMap.getOrDefault(block, 0) + 1);
                    continue;
                }

                blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
            }

            for (Map.Entry<Block, Integer> entry : halfCountMap.entrySet()) {
                int adjusted = (entry.getValue() + 1) / 2;
                blockCounts.put(entry.getKey(), adjusted);
            }
        }

        return blockCounts;
    }

    public void consumeBlocks(Player player, Level level) {

        if (player.isCreative()) {
            return;
        }

        if (definition.blocksRequired()) {

            Map<Block, Integer> requiredBlocks = getRequiredBlocks(level);

            for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
                Block requiredBlock = entry.getKey();
                int requiredAmount = entry.getValue();

                for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
                    ItemStack stack = player.getInventory().getNonEquipmentItems().get(i);
                    if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == requiredBlock) {
                        int availableAmount = stack.getCount();

                        if (requiredAmount >= availableAmount) {
                            player.getInventory().getNonEquipmentItems().set(i, ItemStack.EMPTY);
                            requiredAmount -= availableAmount;
                        } else {
                            stack.shrink(requiredAmount);
                            requiredAmount = 0;
                        }

                        if (requiredAmount <= 0) break;
                    }
                }
            }
        }
    }

    public void createTemplate(Level level, Rotation rotation, Direction facing, BlockPos pos) {
        StructureTemplateManager structureManager = Objects.requireNonNull(level.getServer()).getStructureManager();
        Optional<StructureTemplate> optionalTemplate = structureManager.get(definition.templateId());

        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

        if (optionalTemplate.isPresent()) {
            // Template Information
            StructureTemplate template = optionalTemplate.get();

            Vec3i templateSize = KeyItemSizeCache.getTemplateSize(definition.templateId());

            if (templateSize == null) {
                System.err.println("Template size missing for " + definition.templateId());
                return;
            }

            // Position Adjustments to make the template spawn a block in front of the player and adjust the height of the template
            BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
            BlockPos adjustedOffset = StructureTemplate.calculateRelativePosition(placementSettings, centerOffset);
            int forwardShift = Math.max(templateSize.getX() / 2, 1) + 1 + definition.frontAdjustment();
            BlockPos forwardOffset = pos.relative(facing, forwardShift);
            BlockPos placementPos = forwardOffset.offset(adjustedOffset);
            placementPos = placementPos.above(definition.heightAdjustment());

            // Check if the location is empty (all air blocks)
            boolean isEmpty = true;


            if (!definition.overrideExistingBlocks()) {
                for (int x = 0; x < templateSize.getX(); x++) {
                    for (int y = 0; y < templateSize.getY(); y++) {
                        for (int z = 0; z < templateSize.getZ(); z++) {
                            BlockPos relPos = new BlockPos(x, y, z);
                            BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(placementSettings, relPos);
                            BlockPos worldPos = placementPos.offset(rotatedPos);

                            if (!level.getBlockState(worldPos).isAir() && !worldPos.equals(pos)) {
                                isEmpty = false;
                                break;
                            }
                        }
                        if (!isEmpty) break;
                    }
                    if (!isEmpty) break;
                }
            }

            if (isEmpty || definition.overrideExistingBlocks()) {
                // Place the template if the location is empty
                template.placeInWorld((ServerLevelAccessor) level, placementPos, placementPos, placementSettings, level.getRandom(), Block.UPDATE_ALL);
                isPlaced = true;

                // Un-waterlog blocks if option is enabled
                if (definition.replaceWaterLoggedBlocks()) {
                    unWaterLogPlacedBlocks(level, placementPos, placementSettings);
                }

                // Update all placed blocks
                for (int x = 0; x < templateSize.getX(); x++) {
                    for (int y = 0; y < templateSize.getY(); y++) {
                        for (int z = 0; z < templateSize.getZ(); z++) {
                            BlockPos relPos = new BlockPos(x, y, z);
                            BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(placementSettings, relPos);
                            BlockPos worldPos = placementPos.offset(rotatedPos);

                            level.sendBlockUpdated(worldPos, level.getBlockState(worldPos), level.getBlockState(worldPos), Block.UPDATE_ALL);
                        }
                    }
                }
            } else {
                System.out.println("Target location is not empty. Structure placement aborted.");
            }

        } else {
            System.out.println("Structure not found: " + definition.templateId());
        }
    }

    private void unWaterLogPlacedBlocks(Level level, BlockPos placementPos, StructurePlaceSettings settings) {
        Vec3i templateSize = KeyItemSizeCache.getTemplateSize(definition.templateId());

        if (templateSize == null) {
            System.err.println("Template size missing for " + definition.templateId());
            return;
        }

        for (int x = 0; x < templateSize.getX(); x++) {
            for (int y = 0; y < templateSize.getY(); y++) {
                for (int z = 0; z < templateSize.getZ(); z++) {

                    BlockPos relPos = new BlockPos(x, y, z);
                    BlockPos rotatedPos =
                            StructureTemplate.calculateRelativePosition(settings, relPos);
                    BlockPos worldPos = placementPos.offset(rotatedPos);

                    BlockState state = level.getBlockState(worldPos);

                    if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                            && state.getValue(BlockStateProperties.WATERLOGGED)) {

                        level.setBlock(
                                worldPos,
                                state.setValue(BlockStateProperties.WATERLOGGED, false),
                                Block.UPDATE_ALL
                        );
                    }
                }
            }
        }
    }

    private Rotation combineRotation(Rotation base, Rotation extra) {

        int baseDeg = switch (base) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> 270;
        };

        int extraDeg = switch (extra) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> 270;
        };

        int total = (baseDeg + extraDeg) % 360;

        return switch (total) {
            case 90 -> Rotation.CLOCKWISE_90;
            case 180 -> Rotation.CLOCKWISE_180;
            case 270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        Player player = (Player) owner;
        if (player.getMainHandItem().getItem() instanceof KeyItem) {
            if (definition.overrideExistingBlocks()) {
                player.displayClientMessage(Component.translatable("message.key.overrides_blocks")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
    }
}


