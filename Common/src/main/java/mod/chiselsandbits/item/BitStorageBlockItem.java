package mod.chiselsandbits.item;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.util.HelpTextUtils;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BitStorageBlockItem extends BlockItem
{

    public BitStorageBlockItem(
      final Block block , Properties builder)
    {
        super( block, builder );
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText( stack, worldIn, tooltip, flagIn );

        if (stack.getOrCreateTag().contains(NbtConstants.BLOCK_INFORMATION)) {
            final BlockInformation containedState = new BlockInformation(stack.getOrCreateTag().getCompound(NbtConstants.BLOCK_INFORMATION));
            final int count = stack.getOrCreateTag().getInt(NbtConstants.COUNT);

            HelpTextUtils.build(LocalStrings.HelpBitStorageFilled, tooltip, containedState.getBlockState().getBlock().getName(), count);
        }
        else
        {
            HelpTextUtils.build(LocalStrings.HelpBitStorageEmpty, tooltip);
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
      final @NotNull BlockPos pos, final @NotNull Level worldIn, @Nullable final Player player, final @NotNull ItemStack stack, final @NotNull BlockState state)
    {
        super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
        if (worldIn.isClientSide)
            return false;

        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (!(blockEntity instanceof final BitStorageBlockEntity bitStorage))
            return false;

        if (stack.getOrCreateTag().contains(NbtConstants.CONTENTS)) {
            final BlockState blockState = NbtUtils.readBlockState(stack.getOrCreateTag().getCompound(NbtConstants.CONTENTS));
            final int count = stack.getOrCreateTag().getInt(NbtConstants.COUNT);

            bitStorage.setContents(
              new BlockInformation(blockState),
              count
            );

            return true;
        }

        if (stack.getOrCreateTag().contains(NbtConstants.BLOCK_INFORMATION)) {
            final BlockInformation blockInformation = new BlockInformation(stack.getOrCreateTag().getCompound(NbtConstants.BLOCK_INFORMATION));
            final int count = stack.getOrCreateTag().getInt(NbtConstants.COUNT);

            bitStorage.setContents(
              blockInformation,
              count
            );

            return true;
        }
        return false;
    }
}