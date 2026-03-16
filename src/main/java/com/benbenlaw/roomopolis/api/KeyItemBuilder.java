package com.benbenlaw.roomopolis.api;

import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.util.BlockTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

public class KeyItemBuilder {

    private Identifier templateId;
    private BlockTarget blockTarget;

    private int heightAdjustment = 0;
    private int frontAdjustment = 0;

    private boolean consumeKey = true;
    private boolean removeDoorArea = false;

    private boolean sideOnlyPlacement = false;
    private boolean topOnlyPlacement = false;

    private boolean blocksRequired = false;
    private boolean overrideExistingBlocks = false;
    private boolean replaceWaterLoggedBlocks = false;

    private int doorLeft = 0;
    private int doorRight = 0;
    private int doorUp = 0;
    private int doorDown = 0;

    private int maxHeight = 0;

    private Rotation rotation = Rotation.NONE;

    /**
     * Creates a new KeyItemBuilder instance.
     * @return a new KeyItemBuilder instance
     */
    public static KeyItemBuilder create() {
        return new KeyItemBuilder();
    }

    /**
     * Sets the template for the key item. This is required to build a RoomKeyDefinition.
     * Template takes a String ig "roomopolis:test_template" would be inside data/roomopolis/structure/test_template.nbt
     * @param id is String of template
     */
    public KeyItemBuilder template(String id) {
        this.templateId = Identifier.parse(id);
        return this;
    }

    /**
     * Clicked on block as a string,
     * @param id is String of block or block tag eg "minecraft:oak_planks", can be tag eg "#minecraft:planks"
     */
    public KeyItemBuilder block(String id) {
        this.blockTarget = BlockTarget.fromString(id);
        return this;
    }

    /**
     * Clicked on block as a Block
     * @param block is Block eg Blocks.OAK_PLANKS
     */
    public KeyItemBuilder block(Block block) {
        this.blockTarget = new BlockTarget.Single(block.defaultBlockState());
        return this;
    }

    /**
     * Clicked on block as a Block Tag
     * @param tag is TagKey<Block> BlockTags.PLANKS
     */
    public KeyItemBuilder block(TagKey<Block> tag) {
        this.blockTarget = new BlockTarget.Tag(tag);
        return this;
    }

    /**
     * Sets the door area around the key item. This is the area that will be cleared when the key item is used. The door area is defined by the number of blocks to the left, right, up and down of the key item. For example, a door area of (1, 1, 2, 0) would clear a 3x3 area around the key item, with 2 blocks above and no blocks below.
     * @param left
     * @param right
     * @param up
     * @param down
     * @return
     */
    public KeyItemBuilder door(int left, int right, int up, int down) {
        this.doorLeft = left;
        this.doorRight = right;
        this.doorUp = up;
        this.doorDown = down;
        return this;
    }

    /**
     * Sets whether the key item should be consumed when used.
     * @param value true if the key item should be consumed, false otherwise
     */
    public KeyItemBuilder consumeKey(boolean value) {
        this.consumeKey = value;
        return this;
    }

    /**
     * Sets whether the door area should be removed when the key item is used. If true, the blocks in the door area will be replaced with air when the key item is used. If false, the blocks in the door area will not be changed when the key item is used.
     * @param value true if the door area should be removed, false otherwise
     */
    public KeyItemBuilder removeDoorArea(boolean value) {
        this.removeDoorArea = value;
        return this;
    }

    /**
     * Sets whether the key item can only be placed on the side of a block. If true, the key item can only be placed on the side of a block. If false, the key item can be placed on any face of a block.
     * @param value true if the key item can only be placed on the side of a block, false otherwise
     */
    public KeyItemBuilder sideOnly(boolean value) {
        this.sideOnlyPlacement = value;
        return this;
    }

    /**
     * Sets whether the key item can only be placed on top of a block. If true, the key item can only be placed on top of a block. If false, the key item can be placed on any face of a block.
      * @param value true if the key item can only be placed on top of a block, false otherwise
     */
    public KeyItemBuilder topOnly(boolean value) {
        this.topOnlyPlacement = value;
        return this;
    }

    /**
     * Sets whether the key item requires the blocks in the template to be present in player inventory order to be used. If true, the key item can only be used if the blocks in the template are present. If false, the key item can be used regardless of whether the blocks in the template are present or not.
     * @param value true if the key item requires the blocks in the template to be present, false otherwise
     */
    public KeyItemBuilder blocksRequired(boolean value) {
        this.blocksRequired = value;
        return this;
    }

    /**
     * Forces the template to be placed overriding all blocks in the are.
      * @param value true if the template should override all blocks, false otherwise
     */
    public KeyItemBuilder overrideExistingBlocks(boolean value) {
        this.overrideExistingBlocks = value;
        return this;
    }

    /**
     * Templates if placed in water will make blocks become waterlogged when placed, enable this to revert the blocks to be un waterlogged.
     * @param value true if the template should replace waterlogged blocks with non waterlogged versions, false otherwise
     */
    public KeyItemBuilder replaceWaterLoggedBlocks(boolean value) {
        this.replaceWaterLoggedBlocks = value;
        return this;
    }

    /**
     * Moves the template up or down when placed. Positive values will move the template up, negative values will move the template down.
     * @param value is the amount to move the template up or down when placed
     */
    public KeyItemBuilder heightAdjustment(int value) {
        this.heightAdjustment = value;
        return this;
    }

    /**
     * Moves the template forward or backward when placed. Positive values will move the template forward, negative values will move the template backward. Forward and backward are determined by the direction the player is facing when placing the key item.
     * @param value is the amount to move the template forward or backward when placed
     */
    public KeyItemBuilder frontAdjustment(int value) {
        this.frontAdjustment = value;
        return this;
    }

    /**
     * Max height that the key item can be placed at. eg prevent placement above 200 blocks if structure when placed would exceed build height. A value of 0 means no max height.
     * @param value is the max height that the key item can be used at
     */
    public KeyItemBuilder maxHeight(int value) {
        this.maxHeight = value;
        return this;
    }

    /**
     * Rotates the template when placed
     * @param rotation is the rotation to place the template with
     * @return
     */
    public KeyItemBuilder rotation(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Rotates the template when placed
     * @param degrees is the rotation in degrees to place the template with. Must be a multiple of 90. Positive values rotate clockwise, negative values rotate counterclockwise.
     * @return
     */
    public KeyItemBuilder rotation(int degrees) {
        switch (degrees % 360) {
            case 90, -270 -> this.rotation = Rotation.CLOCKWISE_90;
            case 180, -180 -> this.rotation = Rotation.CLOCKWISE_180;
            case 270, -90 -> this.rotation = Rotation.COUNTERCLOCKWISE_90;
            default -> this.rotation = Rotation.NONE;
        }
        return this;
    }

    public RoomKeyDefinition build() {

        if (templateId == null) {
            throw new IllegalStateException("Template must be set");
        }

        return new RoomKeyDefinition(
                templateId,
                blockTarget,
                heightAdjustment,
                frontAdjustment,
                consumeKey,
                removeDoorArea,
                sideOnlyPlacement,
                topOnlyPlacement,
                blocksRequired,
                overrideExistingBlocks,
                replaceWaterLoggedBlocks,
                doorLeft,
                doorRight,
                doorUp,
                doorDown,
                maxHeight,
                rotation
        );
    }
}