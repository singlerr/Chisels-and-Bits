
package mod.chiselsandbits.chiseledblock;

import java.lang.reflect.Field;
import java.util.List;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.chiseledblock.data.UnlistedBlockState;
import mod.chiselsandbits.chiseledblock.data.UnlistedLightOpacity;
import mod.chiselsandbits.chiseledblock.data.UnlistedVoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;


public class BlockChiseled extends Block implements ITileEntityProvider
{

	public static final IUnlistedProperty<VoxelBlobState> v_prop = new UnlistedVoxelBlob();
	public static final IUnlistedProperty<Integer> block_prop = new UnlistedBlockState();
	public static final IUnlistedProperty<Float> light_prop = new UnlistedLightOpacity();

	public final String name;

	public BlockChiseled(
			final Material mat,
			final String BlockName )
	{
		super( new SubMaterial( mat ) );

		// slippery ice...
		if ( mat == Material.ice || mat == Material.packedIce )
			slipperiness = 0.98F;

		setLightOpacity( 0 );
		setHardness( 1 );
		setHarvestLevel( "pickaxe", 0 );
		name = BlockName;
	}

	public TileEntityBlockChiseled getTileEntity(
			final TileEntity te ) throws ExceptionNoTileEntity
	{
		if ( te instanceof TileEntityBlockChiseled )
			return ( TileEntityBlockChiseled ) te;
		throw new ExceptionNoTileEntity();
	}

	public TileEntityBlockChiseled getTileEntity(
			final IBlockAccess world,
			final BlockPos pos ) throws ExceptionNoTileEntity
	{
		return getTileEntity( world.getTileEntity( pos ) );
	}

