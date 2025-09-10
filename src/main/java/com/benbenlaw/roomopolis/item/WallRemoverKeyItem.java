package com.benbenlaw.roomopolis.item;

import com.benbenlaw.roomopolis.block.custom.RoomBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WallRemoverKeyItem extends Item {

    public WallRemoverKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction direction = context.getClickedFace();

        if (!level.isClientSide()) {
            if (state.getBlock() instanceof RoomBlock) {
                //get block behind the wall
                BlockPos behindPos = pos.relative(direction.getOpposite());

                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);

                }
            }


        return super.useOn(context);
    }



    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.key.wall_remover").withStyle(ChatFormatting.GOLD));
    }
}
