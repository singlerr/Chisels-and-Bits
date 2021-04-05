package mod.chiselsandbits.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleBlockBlockReader implements IBlockReader
{

    protected final BlockState state;
    protected final Block      blk;
    protected final BlockPos pos;
    protected final IBlockReader source;

    public SingleBlockBlockReader(final BlockState state, final Block blk)
    {
        this.state = state;
        this.blk = blk;
        this.pos = BlockPos.ZERO;
        this.source = null;
    }

    public SingleBlockBlockReader(final BlockState state)
    {
        this.state = state;
        this.blk = state.getBlock();
        this.pos = BlockPos.ZERO;
        this.source = null;
    }

    public SingleBlockBlockReader(final BlockState state, final Block blk, final BlockPos pos)
    {
        this.state = state;
        this.blk = blk;
        this.pos = pos;
        this.source = null;
    }

    public SingleBlockBlockReader(final BlockState state, final BlockPos pos)
    {
        this.state = state;
        this.blk = state.getBlock();
        this.pos = pos;
        this.source = null;
    }

    public SingleBlockBlockReader(final BlockState state, final Block blk, final IBlockReader source)
    {
        this.state = state;
        this.blk = blk;
        this.source = source;
        this.pos = BlockPos.ZERO;
    }

    public SingleBlockBlockReader(final BlockState state, final IBlockReader source)
    {
        this.state = state;
        this.blk = state.getBlock();
        this.source = source;
        this.pos = BlockPos.ZERO;
    }

    public SingleBlockBlockReader(final BlockState state, final Block blk, final BlockPos pos, final IBlockReader source)
    {
        this.state = state;
        this.blk = blk;
        this.pos = pos;
        this.source = source;
    }

    public SingleBlockBlockReader(final BlockState state, final BlockPos pos, final IBlockReader source)
    {
        this.state = state;
        this.blk = state.getBlock();
        this.pos = pos;
        this.source = source;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        if (pos == this.pos && blk.hasTileEntity(state))
        {
            return blk.createTileEntity(state, this);
        }

        return source == null ? null : source.getTileEntity(pos);
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        if (pos == this.pos)
        {
            return state;
        }

        return source == null ? Blocks.AIR.getDefaultState() : source.getBlockState(pos);
    }

    @NotNull
    @Override
    public FluidState getFluidState(@NotNull final BlockPos pos)
    {
        return getBlockState(pos).getFluidState();
    }
}