	@Override
	public float getAmbientOcclusionLightValue()
	{
		return 1.0f;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public IBlockState getExtendedState(
			final IBlockState state,
			final IBlockAccess world,
			final BlockPos pos )
	{
		try
		{
			return getTileEntity( world, pos ).getState();
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return state;
		}
	}

	@Override
	public void dropBlockAsItemWithChance(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final float chance,
			final int fortune )
	{
		try
		{
			spawnAsEntity( worldIn, pos, getTileEntity( worldIn, pos ).getItemStack( this, null ) );
		}
		catch ( final ExceptionNoTileEntity exp )
		{
			// not much we can do here..
		}
	}

	@Override
	public void harvestBlock(
			final World worldIn,
			final EntityPlayer player,
			final BlockPos pos,
			final IBlockState state,
			final TileEntity te )
	{
		try
		{
			spawnAsEntity( worldIn, pos, getTileEntity( te ).getItemStack( this, player ) );

		}
		catch ( final ExceptionNoTileEntity exp )
		{
			super.harvestBlock( worldIn, player, pos, state, ( TileEntity ) null );
		}
	}

	@Override
	public void onBlockPlacedBy(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final EntityLivingBase placer,
			final ItemStack stack )
	{
		try
		{
			if ( stack == null || placer == null )
				return;

			final TileEntityBlockChiseled bc = getTileEntity( worldIn, pos );
			int rotations = ModUtil.getRotations( placer, stack.getTagCompound().getByte( "side" ) );

			VoxelBlob blob = bc.getBlob();
			while ( rotations-- > 0 )
				blob = blob.spin( Axis.Y );
			bc.setBlob( blob );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			// :(
		}
	}

	@Override
	public ItemStack getPickBlock(
			final MovingObjectPosition target,
			final World world,
			final BlockPos pos )
	{
		try
		{
			final EntityPlayer player = ClientSide.instance.getPlayer();
			if ( player != null && ModUtil.isHoldingChiselTool( player ) != null )
			{
				final VoxelBlob vb = getTileEntity( world, pos ).getBlob();

				final int x = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * ( target.hitVec.xCoord - pos.getX() ) - target.sideHit.getFrontOffsetX() * 0.5 ) ) );
				final int y = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * ( target.hitVec.yCoord - pos.getY() ) - target.sideHit.getFrontOffsetY() * 0.5 ) ) );
				final int z = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * ( target.hitVec.zCoord - pos.getZ() ) - target.sideHit.getFrontOffsetZ() * 0.5 ) ) );

				final int itemBlock = vb.get( x, y, z );
				if ( itemBlock == 0 )
					return null;

				return ItemChiseledBit.createStack( itemBlock, 1 );
			}

			return getTileEntity( world, pos ).getItemStack( this, player );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return null;
		}
	}

	@Override
	protected BlockState createBlockState()
	{
		return new ExtendedBlockState( this, new IProperty[0], new IUnlistedProperty[] { v_prop, block_prop, light_prop } );
	}

	@Override
	public TileEntity createNewTileEntity(
			final World worldIn,
			final int meta )
	{
		return new TileEntityBlockChiseled();
	}

	@Override
	public boolean addDestroyEffects(
			final World world,
			final BlockPos pos,
			final EffectRenderer effectRenderer )
	{
		try
		{
			final IBlockState state = getTileEntity( world, pos ).getParticleBlockState( this );
			effectRenderer.func_180533_a( pos, state );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			// well not much we can do, so just don't render anything...
		}

		return true;
	}

	@Override
	public boolean addHitEffects(
			final World world,
			final MovingObjectPosition target,
			final EffectRenderer effectRenderer )
	{
		try
		{
			final BlockPos pos = target.getBlockPos();
			final IBlockState state = getTileEntity( world, pos ).getParticleBlockState( this );
			return ClientSide.instance.addHitEffects( world, target, state, effectRenderer );
		}
		catch ( final ExceptionNoTileEntity exp )
		{
			// well not much we can do, so just don't render anything...
			return true;
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public void addCollisionBoxesToList(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final AxisAlignedBB mask,
			final List list,
			final Entity collidingEntity )
	{
		setBounds( worldIn, pos, mask, list );
		setBlockBounds( 0, 0, 0, 1, 1, 1 );
	}

	/**
	 * this method dosn't use AxisAlignedBB internally to prevent GC thrashing.
	 *
	 * @param worldIn
	 * @param pos
	 *
	 * mask and list should be null if not looking for collisions
	 *
	 * @return if the method results in a non-full cube box.
	 */
	private boolean setBounds(
			final World worldIn,
			final BlockPos pos,
			final AxisAlignedBB mask,
			final List<AxisAlignedBB> list )
	{
		boolean started = false;

		float minX = 0.0f;
		float minY = 0.0f;
		float minZ = 0.0f;

		float maxX = 1.0f;
		float maxY = 1.0f;
		float maxZ = 1.0f;

		try
		{
			final TileEntityBlockChiseled tec = getTileEntity( worldIn, pos );
			final VoxelBlob vb = tec.getBlob();

			final float One16thf = 1.0f / vb.detail;

			for ( int y = 0; y < vb.detail; y++ )
			{
				for ( int z = 0; z < vb.detail; z++ )
					for ( int x = 0; x < vb.detail; x++ )
						if ( vb.get( x, y, z ) != 0 )
							if ( started )
							{
								minX = Math.min( minX, One16thf * x );
								minY = Math.min( minY, One16thf * y );
								minZ = Math.min( minZ, One16thf * z );
								maxX = Math.max( maxX, One16thf * ( x + 1.0f ) );
								maxY = Math.max( maxY, One16thf * ( y + 1.0f ) );
								maxZ = Math.max( maxZ, One16thf * ( z + 1.0f ) );
							}
							else
							{
								started = true;
								minX = One16thf * x;
								minY = One16thf * y;
								minZ = One16thf * z;
								maxX = One16thf * ( x + 1.0f );
								maxY = One16thf * ( y + 1.0f );
								maxZ = One16thf * ( z + 1.0f );
							}

				// VERY hackey collision extraction to do 2 bounding boxes, one for top and one for the bottom.
				if ( list != null && started && ( y == 8 || y == VoxelBlob.dim_minus_one ) )
				{
					final AxisAlignedBB bb = AxisAlignedBB.fromBounds( minX + pos.getX(), minY + pos.getY(), minZ + pos.getZ(), maxX + pos.getX(), maxY + pos.getY(), maxZ + pos.getZ() );
					setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if ( mask.intersectsWith( bb ) )
						list.add( bb );

					started = false;
					minX = 0.0f;
					minY = 0.0f;
					minZ = 0.0f;
					maxX = 1.0f;
					maxY = 1.0f;
					maxZ = 1.0f;
				}
			}
		}
		catch ( final ExceptionNoTileEntity e )
		{}

		setBlockBounds( minX, minY, minZ, maxX, maxY, maxZ );
		return started;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public AxisAlignedBB getSelectedBoundingBox(
			final World worldIn,
			final BlockPos pos )
	{
		if ( worldIn.isRemote )
		{
			final EntityPlayer playerIn = ClientSide.instance.getPlayer();
			final ItemStack equiped = playerIn.getCurrentEquippedItem();

			final ChiselMode chMode = ModUtil.isHoldingChiselTool( playerIn );
			if ( equiped == null || null == chMode )
			{
				setBounds( worldIn, pos, null, null );
				final AxisAlignedBB r = super.getSelectedBoundingBox( worldIn, pos );
				setBlockBounds( 0, 0, 0, 1, 1, 1 );

				return r;
			}

			try
			{
				final TileEntityBlockChiseled tec = getTileEntity( worldIn, pos );
				final VoxelBlob vb = tec.getBlob();

				return getSelectedBoundingBox( playerIn, pos, vb, chMode );
			}
			catch ( final ExceptionNoTileEntity e )
			{}
		}

		setBounds( worldIn, pos, null, null );
		final AxisAlignedBB r = super.getSelectedBoundingBox( worldIn, pos );
		setBlockBounds( 0, 0, 0, 1, 1, 1 );

		return r;
	}

	@SideOnly( Side.CLIENT )
	public AxisAlignedBB getSelectedBoundingBox(
			final EntityPlayer playerIn,
			final BlockPos pos,
			final VoxelBlob vb,
			final ChiselMode chMode )
	{
		final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay( playerIn );
		final Vec3 a = PlayerRay.getLeft();
		final Vec3 b = PlayerRay.getRight();

		MovingObjectPosition selectedR = null;
		AxisAlignedBB br = null;
		double lastDist = 0;

		final float One16thf = 1.0f / vb.detail;

		for ( int z = 0; z < vb.detail; z++ )
			for ( int y = 0; y < vb.detail; y++ )
				for ( int x = 0; x < vb.detail; x++ )
					if ( vb.get( x, y, z ) != 0 )
					{
						setBlockBounds( One16thf * x, One16thf * y, One16thf * z, One16thf * ( x + 1.0f ), One16thf * ( y + 1.0f ), One16thf * ( z + 1.0f ) );
						final MovingObjectPosition r = super.collisionRayTrace( playerIn.worldObj, pos, a, b );

						if ( r != null )
						{
							final double xLen = a.xCoord - r.hitVec.xCoord;
							final double yLen = a.yCoord - r.hitVec.yCoord;
							final double zLen = a.zCoord - r.hitVec.zCoord;
							final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;

							if ( selectedR == null || lastDist > thisDist )
							{
								lastDist = thisDist;
								selectedR = r;
							}

						}
					}

		setBlockBounds( 0, 0, 0, 1, 1, 1 );

		if ( selectedR != null )
		{
			final float One32ndf = 0.5f / VoxelBlob.dim;

			final int x = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, ( int ) ( VoxelBlob.dim * ( selectedR.hitVec.xCoord - pos.getX() - One32ndf * selectedR.sideHit.getFrontOffsetX() ) ) ) );
			final int y = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, ( int ) ( VoxelBlob.dim * ( selectedR.hitVec.yCoord - pos.getY() - One32ndf * selectedR.sideHit.getFrontOffsetY() ) ) ) );
			final int z = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, ( int ) ( VoxelBlob.dim * ( selectedR.hitVec.zCoord - pos.getZ() - One32ndf * selectedR.sideHit.getFrontOffsetZ() ) ) ) );

			final ChiselTypeIterator ci = new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vb, chMode, selectedR.sideHit );

			boolean started = false;
			while ( ci.hasNext() )
				if ( vb.get( ci.x(), ci.y(), ci.z() ) != 0 )
					if ( started )
					{
						minX = Math.min( minX, One16thf * ci.x() );
						minY = Math.min( minY, One16thf * ci.y() );
						minZ = Math.min( minZ, One16thf * ci.z() );
						maxX = Math.max( maxX, One16thf * ( ci.x() + 1.0f ) );
						maxY = Math.max( maxY, One16thf * ( ci.y() + 1.0f ) );
						maxZ = Math.max( maxZ, One16thf * ( ci.z() + 1.0f ) );
					}
					else
					{
						started = true;
						minX = One16thf * ci.x();
						minY = One16thf * ci.y();
						minZ = One16thf * ci.z();
						maxX = One16thf * ( ci.x() + 1.0f );
						maxY = One16thf * ( ci.y() + 1.0f );
						maxZ = One16thf * ( ci.z() + 1.0f );
					}

			br = AxisAlignedBB.fromBounds( minX, minY, minZ, maxX, maxY, maxZ );
			br = br.offset( pos.getX(), pos.getY(), pos.getZ() );

			setBlockBounds( 0, 0, 0, 1, 1, 1 );

			return br;
		}

		return AxisAlignedBB.fromBounds( 0, 0, 0, 1, 1, 1 ).offset( pos.getX(), pos.getY(), pos.getZ() );
	}

	@Override
	public MovingObjectPosition collisionRayTrace(
			final World worldIn,
			final BlockPos pos,
			final Vec3 a,
			final Vec3 b )
	{
		if ( worldIn.isRemote )
		{
			MovingObjectPosition br = null;
			double lastDist = 0;

			try
			{
				final TileEntityBlockChiseled tec = getTileEntity( worldIn, pos );
				final VoxelBlob vb = tec.getBlob();

				final float One16thf = 1.0f / vb.detail;

				for ( int z = 0; z < vb.detail; z++ )
					for ( int y = 0; y < vb.detail; y++ )
						for ( int x = 0; x < vb.detail; x++ )
							if ( vb.get( x, y, z ) != 0 )
							{
								setBlockBounds( One16thf * x, One16thf * y, One16thf * z, One16thf * ( x + 1.0f ), One16thf * ( y + 1.0f ), One16thf * ( z + 1.0f ) );

								final MovingObjectPosition r = super.collisionRayTrace( worldIn, pos, a, b );

								if ( r != null )
								{
									final double xLen = a.xCoord - r.hitVec.xCoord;
									final double yLen = a.yCoord - r.hitVec.yCoord;
									final double zLen = a.zCoord - r.hitVec.zCoord;

									final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
									if ( br == null || lastDist > thisDist )
									{
										lastDist = thisDist;
										br = r;
									}

								}
							}
			}
			catch ( final ExceptionNoTileEntity e )
			{}

			setBlockBounds( 0, 0, 0, 1, 1, 1 );

			return br;
		}

		setBounds( worldIn, pos, null, null );
		final MovingObjectPosition r = super.collisionRayTrace( worldIn, pos, a, b );
		setBlockBounds( 0, 0, 0, 1, 1, 1 );

		return r;
	}

	@Override
	public float getBlockHardness(
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getTileEntity( worldIn, pos ).getBlock( this ).getBlockHardness( worldIn, pos );
		}
		catch ( final Throwable err )
		{
			// if for some reason the block has an override that causes issues...
			return super.getBlockHardness( worldIn, pos );
		}
	}

	@Override
	public float getPlayerRelativeBlockHardness(
			final EntityPlayer playerIn,
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getTileEntity( worldIn, pos ).getBlock( this ).getPlayerRelativeBlockHardness( playerIn, worldIn, pos );
		}
		catch ( final Throwable err )
		{
			// if for some reason the block has an override that causes issues...
			return super.getPlayerRelativeBlockHardness( playerIn, worldIn, pos );
		}
	}

	@Override
	public float getExplosionResistance(
			final World world,
			final BlockPos pos,
			final Entity exploder,
			final Explosion explosion )
	{
		try
		{
			return getTileEntity( world, pos ).getBlock( this ).getExplosionResistance( world, pos, exploder, explosion );
		}
		catch ( final Throwable err )
		{
			// if for some reason the block has an override that causes issues...
			return super.getExplosionResistance( world, pos, exploder, explosion );
		}
	}

	public static boolean replaceWithChisled(
			final World world,
			final BlockPos pos,
			final IBlockState originalState )
	{
		return replaceWithChisled( world, pos, originalState, 0 );
	}

	public static boolean supportsBlock(
			final IBlockState state )
	{
		final Block blk = state.getBlock();

		try
		{
			final Class<? extends Block> blkClass = blk.getClass();

			final boolean test_a = blkClass.getMethod( "getBlockHardness", World.class, BlockPos.class ).getDeclaringClass() == Block.class;
			final boolean test_b = blkClass.getMethod( "getPlayerRelativeBlockHardness", EntityPlayer.class, World.class, BlockPos.class ).getDeclaringClass() == Block.class;
			final boolean test_c = blkClass.getMethod( "getExplosionResistance", World.class, BlockPos.class, Entity.class, Explosion.class ).getDeclaringClass() == Block.class;
			final Field f = Block.class.getDeclaredField( "blockHardness" );

			final boolean wasAccessible = f.isAccessible();
			f.setAccessible( true );
			final float blockHardness = f.getFloat( blk );
			f.setAccessible( wasAccessible );

			return test_a && test_b && test_c && blockHardness >= -0.5f && blk.hasTileEntity( state ) == false && blk.isFullCube() && ChiselsAndBits.instance.getConversion( blk.getMaterial() ) != null;
		}
		catch ( final Throwable t )
		{
			// if the above test fails for any reason, then the block cannot be supported.
			return false;
		}
	}

	@Override
	public boolean rotateBlock(
			final World world,
			final BlockPos pos,
			final EnumFacing axis )
	{
		try
		{
			final TileEntityBlockChiseled cte = getTileEntity( world, pos );
			cte.setBlob( cte.getBlob().spin( axis.getAxis() ) );
			return true;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return false;
		}
	}

	public static boolean replaceWithChisled(
			final World world,
			final BlockPos pos,
			IBlockState originalState,
			final int fragmentBlockStateID )
	{
		Block target = originalState.getBlock();
		final boolean isAir = world.isAirBlock( pos );

		if ( supportsBlock( originalState ) || isAir )
		{
			BlockChiseled blk = ChiselsAndBits.instance.getConversion( target.getMaterial() );

			int BlockID = Block.getStateId( originalState );

			if ( isAir )
			{
				originalState = Block.getStateById( fragmentBlockStateID );
				target = originalState.getBlock();
				BlockID = Block.getStateId( originalState );
				blk = ChiselsAndBits.instance.getConversion( target.getMaterial() );
				// its still air tho..
				originalState = Blocks.air.getDefaultState();
			}

			if ( BlockID == 0 )
				throw new NullPointerException();

			if ( blk != null && blk != target )
			{
				world.setBlockState( pos, blk.getDefaultState() );
				final TileEntity te = world.getTileEntity( pos );

				TileEntityBlockChiseled tec = null;
				if ( !( te instanceof TileEntityBlockChiseled ) )
					world.setTileEntity( pos, tec = ( TileEntityBlockChiseled ) blk.createTileEntity( world, blk.getDefaultState() ) );
				else
					tec = ( TileEntityBlockChiseled ) te;

				tec.fillWith( originalState );
				tec.setState( tec.getState().withProperty( BlockChiseled.block_prop, BlockID ) );

				return true;
			}
		}

		return false;
	}

}
