package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.caveopolis.screen.CaveopolisMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class KeyCrafterMenu extends AbstractContainerMenu {

    protected Level level;
    protected Player player;

    public final Container container = new SimpleContainer(16) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    public KeyCrafterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, ContainerLevelAccess.NULL);
    }

    public KeyCrafterMenu(int containerID, Inventory inventory, final ContainerLevelAccess data) {
        super(RoomopolisMenuTypes.KEY_CRAFTER_MENU.get(), containerID);
        this.player = inventory.player;
        this.level = inventory.player.level();

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        int startX = 8;
        int startY = 17;
        int slotSize = 18;
        int slotIndex = 0;


        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new Slot(container, slotIndex++, startX + col * slotSize, startY + row * slotSize));
            }
        }

        this.addSlot(new Slot(container, 15, 139, 35) {

            @Override
            public boolean mayPlace(ItemStack p_40231_) {
                return false;
            }

            @Override
            public void onTake(Player p_150645_, ItemStack p_150646_) {

            //    removeIngredients();
            }
        });


    }









    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 16;  // must be the number of slots you have!



    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

}
