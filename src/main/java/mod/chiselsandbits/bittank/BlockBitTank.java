package mod.chiselsandbits.bittank;

import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class BlockBitTank extends Block implements ITileEntityProvider
{

	private static final Property<Direction> FACING = HorizontalBlock.HORIZONTAL_FACING;

	public BlockBitTank(AbstractBlock.Properties properties)
	{
		super( properties );
	}

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final IBlockReader worldIn)
    {
        return new TileEntityBitTank();
    }

	public TileEntityBitTank getTileEntity(
			final TileEntity te ) throws ExceptionNoTileEntity
	{
		if ( te instanceof TileEntityBitTank )
		{
			return (TileEntityBitTank) te;
		}
		throw new ExceptionNoTileEntity();
	}

	public TileEntityBitTank getTileEntity(
			final IBlockReader world,
			final BlockPos pos ) throws ExceptionNoTileEntity
	{
		return getTileEntity( world.getTileEntity( pos ) );
	}

    @Override
    public ActionResultType onBlockActivated(
      final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit)
    {
        try
        {
            final TileEntityBitTank tank = getTileEntity( worldIn, pos );
            final ItemStack current = ModUtil.nonNull( player.inventory.getCurrentItem() );

            if ( !ModUtil.isEmpty( current ) )
            {
                final IFluidHandler wrappedTank = tank;
                if ( FluidUtil.interactWithFluidHandler( player, handIn, wrappedTank ) )
                {
                    return ActionResultType.SUCCESS;
                }

                if ( tank.addHeldBits( current, player ) )
                {
                    return ActionResultType.SUCCESS;
                }
            }
            else
            {
                if ( tank.addAllPossibleBits( player ) )
                {
                    return ActionResultType.SUCCESS;
                }
            }

            if ( tank.extractBits( player, hit.getHitVec().x, hit.getHitVec().y, hit.getHitVec().z, pos ) )
            {
                return ActionResultType.SUCCESS;
            }
        }
        catch ( final ExceptionNoTileEntity e )
        {
            Log.noTileError( e );
        }

        return ActionResultType.FAIL;
    }
}
