package mod.chiselsandbits.chiseledblock;

import java.util.Collections;
import java.util.List;

import mod.chiselsandbits.chiseledblock.data.BitCollisionIterator;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.chiseledblock.properties.UnlistedBlockFlags;
import mod.chiselsandbits.chiseledblock.properties.UnlistedBlockStateID;
import mod.chiselsandbits.chiseledblock.properties.UnlistedLightOpacity;
import mod.chiselsandbits.chiseledblock.properties.UnlistedLightValue;
import mod.chiselsandbits.chiseledblock.properties.UnlistedVoxelBlob;
import mod.chiselsandbits.chiseledblock.properties.UnlistedVoxelNeighborState;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ChiselToolType;
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

	private static ThreadLocal<Integer> replacementLightValue = new ThreadLocal<Integer>();
	private static ThreadLocal<IBlockState> actingAs = new ThreadLocal<IBlockState>();

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
			Log.noTileError( e );
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
			Log.noTileError( e );
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
	@SideOnly( Side.CLIENT )
	public int colorMultiplier(
			final IBlockAccess worldIn,
			final BlockPos pos,
			final int tint )
	{
		final Block blk = Block.getStateById( tint ).getBlock();
		return blk.colorMultiplier( worldIn, pos, 0 );
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
			Log.noTileError( e );
			return state;
		}
		catch ( final Throwable err )
		{
			Log.logError( "Unable to get extended state...", err );
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
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
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
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
			super.harvestBlock( worldIn, player, pos, state, (TileEntity) null );
		}
	}

	@Override
	public List<ItemStack> getDrops(
			final IBlockAccess world,
			final BlockPos pos,
			final IBlockState state,
			final int fortune )
	{
		try
		{
			return Collections.singletonList( getTileEntity( world, pos ).getItemStack( this, null ) );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
			return Collections.emptyList();
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
			Log.noTileError( e );
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
			Log.noTileError( e );
			return null;
		}
	}

	public ItemStack getPickBlock(
			final MovingObjectPosition target,
			final BlockPos pos,
			final TileEntityBlockChiseled te )
	{
		if ( ClientSide.instance.getHeldToolType() != null )
		{
			final VoxelBlob vb = te.getBlob();

			final BitLocation bitLoc = new BitLocation( target, true, ChiselToolType.CHISEL );

			final int itemBlock = vb.get( bitLoc.bitX, bitLoc.bitY, bitLoc.bitZ );
			if ( itemBlock == 0 )
			{
				return null;
			}

			return ItemChiseledBit.createStack( itemBlock, 1, false );
		}

		return te.getItemStack( this, ClientSide.instance.getPlayer() );
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
			final IBlockState texture = getTileEntity( worldObj, blockPosition ).getBlockState( Blocks.stone );
			worldObj.spawnParticle( EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block.getStateId( texture ) } );
			return true;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
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
			final IBlockState state = getTileEntity( world, pos ).getBlockState( this );
			return ClientSide.instance.addBlockDestroyEffects( world, pos, state, effectRenderer );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
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
			final IBlockState state = getTileEntity( world, pos ).getBlockState( this );
			return ClientSide.instance.addHitEffects( world, target, state, effectRenderer );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
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
		try
		{
			addCollisionBoxesToList( getTileEntity( worldIn, pos ), pos, mask, list, collidingEntity );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
		}
	}

	public void addCollisionBoxesToList(
			final TileEntityBlockChiseled te,
			final BlockPos pos,
			final AxisAlignedBB mask,
			final List<AxisAlignedBB> list,
			final Entity collidingEntity )
	{
		final AxisAlignedBB localMask = mask.offset( -pos.getX(), -pos.getY(), -pos.getZ() );

		for ( final AxisAlignedBB bb : te.getBoxes( true ) )
		{
			if ( bb.intersectsWith( localMask ) )
			{
				list.add( bb.offset( pos.getX(), pos.getY(), pos.getZ() ) );
			}
		}
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
	private Block setBounds(
			final TileEntityBlockChiseled tec,
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

		final Block b = getTestBlock();

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

		b.setBlockBounds( minX, minY, minZ, maxX, maxY, maxZ );
		return b; // started ;
	}

	private final ThreadLocal<Block> testBlock = new ThreadLocal<Block>();

	private Block getTestBlock()
	{
		Block b = testBlock.get();

		if ( b == null )
		{
			b = new Block( Material.rock );
			testBlock.set( b );
		}

		return b;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getSelectedBoundingBox( getTileEntity( worldIn, pos ), pos );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
		}

		return super.getSelectedBoundingBox( worldIn, pos );
	}

	public AxisAlignedBB getSelectedBoundingBox(
			final TileEntityBlockChiseled tec,
			final BlockPos pos )
	{
		return getBoundingBox( setBounds( tec, pos, null, null ), pos );
	}

	private AxisAlignedBB getBoundingBox(
			final Block target,
			final BlockPos pos )
	{
		return new AxisAlignedBB(
				pos.getX() + target.getBlockBoundsMinX(),
				pos.getY() + target.getBlockBoundsMinY(),
				pos.getZ() + target.getBlockBoundsMinZ(),
				pos.getX() + target.getBlockBoundsMaxX(),
				pos.getY() + target.getBlockBoundsMaxY(),
				pos.getZ() + target.getBlockBoundsMaxZ() );
	}

	@Override
	public MovingObjectPosition collisionRayTrace(
			final World worldIn,
			final BlockPos pos,
			final Vec3 a,
			final Vec3 b )
	{
		try
		{
			return collisionRayTrace( getTileEntity( worldIn, pos ), pos, a, b, worldIn.isRemote );
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
		}

		return super.collisionRayTrace( worldIn, pos, a, b );
	}

	public MovingObjectPosition collisionRayTrace(
			final TileEntityBlockChiseled tec,
			final BlockPos pos,
			final Vec3 a,
			final Vec3 b,
			final boolean realTest )
	{
		final Block boundsToTest = getTestBlock();

		MovingObjectPosition br = null;
		double lastDist = 0;

		for ( final AxisAlignedBB box : tec.getBoxes( false ) )
		{
			boundsToTest.setBlockBounds( (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ );
			final MovingObjectPosition r = boundsToTest.collisionRayTrace( null, pos, a, b );

			if ( r != null )
			{
				final double xLen = a.xCoord - r.hitVec.xCoord;
				final double yLen = a.yCoord - r.hitVec.yCoord;
				final double zLen = a.zCoord - r.hitVec.zCoord;

				final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
				if ( br == null || lastDist > thisDist && r != null )
				{
					lastDist = thisDist;
					br = r;
				}

			}
		}

		setBlockBounds( 0, 0, 0, 1, 1, 1 );
		return br;
	}

	@Override
	public float getBlockHardness(
			final World worldIn,
			final BlockPos pos )
	{
		try
		{
			return getTileEntity( worldIn, pos ).getBlockInfo( this ).hardness;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
			return super.getBlockHardness( worldIn, pos );
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
			return getTileEntity( world, pos ).getBlockInfo( this ).explosionResistance;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			Log.noTileError( e );
			return super.getExplosionResistance( world, pos, exploder, explosion );
		}
	}

	public static boolean replaceWithChisled(
			final World world,
			final BlockPos pos,
			final IBlockState originalState,
			final boolean triggerUpdate )
	{
		return replaceWithChisled( world, pos, originalState, 0, triggerUpdate );
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
			Log.noTileError( e );
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
			Log.noTileError( e );
			return false;
		}
	}

	public static boolean replaceWithChisled(
			final World world,
			final BlockPos pos,
			final IBlockState originalState,
			final int fragmentBlockStateID,
			final boolean triggerUpdate )
	{
		IBlockState actingState = originalState;
		Block target = originalState.getBlock();
		final boolean isAir = world.isAirBlock( pos );

		if ( BlockBitInfo.supportsBlock( actingState ) || isAir )
		{
			BlockChiseled blk = ChiselsAndBits.getBlocks().getConversion( target );

			int BlockID = Block.getStateId( actingState );

			if ( isAir )
			{
				actingState = Block.getStateById( fragmentBlockStateID );
				target = actingState.getBlock();
				BlockID = Block.getStateId( actingState );
				blk = ChiselsAndBits.getBlocks().getConversion( target );
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

				world.setBlockState( pos, blk.getDefaultState(), triggerUpdate ? 3 : 0 );
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
				return Block.getStateById( vb.getVoxelStats().mostCommonState );
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
		if ( ChiselsAndBits.getConfig().enableBitLightSource )
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
				Log.noTileError( e );
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
		if ( ChiselsAndBits.getConfig().enableToolHarvestLevels )
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
				Log.noTileError( e );
			}
		}

		return super.getActualState( state, worldIn, pos );
	}

	public static void setActingAs(
			final IBlockState state )
	{
		actingAs.set( state );
	}

	@Override
	public String getHarvestTool(
			final IBlockState state )
	{
		final IBlockState actingAsState = actingAs.get();

		if ( actingAsState != null && actingAsState.getBlock() != this )
		{
			return actingAsState.getBlock().getHarvestTool( actingAsState );
		}

		if ( ChiselsAndBits.getConfig().enableToolHarvestLevels && state instanceof IExtendedBlockState )
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
		final IBlockState actingAsState = actingAs.get();

		if ( actingAsState != null && actingAsState.getBlock() != this )
		{
			return actingAsState.getBlock().getHarvestLevel( actingAsState );
		}

		if ( ChiselsAndBits.getConfig().enableToolHarvestLevels && state instanceof IExtendedBlockState )
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
