package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

public class ItemStackUtils
{

    private ItemStackUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ItemStackUtils. This is a utility class");
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState)
    {
        if (blockState.getBlock() instanceof IFluidBlock)
        {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);
        if (item != Items.AIR && item != null)
        {
            return new ItemStack(item, 1);
        }

        return new ItemStack(blockState.getBlock(), 1);
    }

    public static Item getItem(@NotNull final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block.equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (block instanceof CropsBlock)
        {
            final ItemStack stack = ((CropsBlock) block).getItem(new SingleBlockBlockReader(blockState), BlockPos.ZERO, blockState);
            if (!stack.isEmpty())
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        // oh no...
        else if (block instanceof FarmlandBlock || block instanceof GrassPathBlock)
        {
            return Blocks.DIRT.asItem();
        }
        else if (block instanceof FireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }
        else if (block instanceof FlowerPotBlock)
        {
            return Items.FLOWER_POT;
        }
        else if (block == Blocks.BAMBOO_SAPLING)
        {
            return Items.BAMBOO;
        }
        else
        {
            return block.asItem();
        }
    }


}
