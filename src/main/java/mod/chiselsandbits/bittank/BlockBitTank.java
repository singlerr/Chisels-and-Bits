package mod.chiselsandbits.bittank;

import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Predicate;

public class BlockBitTank extends Block implements ITileEntityProvider
{

	public static final PropertyDirection FACING = PropertyDirection.create( "facing", new Predicate<EnumFacing>() {
		@Override
		public boolean apply(
				final EnumFacing face )
		{
			return face != EnumFacing.DOWN && face != EnumFacing.UP;
		}
	} );

	public BlockBitTank()
	{
		super( Material.iron );
		setStepSound( soundTypeGlass );
		translucent = true;
		setLightOpacity( 0 );
		setHardness( 1 );
		setHarvestLevel( "pickaxe", 0 );
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public boolean isFullBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public float getAmbientOcclusionLightValue()
	{
		return 1.0f;
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState( this, FACING );
	}

	@Override
	public int getMetaFromState(
			final IBlockState state )
	{
		switch ( state.getValue( FACING ) )
		{
			case NORTH:
				return 0;
			case SOUTH:
				return 1;
			case EAST:
				return 2;
			case WEST:
				return 3;
			default:
				throw new RuntimeException( "Invalid State." );
		}
	}

	@Override
	public IBlockState getStateFromMeta(
			final int meta )
	{
		switch ( meta )
		{
			case 0:
				return getDefaultState().withProperty( FACING, EnumFacing.NORTH );
			case 1:
				return getDefaultState().withProperty( FACING, EnumFacing.SOUTH );
			case 2:
				return getDefaultState().withProperty( FACING, EnumFacing.EAST );
			case 3:
				return getDefaultState().withProperty( FACING, EnumFacing.WEST );
			default:
				throw new RuntimeException( "Invalid State." );
		}
	}

	@Override
	public TileEntity createNewTileEntity(
			final World worldIn,
			final int meta )
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
			final IBlockAccess world,
			final BlockPos pos ) throws ExceptionNoTileEntity
	{
		return getTileEntity( world.getTileEntity( pos ) );
	}

	@Override
	public boolean onBlockActivated(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final EntityPlayer playerIn,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		try
		{
			final TileEntityBitTank tank = getTileEntity( worldIn, pos );
			final ItemStack current = playerIn.inventory.getCurrentItem();

			if ( current != null )
			{
				final FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem( current );
				if ( liquid != null && liquid.amount % TileEntityBitTank.MB_PER_BIT_CONVERSION == 0 && liquid.amount > 0 )
				{
					final int bitCount = liquid.amount * TileEntityBitTank.BITS_PER_MB_CONVERSION / TileEntityBitTank.MB_PER_BIT_CONVERSION;
					final ItemStack bitItems = tank.getFluidBitStack( liquid.getFluid(), bitCount );

					if ( tank.insertItem( 0, bitItems, true ) == null )
					{
						final ItemStack empty = FluidContainerRegistry.drainFluidContainer( current );

						if ( empty != null )
						{
							// insert items.. and drain the container.
							tank.insertItem( 0, bitItems, false );
							playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, empty );
							playerIn.inventory.markDirty();
						}
					}
					return true;
				}

				if ( FluidContainerRegistry.isEmptyContainer( current ) )
				{
					final FluidStack outFluid = tank.getAccessableFluid();
					final int capacity = FluidContainerRegistry.getContainerCapacity( outFluid, current );

					if ( capacity % TileEntityBitTank.MB_PER_BIT_CONVERSION == 0 )
					{
						int requiredBits = capacity / TileEntityBitTank.MB_PER_BIT_CONVERSION;
						requiredBits *= TileEntityBitTank.BITS_PER_MB_CONVERSION;

						final ItemStack output = tank.extractBits( 0, requiredBits, true );
						if ( output != null && output.stackSize == requiredBits )
						{
							outFluid.amount = capacity;
							final ItemStack filled = FluidContainerRegistry.fillFluidContainer( outFluid, current );

							if ( filled != null )
							{
								tank.extractBits( 0, requiredBits, false );
								playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, filled );
								playerIn.inventory.markDirty();
							}
						}
					}
					return true;
				}

				if ( playerIn.isSneaking() )
				{
					if ( ChiselsAndBits.getApi().getItemType( current ) == ItemType.CHISLED_BIT )
					{
						playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, tank.insertItem( 0, current, false ) );
						playerIn.inventory.markDirty();
						return true;
					}
				}
			}

			if ( !playerIn.isSneaking() )
			{
				final ItemStack is = tank.extractItem( 0, 64, false );
				if ( is != null )
				{
					ChiselsAndBits.getApi().giveBitToPlayer( playerIn, is, new Vec3( (double) hitX + pos.getX(), (double) hitY + pos.getY(), (double) hitZ + pos.getZ() ) );
				}
				return true;
			}
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
		}

		return false;
	}

}
