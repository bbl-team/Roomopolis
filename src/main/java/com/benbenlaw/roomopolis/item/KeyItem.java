package com.benbenlaw.roomopolis.item;

import com.benbenlaw.roomopolis.util.DirectionUtil;
import com.benbenlaw.roomopolis.util.RoomopolisTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class KeyItem extends Item {

    public ResourceLocation templateId;
    public int heightAdjustment;
    public int frontAdjustment;
    public Optional<Block> keyBlock;
    public Optional<TagKey<Block>> keyBlockTag;
    public boolean isPlaced;
    public boolean consumeKey;
    public Vec3i templateSize;
    public boolean removeDoorArea;
    public boolean sideOnlyPlacement;
    public boolean topOnlyPlacement;
    public boolean blocksRequired;
    public boolean overrideExistingBlocks;
    public boolean replaceWaterLoggedBlocks = false;
    public int doorLeft;
    public int doorRight;
    public int doorUp;
    public int doorDown;
    public int maxHeight;

    public KeyItem(Properties properties, String templateId, int heightAdjustment, int frontAdjustment, String keyBlock, boolean consumeKey,
                   boolean removeDoorArea, boolean sideOnlyPlacement, boolean topOnlyPlacement, boolean blocksRequired, boolean overrideExistingBlocks,
                   int doorLeft, int doorRight, int doorUp, int doorDown, int requiresHeight) {
        super(properties);
        this.templateId = ResourceLocation.parse(templateId);
        this.heightAdjustment = heightAdjustment;
        this.consumeKey = consumeKey;
        this.frontAdjustment = frontAdjustment;
        this.removeDoorArea = removeDoorArea;
        this.sideOnlyPlacement = sideOnlyPlacement;
        this.topOnlyPlacement = topOnlyPlacement;
        this.blocksRequired = blocksRequired;
        this.overrideExistingBlocks = overrideExistingBlocks;

        this.doorLeft = doorLeft;
        this.doorRight = doorRight;
        this.doorUp = doorUp;
        this.doorDown = doorDown;

        this.maxHeight = requiresHeight;

        if (keyBlock == null || keyBlock.isEmpty()) {
            this.keyBlock = Optional.empty();
            this.keyBlockTag = Optional.empty();
        } else {
            if (keyBlock.startsWith("#")) {
                this.keyBlock = Optional.empty();
                this.keyBlockTag = Optional.of(TagKey.create(Registries.BLOCK, ResourceLocation.parse(keyBlock.substring(1))));
            } else {
                this.keyBlock = Optional.of(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(keyBlock)));
                this.keyBlockTag = Optional.empty();
            }
        }
    }

    public KeyItem replaceWaterLoggedBlocks(boolean replaceWaterLoggedBlocks) {
        this.replaceWaterLoggedBlocks = true;
        return this;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Rotation rotation = DirectionUtil.getRotationFromDirection(context.getClickedFace());

        Direction facing = context.getClickedFace().getOpposite();// getHorizontalDirection();

        InteractionHand hand = context.getHand();
        assert player != null;
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {

            if (!hasEnoughBlocks(player, level)) {
                return InteractionResult.FAIL;
            }

            templateSize = KeyItemSizeCache.getTemplateSize(templateId);

            if (player.getItemInHand(hand).is(this)) {

                if (keyBlock.isPresent() || keyBlockTag.isPresent()) {
                    if ((keyBlock.isPresent() && state.is(keyBlock.get())) || (keyBlockTag.isPresent() && state.is(keyBlockTag.get()))) {

                        int clickedOnY = pos.getY();
                        if (maxHeight <= clickedOnY) {
                            player.sendSystemMessage(Component.translatable("item.key.too_high", maxHeight).withStyle(ChatFormatting.RED));
                            return InteractionResult.FAIL;
                        }

                        if (topOnlyPlacement && context.getClickedFace() != Direction.UP) {
                            player.sendSystemMessage(Component.translatable("item.key.top_only").withStyle(ChatFormatting.RED));
                            return InteractionResult.FAIL;
                        }

                        if (sideOnlyPlacement && (context.getClickedFace() == Direction.UP || context.getClickedFace() == Direction.DOWN)) {
                            player.sendSystemMessage(Component.translatable("item.key.side_only").withStyle(ChatFormatting.RED));
                            return InteractionResult.FAIL;
                        }

                        BlockPos placePosition = pos;

                        if (context.getClickedFace() == Direction.UP) {
                            placePosition = new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ());
                            rotation = DirectionUtil.getRotationFromDirection(context.getHorizontalDirection().getOpposite());
                            removeDoorArea = false;
                        }

                        if (isStructureTooLarge()) {
                            player.sendSystemMessage(Component.translatable("item.key.too_large").withStyle(ChatFormatting.RED));
                            return InteractionResult.FAIL;
                        }

                        createTemplate(level, rotation, facing, placePosition);

                        if (isPlaced) {
                            player.sendSystemMessage(Component.translatable("item.key.placed").withStyle(ChatFormatting.GREEN));
                            if (removeDoorArea) {
                                Direction placementFacing = (context.getClickedFace().getAxis().isVertical())
                                        ? context.getHorizontalDirection().getOpposite()
                                        : context.getClickedFace().getOpposite();

                                removeDoor(level, pos, placementFacing);
                            }
                            consumeBlocks(player, level);
                            if (consumeKey) {
                                player.getItemInHand(hand).shrink(1);
                            }
                        } else {
                            player.sendSystemMessage(Component.translatable("item.key.area_not_empty").withStyle(ChatFormatting.RED));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("item.key.requires_key_block",
                                keyBlock.map(Block::getName).orElse(Component.literal("Unknown Block"))).withStyle(ChatFormatting.RED));
                    }
                } else {
                    if (context.getClickedFace() == Direction.DOWN) {
                        player.sendSystemMessage(Component.translatable("item.key.invalid_placement").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }

                    BlockPos placePosition = pos;

                    if (context.getClickedFace() == Direction.UP) {
                        placePosition = new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ());
                        rotation = DirectionUtil.getRotationFromDirection(context.getHorizontalDirection().getOpposite());
                    }

                    if (isStructureTooLarge()) {
                        player.sendSystemMessage(Component.translatable("item.key.too_large").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }

                    createTemplate(level, rotation, facing, placePosition);

                    if (isPlaced) {
                        player.sendSystemMessage(Component.translatable("item.key.placed").withStyle(ChatFormatting.GREEN));
                        if (consumeKey) {
                            player.getItemInHand(hand).shrink(1);
                        }
                        consumeBlocks(player, level);
                    } else {
                        player.sendSystemMessage(Component.translatable("item.key.area_not_empty").withStyle(ChatFormatting.RED));
                    }
                }
            }
        }

        isPlaced = false;
        return super.useOn(context);
    }

    public Map<Block, Integer> getRequiredBlocks(Level level) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        Map<Block, Integer> halfCountMap = new HashMap<>();

        StructureTemplateManager structureManager = Objects.requireNonNull(level.getServer()).getStructureManager();
        Optional<StructureTemplate> optionalTemplate = structureManager.get(templateId);

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

        if (blocksRequired) {

            Map<Block, Integer> requiredBlocks = getRequiredBlocks(level);

            for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
                Block requiredBlock = entry.getKey();
                int requiredAmount = entry.getValue();

                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    ItemStack stack = player.getInventory().items.get(i);
                    if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == requiredBlock) {
                        int availableAmount = stack.getCount();

                        if (requiredAmount >= availableAmount) {
                            player.getInventory().items.set(i, ItemStack.EMPTY);
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


    public boolean hasEnoughBlocks(Player player, Level level) {

        if (!blocksRequired) {
            return true;
        }

        if (player.isCreative()) {
            return true;
        }

        Map<Block, Integer> requiredBlocks = getRequiredBlocks(level);
        Map<Block, Integer> playerBlocks = new HashMap<>();
        Map<Block, Integer> missingBlocks = new HashMap<>();

        // Player Inventory
        for (ItemStack stack : player.getInventory().items) {
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
            player.sendSystemMessage(Component.translatable("item.key.missing_blocks").withStyle(ChatFormatting.RED));

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

                player.sendSystemMessage(message);
            }

            return false;
        }
        return true;
    }


    private boolean isStructureTooLarge() {
        int sizeX = templateSize.getX();
        int sizeY = templateSize.getY();
        int sizeZ = templateSize.getZ();

        return sizeX > 48 || sizeY > 48 || sizeZ > 48;
    }

    public void createTemplate(Level level, Rotation rotation, Direction facing, BlockPos pos) {
        StructureTemplateManager structureManager = Objects.requireNonNull(level.getServer()).getStructureManager();
        Optional<StructureTemplate> optionalTemplate = structureManager.get(templateId);

        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

        if (optionalTemplate.isPresent()) {
            // Template Information
            StructureTemplate template = optionalTemplate.get();

            // Position Adjustments to make the template spawn a block in front of the player and adjust the height of the template
            BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
            BlockPos adjustedOffset = StructureTemplate.calculateRelativePosition(placementSettings, centerOffset);
            int forwardShift = Math.max(templateSize.getX() / 2, 1) + 1 + frontAdjustment;
            BlockPos forwardOffset = pos.relative(facing, forwardShift);
            BlockPos placementPos = forwardOffset.offset(adjustedOffset);
            placementPos = placementPos.above(heightAdjustment);

            // Check if the location is empty (all air blocks)
            boolean isEmpty = true;


            if (!overrideExistingBlocks) {
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

            if (isEmpty || overrideExistingBlocks) {
                // Place the template if the location is empty
                template.placeInWorld((ServerLevelAccessor) level, placementPos, placementPos, placementSettings, level.getRandom(), Block.UPDATE_ALL);
                isPlaced = true;

                // Un-waterlog blocks if option is enabled
                if (replaceWaterLoggedBlocks) {
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
            System.out.println("Structure not found: " + templateId);
        }
    }

    private void unWaterLogPlacedBlocks(Level level, BlockPos placementPos, StructurePlaceSettings settings) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Map<Block, Integer> blockMap = KeyItemPaletteCache.getTemplatePalette(templateId);
        Player player = Minecraft.getInstance().player;

        if (Screen.hasShiftDown()) {

            if (templateSize == null && Minecraft.getInstance().player != null) {
                templateSize = KeyItemSizeCache.getTemplateSize(templateId);
            }

            if (consumeKey) {
                tooltipComponents.add(Component.translatable("tooltips.key.consume_key").withStyle(ChatFormatting.GRAY));
            } else {
                tooltipComponents.add(Component.translatable("tooltips.key.retain_key").withStyle(ChatFormatting.GRAY));
            }

            if (overrideExistingBlocks) {
                tooltipComponents.add(Component.translatable("tooltips.key.override_existing_blocks").withStyle(ChatFormatting.GRAY));
            } else {
                tooltipComponents.add(Component.translatable("tooltips.key.normal_checks").withStyle(ChatFormatting.GRAY));
            }

            if (removeDoorArea) {
                tooltipComponents.add(Component.translatable("tooltips.key.remove_door_area",
                        doorLeft, doorRight, doorUp, doorDown).withStyle(ChatFormatting.GRAY));
            }

            if (sideOnlyPlacement) {
                tooltipComponents.add(Component.translatable("tooltips.key.side_only").withStyle(ChatFormatting.GRAY));
            }

            if (topOnlyPlacement) {
                tooltipComponents.add(Component.translatable("tooltips.key.top_only").withStyle(ChatFormatting.GRAY));
            }

            if (templateSize != null) {
                Component templateSizeText = Component.translatable("tooltips.key.template_size",
                        templateSize.getX(), templateSize.getY(), templateSize.getZ()).withStyle(ChatFormatting.GRAY);
                tooltipComponents.add(templateSizeText);
            }

            keyBlock.ifPresent(block -> tooltipComponents.add(Component.translatable("tooltips.key.requires_key_block", block.getName()).withStyle(ChatFormatting.RED)));

            if (keyBlockTag.isPresent()) {
                String tag = keyBlockTag.get().location().toString();
                tooltipComponents.add(Component.translatable("tooltips.key.requires_key_block", tag).withStyle(ChatFormatting.RED));
            }

        } else {
            tooltipComponents.add(Component.translatable("tooltips.roomopolis.shift").withStyle(ChatFormatting.YELLOW));
        }

        // Add List
        if (blocksRequired) {
            if (Screen.hasAltDown()) {
                if (blockMap != null && player != null) {
                    Map<Block, Integer> playerBlocks = new HashMap<>();

                    // Count blocks in the player's inventory
                    for (ItemStack itemStack : player.getInventory().items) {
                        if (itemStack.getItem() instanceof BlockItem blockItem) {
                            Block block = blockItem.getBlock();
                            playerBlocks.put(block, playerBlocks.getOrDefault(block, 0) + itemStack.getCount());
                        }
                    }

                    tooltipComponents.add(Component.translatable("tooltips.key.required_blocks").withStyle(ChatFormatting.GRAY));

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

                        tooltipComponents.add(Component.literal(tooltipText)
                                .withStyle(color));
                    }

                }

            } else {
                tooltipComponents.add(Component.translatable("tooltips.roomopolis.alt").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private void removeDoor(Level level, BlockPos centerPos, Direction horizontalFacing) {
        if (!horizontalFacing.getAxis().isHorizontal()) {
            // Fallback: assume NORTH if invalid
            horizontalFacing = Direction.NORTH;
        }

        Direction leftDir = horizontalFacing.getCounterClockWise().getOpposite();

        for (int x = -doorLeft; x <= doorRight; x++) {
            for (int y = -doorDown; y <= doorUp; y++) {
                BlockPos offset = centerPos.relative(leftDir, x).above(y);
                BlockState current = level.getBlockState(offset);
                if (!current.isAir()) {
                    level.setBlockAndUpdate(offset, Blocks.AIR.defaultBlockState());
                    level.sendBlockUpdated(offset, current, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {

        Player player = (Player) entity;
        if (player.getMainHandItem().getItem() instanceof KeyItem keyItem) {
            if (keyItem.overrideExistingBlocks) {
                player.displayClientMessage(Component.translatable("message.key.overrides_blocks")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
