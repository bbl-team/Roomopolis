package com.benbenlaw.roomopolis.integration.kubejs;

import com.benbenlaw.roomopolis.item.KeyItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class KeyItemBuilder extends ItemBuilder {

    private String templateId;
    private int heightAdjustment;
    private int frontAdjustment = 0;
    private String keyBlock;
    private boolean consumeKey = true;
    private boolean removeDoor = true;
    private boolean sideOnlyPlacement = true;
    private boolean blocksRequired = false;
    private boolean overrideExistingBlocks = false;
    public int doorLeft;
    public int doorRight;
    public int doorUp;
    public int doorDown;

    public KeyItemBuilder(ResourceLocation i) {
        super(i);
    }

    @Info("Resource location of the template eg roomopolis:tiny_room")
    public KeyItemBuilder templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    @Info("The height adjustment for the template to be placed")
    public KeyItemBuilder heightAdjustment(int heightAdjustment) {
        this.heightAdjustment = heightAdjustment;
        return this;
    }

    @Info("The front adjustment for the template to be placed in front or behind the player -2 should be center of the player")
    public KeyItemBuilder frontAdjustment(int frontAdjustment) {
        this.frontAdjustment = frontAdjustment;
        return this;
    }

    @Info("The block to use as the key eg minecraft:iron_block or #minecraft:logs for block tags, this is not required")
    public KeyItemBuilder keyBlock(String keyBlock) {
        if (keyBlock.isEmpty()) {
            this.keyBlock = null;
        } else {
            this.keyBlock = keyBlock;
        }
        return this;
    }

    @Info("Is the key consumed when used, default true")
    public KeyItemBuilder consumeKey(boolean consumeKey) {
        this.consumeKey = consumeKey;
        return this;
    }

    @Info("If using the key removes a 2 high door, default true")
    public KeyItemBuilder removeDoor(boolean removeDoor) {
        this.removeDoor = removeDoor;
        return this;
    }

    @Info("If the template can only be placed on the side of a block, default true")
    public KeyItemBuilder sideOnlyPlacement(boolean sideOnlyPlacement) {
        this.sideOnlyPlacement = sideOnlyPlacement;
        return this;
    }

    @Info("If the template requires blocks to be placed, default false")
    public KeyItemBuilder blocksRequired(boolean blocksRequired) {
        this.blocksRequired = blocksRequired;
        return this;
    }

    @Info("If false normal checks for placement are done if true the template will be placed no matter the blocks in the world, default false")
    public KeyItemBuilder overrideExistingBlocks(boolean overrideExistingBlocks) {
        this.overrideExistingBlocks = overrideExistingBlocks;
        return this;
    }

    @Info("How many block to the left of the key block an opening should be created, default 0")
    public KeyItemBuilder doorLeft(int doorLeft) {
        this.doorLeft = doorLeft;
        return this;
    }

    @Info("How many block to the right of the key block an opening should be created, default 0")
    public KeyItemBuilder doorRight(int doorRight) {
        this.doorRight = doorRight;
        return this;
    }

    @Info("How many block above the key block an opening should be created, default 0")
    public KeyItemBuilder doorUp(int doorUp) {
        this.doorUp = doorUp;
        return this;
    }

    @Info("How many block below the key block an opening should be created, default 0")
    public KeyItemBuilder doorDown(int doorDown) {
        this.doorDown = doorDown;
        return this;
    }

    @Override
    public Item createObject() {
        return new KeyItem(createItemProperties(), templateId, heightAdjustment, frontAdjustment, keyBlock,
                consumeKey, removeDoor, sideOnlyPlacement, blocksRequired, overrideExistingBlocks,
                doorLeft, doorRight, doorUp, doorDown);
    }
}
