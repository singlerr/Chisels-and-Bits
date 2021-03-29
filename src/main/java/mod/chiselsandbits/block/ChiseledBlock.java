package mod.chiselsandbits.block;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.CreativeClipboardTab;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.registry.ModTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChiseledBlock extends Block implements IMultiStateBlock
{
    public ChiseledBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ChiseledBlockEntity(
          ModTileEntityTypes.CHISELED.get()
        );
    }

    @NotNull
    @Override
    public BlockState getPrimaryState(@NotNull final IBlockReader world, @NotNull final BlockPos pos)
    {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalArgumentException(String.format("The given position: %s does not seem to point to a multi state block!", pos));

        final IMultiStateBlockEntity blockEntityWithMultipleStates = (IMultiStateBlockEntity) tileEntity;
        return blockEntityWithMultipleStates.getStatistics().getPrimaryState();
    }

    @Override
    public float getSlipperiness(final BlockState state, final IWorldReader world, final BlockPos pos, @Nullable final Entity entity)
    {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalArgumentException(String.format("The given position: %s does not seem to point to a multi state block!", pos));

        final IMultiStateBlockEntity blockEntityWithMultipleStates = (IMultiStateBlockEntity) tileEntity;
        return blockEntityWithMultipleStates.getStatistics().getSlipperiness();
    }

    @Override
    public boolean shouldCheckWeakPower(final BlockState state, final IWorldReader world, final BlockPos pos, final Direction side)
    {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalArgumentException("The given combination of position and world do not point to a multi state block.");

        return ((IMultiStateBlockEntity) tileEntity).getStatistics().shouldCheckWeakPower();
    }

    @Override
    public float getAmbientOcclusionLightValue(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalArgumentException("The given combination of position and world do not point to a multi state block.");

        return ((IMultiStateBlockEntity) tileEntity).getStatistics().getFullnessFactor();
    }

    @Override
    public boolean removedByPlayer(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final boolean willHarvest, final FluidState fluid)
    {
        if ( !willHarvest && ChiselsAndBits.getConfig().getClient().addBrokenBlocksToCreativeClipboard.get() )
        {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalArgumentException("The given combination of position and world do not point to a multi state block.");

            final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
            final IMultiStateSnapshot multiStateSnapshot = multiStateBlockEntity.createSnapshot();

            IChangeTracker.getInstance().onBlockBroken(
              world,
              pos,
              multiStateSnapshot
            );
        }

        return super.removedByPlayer( state, world, pos, player, willHarvest, fluid );
    }
}
