package mod.chiselsandbits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.Nullable;

public class SingleBlockBlockReader implements IBlockReader
{

    private final BlockState state;
    private final Block      blk;

    public SingleBlockBlockReader(final BlockState state, final Block blk)
    {
        this.state = state;
        this.blk = blk;
    }

    public SingleBlockBlockReader(final BlockState state)
    {
        this.state = state;
        this.blk = state.getBlock();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        if (pos == BlockPos.ZERO && blk.hasTileEntity(state))
        {
            return blk.createTileEntity(state, this);
        }

        return null;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        if (pos == BlockPos.ZERO)
        {
            return state;
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return Fluids.EMPTY.getDefaultState();
    }
}
