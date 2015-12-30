package mod.chiselsandbits.chiseledblock;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.chiseledblock.data.BitCollisionIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.chiseledblock.properties.UnlistedBlockFlags;
import mod.chiselsandbits.chiseledblock.properties.UnlistedBlockStateID;
import mod.chiselsandbits.chiseledblock.properties.UnlistedLightOpacity;
import mod.chiselsandbits.chiseledblock.properties.UnlistedLightValue;
import mod.chiselsandbits.chiseledblock.properties.UnlistedVoxelBlob;
import mod.chiselsandbits.chiseledblock.properties.UnlistedVoxelNeighborState;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockChiseled extends Block implements ITileEntityProvider
{

	private static HashMap<IBlockState, BlockBitInfo> stateBitInfo = new HashMap<IBlockState, BlockBitInfo>();
	private static HashMap<Block, Boolean> supportedBlocks = new HashMap<Block, Boolean>();
	private static ThreadLocal<Integer> replacementLightValue = new ThreadLocal<Integer>();
	public static IBlockState actingAs = null;

	public static final IUnlistedProperty<VoxelNeighborRenderTracker> n_prop = new UnlistedVoxelNeighborState();
	public static final IUnlistedProperty<VoxelBlobStateReference> v_prop = new UnlistedVoxelBlob();
	public static final IUnlistedProperty<Integer> block_prop = new UnlistedBlockStateID();
	public static final IUnlistedProperty<Integer> side_prop = new UnlistedBlockFlags( "f" );
	public static final IUnlistedProperty<Float> opacity_prop = new UnlistedLightOpacity();
	public static final IUnlistedProperty<Integer> light_prop = new UnlistedLightValue();

	public final String name;

	@Override
	public boolean isReplaceable(
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getTileEntity( worldIn, pos ).getBlob().solid() == 0;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return super.isReplaceable( worldIn, pos );
		}
	}

	@Override
	public boolean doesSideBlockRendering(
			final IBlockAccess world,
			final BlockPos pos,
			final EnumFacing face )
	{
		try
		{
			return getTileEntity( world, pos ).isSideOpaque( face.getOpposite() );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return false;
		}
	}

	public BlockChiseled(
			final Material mat,
			final String BlockName )
	{
		super( new SubMaterial( mat ) );

		configureSound( mat );

		// slippery ice...
		if ( mat == Material.ice || mat == Material.packedIce )
		{
			slipperiness = 0.98F;
		}

		setLightOpacity( 0 );
		setHardness( 1 );
		setHarvestLevel( "pickaxe", 0 );
		name = BlockName;
	}

	private void configureSound(
			final Material mat )
	{
		if ( mat == Material.wood )
		{
			setStepSound( soundTypeWood );
		}
		else if ( mat == Material.rock )
		{
			setStepSound( soundTypeStone );
		}
		else if ( mat == Material.iron )
		{
			setStepSound( soundTypeMetal );
		}
		else if ( mat == Material.cloth )
		{
			setStepSound( soundTypeMetal );
		}
		else if ( mat == Material.ice )
		{
			setStepSound( soundTypeGlass );
		}
		else if ( mat == Material.packedIce )
		{
			setStepSound( soundTypeGlass );
		}
		else if ( mat == Material.clay )
		{
			setStepSound( soundTypeGravel );
		}
		else if ( mat == Material.glass )
		{
			setStepSound( soundTypeGlass );
		}
	}

	@Override
	public boolean canRenderInLayer(
			final EnumWorldBlockLayer layer )
	{
		return true;
	}

	public TileEntityBlockChiseled getTileEntity(
			final TileEntity te ) throws ExceptionNoTileEntity
	{
		if ( te instanceof TileEntityBlockChiseled )
		{
			return (TileEntityBlockChiseled) te;
		}
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
			return getTileEntity( world, pos ).getRenderState();
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
			super.harvestBlock( worldIn, player, pos, state, (TileEntity) null );
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
			if ( stack == null || placer == null || !stack.hasTagCompound() )
			{
				return;
			}

			final TileEntityBlockChiseled bc = getTileEntity( worldIn, pos );
			int rotations = ModUtil.getRotations( placer, stack.getTagCompound().getByte( "side" ) );

			VoxelBlob blob = bc.getBlob();
			while ( rotations-- > 0 )
			{
				blob = blob.spin( Axis.Y );
			}
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
			return getPickBlock( target, pos, getTileEntity( world, pos ) );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return null;
		}
	}

	public ItemStack getPickBlock(
			final MovingObjectPosition target,
			final BlockPos pos,
			final TileEntityBlockChiseled te )
	{
		final EntityPlayer player = ClientSide.instance.getPlayer();
		if ( player != null && ModUtil.isHoldingChiselTool( player ) != null )
		{
			final VoxelBlob vb = te.getBlob();

			final int x = Math.min( 15, Math.max( 0, (int) ( vb.detail * ( target.hitVec.xCoord - pos.getX() ) - target.sideHit.getFrontOffsetX() * 0.5 ) ) );
			final int y = Math.min( 15, Math.max( 0, (int) ( vb.detail * ( target.hitVec.yCoord - pos.getY() ) - target.sideHit.getFrontOffsetY() * 0.5 ) ) );
			final int z = Math.min( 15, Math.max( 0, (int) ( vb.detail * ( target.hitVec.zCoord - pos.getZ() ) - target.sideHit.getFrontOffsetZ() * 0.5 ) ) );

			final int itemBlock = vb.get( x, y, z );
			if ( itemBlock == 0 )
			{
				return null;
			}

			return ItemChiseledBit.createStack( itemBlock, 1, false );
		}

		return te.getItemStack( this, player );
	}

	@Override
	protected BlockState createBlockState()
	{
		return new ExtendedBlockState( this, new IProperty[0], new IUnlistedProperty[] { v_prop, block_prop, opacity_prop, side_prop, light_prop, n_prop } );
	}

	@Override
	public TileEntity createNewTileEntity(
			final World worldIn,
			final int meta )
	{
		return new TileEntityBlockChiseled();
	}

	@Override
	public boolean addLandingEffects(
			final WorldServer worldObj,
			final BlockPos blockPosition,
			final IBlockState iblockstate,
			final EntityLivingBase entity,
			final int numberOfParticles )
	{
		try
		{
			final IBlockState texture = getTileEntity( worldObj, blockPosition ).getParticleBlockState( Blocks.stone );
			worldObj.spawnParticle( EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block.getStateId( texture ) } );
			return true;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return false;
		}
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
			return ClientSide.instance.addBlockDestroyEffects( world, pos, state, effectRenderer );
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

	@Override
	public AxisAlignedBB getCollisionBoundingBox(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state )
	{
		return null;
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
	 *            mask and list should be null if not looking for collisions
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

			final BitCollisionIterator bi = new BitCollisionIterator();
			while ( bi.hasNext() )
			{
				if ( bi.getNext( vb ) != 0 )
				{
					if ( started )
					{
						minX = Math.min( minX, bi.physicalX );
						minY = Math.min( minY, bi.physicalY );
						minZ = Math.min( minZ, bi.physicalZ );
						maxX = Math.max( maxX, bi.physicalX + BitCollisionIterator.One16thf );
						maxY = Math.max( maxY, bi.physicalYp1 );
						maxZ = Math.max( maxZ, bi.physicalZp1 );
					}
					else
					{
						started = true;
						minX = bi.physicalX;
						minY = bi.physicalY;
						minZ = bi.physicalZ;
						maxX = bi.physicalX + BitCollisionIterator.One16thf;
						maxY = bi.physicalYp1;
						maxZ = bi.physicalZp1;
					}
				}

				// VERY hackey collision extraction to do 2 bounding boxes, one
				// for top and one for the bottom.
				if ( list != null && started && ( bi.y == 8 || bi.y == VoxelBlob.dim_minus_one ) )
				{
					final AxisAlignedBB bb = AxisAlignedBB.fromBounds(
							(double) minX + pos.getX(),
							(double) minY + pos.getY(),
							(double) minZ + pos.getZ(),
							(double) maxX + pos.getX(),
							(double) maxY + pos.getY(),
							(double) maxZ + pos.getZ() );

					setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if ( mask.intersectsWith( bb ) )
					{
						list.add( bb );
					}

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
		catch (

		final ExceptionNoTileEntity e )

		{
		}

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
			{
			}
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
		AxisAlignedBB br;
		double lastDist = 0;

		final float One16thf = 1.0f / vb.detail;

		for ( int z = 0; z < vb.detail; ++z )
		{
			final float z_One16thf = One16thf * z;
			final float z_One16thf_p1 = z_One16thf + One16thf;

			for ( int y = 0; y < vb.detail; ++y )
			{
				final float y_One16thf = One16thf * y;
				final float y_One16thf_p1 = y_One16thf + One16thf;

				for ( int x = 0; x < vb.detail; ++x )
				{
					if ( vb.get( x, y, z ) != 0 )
					{
						final float x_One16thf = One16thf * x;

						setBlockBounds( x_One16thf, y_One16thf, z_One16thf, x_One16thf + One16thf, y_One16thf_p1, z_One16thf_p1 );
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
				}
			}
		}

		setBlockBounds( 0, 0, 0, 1, 1, 1 );

		if ( selectedR != null )
		{
			final float One32ndf = 0.5f / VoxelBlob.dim;

			final int x = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, (int) ( VoxelBlob.dim * ( selectedR.hitVec.xCoord - pos.getX() - One32ndf * selectedR.sideHit.getFrontOffsetX() ) ) ) );
			final int y = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, (int) ( VoxelBlob.dim * ( selectedR.hitVec.yCoord - pos.getY() - One32ndf * selectedR.sideHit.getFrontOffsetY() ) ) ) );
			final int z = Math.min( VoxelBlob.dim_minus_one, Math.max( 0, (int) ( VoxelBlob.dim * ( selectedR.hitVec.zCoord - pos.getZ() - One32ndf * selectedR.sideHit.getFrontOffsetZ() ) ) ) );

			ChiselTypeIterator ci;
			final BlockPos drawStart = ClientSide.instance.getStartPos();

			if ( chMode == ChiselMode.DRAWN_REGION && drawStart != null )
			{
				if ( ClientSide.instance.sameDrawBlock( pos, x, y, z ) )
				{
					final int lowX = Math.max( 0, Math.min( x, drawStart.getX() ) );
					final int lowY = Math.max( 0, Math.min( y, drawStart.getY() ) );
					final int lowZ = Math.max( 0, Math.min( z, drawStart.getZ() ) );

					final int highX = Math.min( VoxelBlob.dim, Math.max( x, drawStart.getX() ) );
					final int highY = Math.min( VoxelBlob.dim, Math.max( y, drawStart.getY() ) );
					final int highZ = Math.min( VoxelBlob.dim, Math.max( z, drawStart.getZ() ) );

					ci = new ChiselTypeIterator( VoxelBlob.dim, lowX, lowY, lowZ, 1 + highX - lowX, 1 + highY - lowY, 1 + highZ - lowZ, selectedR.sideHit );
				}
				else
				{
					ci = new ChiselTypeIterator( VoxelBlob.dim, 0, 0, 0, 0, 0, 0, selectedR.sideHit );
				}
			}
			else
			{
				ci = new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vb, chMode, selectedR.sideHit );
			}

			boolean started = false;
			while ( ci.hasNext() )
			{
				if ( vb.get( ci.x(), ci.y(), ci.z() ) != 0 )
				{
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
				}
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

				final BitCollisionIterator bi = new BitCollisionIterator();
				while ( bi.hasNext() )
				{
					if ( bi.getNext( vb ) != 0 )
					{
						setBlockBounds( bi.physicalX, bi.physicalY, bi.physicalZ, bi.physicalX + BitCollisionIterator.One16thf, bi.physicalYp1, bi.physicalZp1 );
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
			}
			catch ( final ExceptionNoTileEntity e )
			{
			}

			setBlockBounds( 0, 0, 0, 1, 1, 1 );

			return br;
		}

		setBounds( worldIn, pos, null, null );
		final MovingObjectPosition r = super.collisionRayTrace( worldIn, pos, a, b );
		setBlockBounds( 0, 0, 0, 1, 1, 1 );

		return r;
	}

	private BlockBitInfo getBlockInfo(
			final IBlockState state )
	{
		BlockBitInfo bit = stateBitInfo.get( state );

		if ( bit == null )
		{
			bit = BlockBitInfo.createFromState( state );
			stateBitInfo.put( state, bit );
		}

		return bit;
	}

	@Override
	public float getBlockHardness(
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getBlockInfo( getTileEntity( worldIn, pos ).getBlockState( this ) ).hardness;
		}
		catch ( final ExceptionNoTileEntity e )
		{
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
			return getBlockInfo( getTileEntity( worldIn, pos ).getBlockState( this ) ).hardness;
		}
		catch ( final ExceptionNoTileEntity err )
		{
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
			return getBlockInfo( getTileEntity( world, pos ).getBlockState( this ) ).explosionResistance;
		}
		catch ( final ExceptionNoTileEntity err )
		{
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

	@Override
	public boolean canPlaceTorchOnTop(
			final IBlockAccess world,
			final BlockPos pos )
	{
		return isSideSolid( world, pos, EnumFacing.UP );
	}

	@Override
	public boolean isSideSolid(
			final IBlockAccess world,
			final BlockPos pos,
			final EnumFacing side )
	{
		try
		{
			return getTileEntity( world, pos ).isSideSolid( side );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return false;
		}
	}

	public static boolean supportsBlock(
			final IBlockState state )
	{
		final Block blk = state.getBlock();

		if ( supportedBlocks.containsKey( blk ) )
		{
			return supportedBlocks.get( blk );
		}

		try
		{
			// require basic hardness behavior...
			final ProxyBlock pb = new ProxyBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			// require default drop behavior...
			pb.quantityDropped( null );
			final Class<?> wc = blkClass.getMethod( pb.MethodName, Random.class ).getDeclaringClass();
			final boolean quantityDroppedTest = wc == Block.class || wc == BlockGlowstone.class || wc == BlockStainedGlass.class || wc == BlockGlass.class;

			pb.quantityDroppedWithBonus( 0, null );
			final boolean quantityDroppedWithBonusTest = blkClass.getMethod( pb.MethodName, int.class, Random.class ).getDeclaringClass() == Block.class || wc == BlockGlowstone.class;

			pb.quantityDropped( null, 0, null );
			final boolean quantityDropped2Test = blkClass.getMethod( pb.MethodName, IBlockState.class, int.class, Random.class ).getDeclaringClass() == Block.class;

			pb.onEntityCollidedWithBlock( null, null, null );
			final boolean entityCollisionTest = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			pb.onEntityCollidedWithBlock( null, null, null, null );
			final boolean entityCollision2Test = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, IBlockState.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			// full cube specifically is tied to lighting... so for glass
			// Compatibility use isFullBlock which can be true for glass.

			// final boolean isFullCube = blk.isFullCube()
			final boolean isFullBlock = blk.isFullBlock() || blkClass == BlockStainedGlass.class || blkClass == BlockGlass.class || blk == Blocks.slime_block;

			final BlockBitInfo info = BlockBitInfo.createFromState( state );

			final boolean requiredImplementation = quantityDroppedTest && quantityDroppedWithBonusTest && quantityDropped2Test && entityCollisionTest && entityCollision2Test;
			final boolean hasBehavior = blk.hasTileEntity( state ) || blk.getTickRandomly();

			final boolean supportedMaterial = ChiselsAndBits.instance.blocks.getConversion( blk.getMaterial() ) != null;

			if ( info.isCompatiable && requiredImplementation && info.hardness >= -0.01f && isFullBlock && supportedMaterial && !hasBehavior )
			{
				final boolean result = ChiselsAndBits.instance.config.isEnabled( blkClass.getName() );
				supportedBlocks.put( blk, result );

				if ( result )
				{
					stateBitInfo.put( state, info );
				}

				return result;
			}

			supportedBlocks.put( blk, false );
			return false;
		}
		catch ( final Throwable t )
		{
			// if the above test fails for any reason, then the block cannot be
			// supported.
			supportedBlocks.put( blk, false );
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
			getTileEntity( world, pos ).rotateBlock( axis );
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
			final IBlockState originalState,
			final int fragmentBlockStateID )
	{
		IBlockState actingState = originalState;
		Block target = originalState.getBlock();
		final boolean isAir = world.isAirBlock( pos );

		if ( supportsBlock( actingState ) || isAir )
		{
			BlockChiseled blk = ChiselsAndBits.instance.blocks.getConversion( target.getMaterial() );

			int BlockID = Block.getStateId( actingState );

			if ( isAir )
			{
				actingState = Block.getStateById( fragmentBlockStateID );
				target = actingState.getBlock();
				BlockID = Block.getStateId( actingState );
				blk = ChiselsAndBits.instance.blocks.getConversion( target.getMaterial() );
				// its still air tho..
				actingState = Blocks.air.getDefaultState();
			}

			if ( BlockID == 0 )
			{
				return false;
			}

			if ( blk != null && blk != target )
			{
				replacementLightValue.set( actingState.getBlock().getLightValue() );

				world.setBlockState( pos, blk.getDefaultState() );
				final TileEntity te = world.getTileEntity( pos );

				TileEntityBlockChiseled tec;
				if ( !( te instanceof TileEntityBlockChiseled ) )
				{
					tec = (TileEntityBlockChiseled) blk.createTileEntity( world, blk.getDefaultState() );
					world.setTileEntity( pos, tec );
				}
				else
				{
					tec = (TileEntityBlockChiseled) te;
				}

				tec.fillWith( actingState );
				tec.setState( tec.getBasicState().withProperty( BlockChiseled.block_prop, BlockID ) );

				replacementLightValue.remove();

				return true;
			}
		}

		return false;
	}

	public IBlockState getCommonState(
			final IExtendedBlockState myState )
	{
		final VoxelBlobStateReference data = myState.getValue( BlockChiseled.v_prop );

		if ( data != null )
		{
			final VoxelBlob vb = data.getVoxelBlob();
			if ( vb != null )
			{
				return Block.getStateById( vb.mostCommonBlock().ref );
			}
		}

		return null;
	}

	@Override
	public int getLightValue(
			final IBlockAccess world,
			final BlockPos pos )
	{
		// is this the right block?
		final Block block = world.getBlockState( pos ).getBlock();
		if ( block != this )
		{
			return block.getLightValue( world, pos );
		}

		// enabled?
		if ( ChiselsAndBits.instance.config.enableBitLightSource )
		{
			try
			{
				final Integer rlv = replacementLightValue.get();
				if ( rlv != null )
				{
					return rlv;
				}

				final Integer lv = getTileEntity( world, pos ).getBasicState().getValue( BlockChiseled.light_prop );
				return lv == null ? 0 : lv;
			}
			catch ( final ExceptionNoTileEntity e )
			{
				// nope..
			}
		}

		return 0;
	}

	@Override
	public IBlockState getActualState(
			final IBlockState state,
			final IBlockAccess worldIn,
			final BlockPos pos )
	{
		// only if this feature is enable should this code ever run.
		if ( ChiselsAndBits.instance.config.enableToolHarvestLevels )
		{
			try
			{
				// require a real world, and extended bloack state..
				if ( state instanceof IExtendedBlockState && worldIn instanceof World )
				{
					// this is pure insanity, but there is no other solution
					// without core modding.
					final Exception e = new Exception();
					final StackTraceElement[] elements = e.getStackTrace();

					if ( elements != null && elements.length > 2 )
					{
						final String cname = elements[1].getClassName();

						// test to see if the hook is asking for this.
						if ( cname.contains( "minecraftforge" ) )
						{
							final TileEntityBlockChiseled tebc = getTileEntity( worldIn, pos );
							return tebc.getBasicState();
						}
					}
				}
			}
			catch ( final ExceptionNoTileEntity e )
			{
			}
		}

		return super.getActualState( state, worldIn, pos );
	}

	@Override
	public String getHarvestTool(
			final IBlockState state )
	{
		if ( actingAs != null && actingAs.getBlock() != this )
		{
			return actingAs.getBlock().getHarvestTool( actingAs );
		}

		if ( ChiselsAndBits.instance.config.enableToolHarvestLevels && state instanceof IExtendedBlockState )
		{
			final IBlockState blockRef = getCommonState( (IExtendedBlockState) state );
			if ( blockRef != null )
			{
				String tool = blockRef.getBlock().getHarvestTool( blockRef );
				if ( tool == null )
				{
					tool = "pickaxe";
				}
				return tool;
			}
		}

		return super.getHarvestTool( state );
	}

	@Override
	public int getHarvestLevel(
			final IBlockState state )
	{
		if ( actingAs != null && actingAs.getBlock() != this )
		{
			return actingAs.getBlock().getHarvestLevel( actingAs );
		}

		if ( ChiselsAndBits.instance.config.enableToolHarvestLevels && state instanceof IExtendedBlockState )
		{
			final IBlockState blockRef = getCommonState( (IExtendedBlockState) state );
			if ( blockRef != null )
			{
				return blockRef.getBlock().getHarvestLevel( blockRef );
			}
		}

		return super.getHarvestLevel( state );
	}

	public String getModel()
	{
		return ChiselsAndBits.MODID + ":" + name;
	}

	@Override
	public void getSubBlocks(
			final Item itemIn,
			final CreativeTabs tab,
			final List<ItemStack> list )
	{
		// no items.
	}

}
