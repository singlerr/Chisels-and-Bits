package mod.chiselsandbits.bittank;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

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

}
