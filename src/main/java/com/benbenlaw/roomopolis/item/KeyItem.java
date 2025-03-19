package com.benbenlaw.roomopolis.item;

import com.benbenlaw.core.item.TooltipUtil;
import com.benbenlaw.core.util.DirectionUtil;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import com.benbenlaw.roomopolis.network.packet.GetStructureSizePacket;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KeyItem extends Item {

    public ResourceLocation templateId;
    public int heightAdjustment;
    public int frontAdjustment;
    public Optional<Block> keyBlock;
    public Optional<TagKey<Block>> keyBlockTag;
    public boolean isPlaced;
    public boolean consumeKey;
    public Vec3i templateSize;

    public KeyItem(Properties properties, String templateId, int heightAdjustment, int frontAdjustment, String keyBlock, boolean consumeKey) {
        super(properties);
        this.templateId = ResourceLocation.parse(templateId);
        this.heightAdjustment = heightAdjustment;
        this.consumeKey = consumeKey;
        this.frontAdjustment = frontAdjustment;

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


    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Rotation rotation = DirectionUtil.getRotationFromDirection(context.getClickedFace());
        Direction facing = context.getHorizontalDirection();
        InteractionHand hand = context.getHand();
        boolean removeDoorArea = true;

        if (!level.isClientSide()) {
            assert player != null;
            if (player.getItemInHand(hand).is(this)) {

                if (keyBlock.isPresent() || keyBlockTag.isPresent()) {
                    if ((keyBlock.isPresent() && state.is(keyBlock.get()))
                            || (keyBlockTag.isPresent() && state.is(keyBlockTag.get()))) {

                        System.out.println("Tag Found: " + keyBlockTag.orElse(null)); // Debug statement

                        if (context.getClickedFace() == Direction.DOWN) {
                            player.sendSystemMessage(Component.translatable("item.key.invalid_placement").withStyle(ChatFormatting.RED));
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
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
                                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
                                level.sendBlockUpdated(pos.below(), level.getBlockState(pos.below()), level.getBlockState(pos.below()), Block.UPDATE_ALL);
                            }
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
                    } else {
                        player.sendSystemMessage(Component.translatable("item.key.area_not_empty").withStyle(ChatFormatting.RED));
                    }
                }
            }
        }

        isPlaced = false;
        return super.useOn(context);
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
            templateSize = template.getSize();

            // Position Adjustments to make the template spawn a block in front of the player and adjust the height of the template
            BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
            BlockPos adjustedOffset = StructureTemplate.calculateRelativePosition(placementSettings, centerOffset);
            int forwardShift = (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180)
                    ? templateSize.getZ() / 2 + 1 + frontAdjustment
                    : templateSize.getX() / 2 + 1 + frontAdjustment;
            BlockPos forwardOffset = pos.relative(facing, forwardShift);
            BlockPos placementPos = forwardOffset.offset(adjustedOffset);
            placementPos = placementPos.above(heightAdjustment);

            // Check if the location is empty (all air blocks)
            boolean isEmpty = true;
            for (int x = 0; x < templateSize.getX(); x++) {
                for (int y = 0; y < templateSize.getY(); y++) {
                    for (int z = 0; z < templateSize.getZ(); z++) {
                        BlockPos relPos = new BlockPos(x, y, z);
                        BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(placementSettings, relPos);
                        BlockPos worldPos = placementPos.offset(rotatedPos);

                        // Check if the block at the world position is not air
                        if (!level.getBlockState(worldPos).isAir()) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (!isEmpty) {
                        break;
                    }
                }
                if (!isEmpty) {
                    break;
                }
            }

            if (isEmpty) {
                // Place the template if the location is empty
                template.placeInWorld((ServerLevelAccessor) level, placementPos, placementPos, placementSettings, level.getRandom(), Block.UPDATE_ALL);
                isPlaced = true;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (templateSize == null && Minecraft.getInstance().player != null) {
            PacketDistributor.sendToServer(new GetStructureSizePayload(templateId.toString()));
            templateSize = KeyItemSizeCache.getTemplateSize(templateId);
        }

        if (consumeKey) {
            tooltipComponents.add(Component.translatable("tooltips.key.consume_key").withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltips.key.retain_key").withStyle(ChatFormatting.GRAY));
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
    }





}
