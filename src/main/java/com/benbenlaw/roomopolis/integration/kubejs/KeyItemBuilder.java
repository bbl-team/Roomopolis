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

    @Override
    public Item createObject() {
        return new KeyItem(createItemProperties(), templateId, heightAdjustment, frontAdjustment, keyBlock, consumeKey, removeDoor);
    }
}
