package com.benbenlaw.roomopolis.block.custom;

import com.benbenlaw.roomopolis.screen.KeyCrafterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public class KeyCrafterBlock extends Block {
    public KeyCrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!level.isClientSide()) {
            // Open the key crafter GUI
            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new KeyCrafterMenu(windowId, playerInventory, ContainerLevelAccess.create(level, player.blockPosition())),
                    Component.translatable("block.roomopolis.key_crafter")), (buf -> buf.writeBlockPos(player.blockPosition())));
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }

}
