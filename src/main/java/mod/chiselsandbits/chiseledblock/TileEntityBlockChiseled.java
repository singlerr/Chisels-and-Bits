package mod.chiselsandbits.chiseledblock;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.api.EventBlockBitPostModification;
import mod.chiselsandbits.api.EventFullBlockRestoration;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IChiseledBlockTileEntity;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.VoxelStats;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.render.chiseledblock.tesr.ChisledBlockRenderChunkTESR;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class TileEntityBlockChiseled extends TileEntity implements IChiseledTileContainer, IChiseledBlockTileEntity
{

    public TileEntityBlockChiseled(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public static class TileEntityBlockChiseledDummy extends TileEntityBlockChiseled
	{
        public TileEntityBlockChiseledDummy(final TileEntityType<?> tileEntityTypeIn)
        {
            super(tileEntityTypeIn);
        }
    };

	public IChiseledTileContainer occlusionState;

	boolean isNormalCube = false;
	int sideState = 0;
	int lightlevel = -1;

	private BlockState state;
	private VoxelNeighborRenderTracker neighborRenderTracker;
	private VoxelBlobStateReference    blobStateReference;
	private int                        primaryBlockStateId;

	// used to initialize light level before I can properly feed things into the
	// tile entity, half 2 of fixing inital lighting issues.
	private static ThreadLocal<Integer> localLightLevel = new ThreadLocal<Integer>();

    public VoxelNeighborRenderTracker getNeighborRenderTracker()
    {
        return neighborRenderTracker;
    }

    public void setNeighborRenderTracker(final VoxelNeighborRenderTracker neighborRenderTracker)
    {
        this.neighborRenderTracker = neighborRenderTracker;
        onDataUpdate();
    }

    public VoxelBlobStateReference getBlobStateReference()
    {
        return blobStateReference;
    }

    private void setBlobStateReference(final VoxelBlobStateReference blobStateReference)
    {
        this.blobStateReference = blobStateReference;
        onDataUpdate();
    }

    public int getPrimaryBlockStateId()
    {
        return primaryBlockStateId;
    }

    public void setPrimaryBlockStateId(final int primaryBlockStateId)
    {
        this.primaryBlockStateId = primaryBlockStateId;
        onDataUpdate();
    }

    public IChiseledTileContainer getTileContainer()
	{
		if ( occlusionState != null )
		{
			return occlusionState;
		}

		return this;
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		return false;
	}

	@Override
	public void saveData()
	{
		super.markDirty();
	}

	@Override
	public void sendUpdate()
	{
		ModUtil.sendUpdate( getWorld(), pos );
	}

	public void copyFrom(
			final TileEntityBlockChiseled src )
	{
	    state = src.state;
	    neighborRenderTracker = src.neighborRenderTracker;
	    blobStateReference = src.blobStateReference;
	    primaryBlockStateId = src.primaryBlockStateId;
		isNormalCube = src.isNormalCube;
		sideState = src.sideState;
		lightlevel = src.lightlevel;
	}

	public BlockState getBasicState()
	{
		return getState( false, 0, world );
	}

	public BlockState getRenderState(
			final IBlockReader access )
	{
		return getState( true, 1, access );
	}

	protected boolean supportsSwapping()
	{
		return true;
	}

	@Nonnull
	protected BlockState getState(
			final boolean updateNeightbors,
			final int updateCost,
			final IBlockReader access )
	{
			return ChiselsAndBits.getBlocks().getChiseledDefaultState();

		if ( updateNeightbors )
		{
			final boolean isDyanmic = this instanceof TileEntityBlockChiseledTESR;

			final VoxelNeighborRenderTracker vns = getNeighborRenderTracker();
			if ( vns == null )
			{
				return state;
			}

			vns.update( isDyanmic, access, pos );
			tesrUpdate( access, vns );

			final TileEntityBlockChiseled self = this;
			if ( supportsSwapping() && vns.isAboveLimit() && !isDyanmic )
			{
				ChisledBlockRenderChunkTESR.addNextFrameTask(() -> {
                    if ( self.world != null && self.pos != null )
                    {
                        final TileEntity current = self.world.getTileEntity( self.pos );

                        if ( current == null || self.isRemoved() )
                        {
                            return;
                        }

                        if ( current == self )
                        {
                            current.remove();
                            final TileEntityBlockChiseledTESR TESR = new TileEntityBlockChiseledTESR();
                            TESR.copyFrom( self );
                            self.world.removeTileEntity( self.pos );
                            self.world.setTileEntity( self.pos, TESR );
                            self.world.markBlockRangeForRenderUpdate( self.pos, self.world.getBlockState(pos), Blocks.AIR.getDefaultState() );
                            vns.unlockDynamic();
                        }
                    }
                });
			}
			else if ( supportsSwapping() && !vns.isAboveLimit() && isDyanmic )
			{
				ChisledBlockRenderChunkTESR.addNextFrameTask( new Runnable() {

					@Override
					public void run()
					{
						if ( self.world != null && self.pos != null )
						{
							final TileEntity current = self.world.getTileEntity( self.pos );

							if ( current == null || self.isRemoved() )
							{
								return;
							}

							if ( current == self )
							{
								current.remove();
								final TileEntityBlockChiseled nonTesr = new TileEntityBlockChiseled();
								nonTesr.copyFrom( self );
								self.world.removeTileEntity( self.pos );
								self.world.setTileEntity( self.pos, nonTesr );
								self.world.markBlockRangeForRenderUpdate( self.pos, world.getBlockState(pos), Blocks.AIR.getDefaultState());
								vns.unlockDynamic();
							}
						}
					}

				} );
			}
		}

		return state;
	}

	protected void onDataUpdate() {

    }

	protected void tesrUpdate(
			final IBlockReader access,
			final VoxelNeighborRenderTracker vns )
	{

	}

	public BlockBitInfo getBlockInfo(
			final Block alternative )
	{
		return BlockBitInfo.getBlockInfo( getBlockState( alternative ) );
	}

	public BlockState getBlockState(
			final Block alternative )
	{
		final Integer stateID = getPrimaryBlockStateId();

		if ( stateID != null )
		{
			final BlockState state = ModUtil.getStateById( stateID );
			if ( state != null )
			{
				return state;
			}
		}

		return alternative.getDefaultState();
	}

	public void setState(
	        final BlockState blockState,
			final VoxelBlobStateReference newRef )
	{
		final VoxelBlobStateReference originalRef = getBlobStateReference();

		this.state = blockState;

		if ( originalRef != null && newRef != null && !newRef.equals( originalRef ) )
		{
			final EventBlockBitPostModification bmm = new EventBlockBitPostModification( getWorld(), getPos() );
			MinecraftForge.EVENT_BUS.post( bmm );
			setBlobStateReference(newRef);
		}
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		final CompoundNBT nbttagcompound = new CompoundNBT();
		writeChisleData( nbttagcompound );

		if ( nbttagcompound.size() == 0 )
		{
			return null;
		}

		return new SUpdateTileEntityPacket( pos, 255, nbttagcompound );
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		final CompoundNBT nbttagcompound = new CompoundNBT();

		nbttagcompound.putInt( "x", pos.getX() );
		nbttagcompound.putInt( "y", pos.getY() );
		nbttagcompound.putInt( "z", pos.getZ() );

		writeChisleData( nbttagcompound );

		return nbttagcompound;
	}

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        readChisleData( tag );
    }

	@Override
	public void onDataPacket(
			final NetworkManager net,
			final SUpdateTileEntityPacket pkt )
	{
		final int oldLight = lightlevel;
		final boolean changed = readChisleData( pkt.getNbtCompound() );

		if ( world != null && changed )
		{
			world.markBlockRangeForRenderUpdate( pos, world.getBlockState(pos), Blocks.AIR.getDefaultState());
			triggerDynamicUpdates();

			// fixes lighting on placement when tile packet arrives.
			if ( oldLight != lightlevel )
			{
				world.getLightManager().checkBlock(pos);
			}
		}
	}

	/**
	 * look at near by TESRs and re-render them.
	 */
	private void triggerDynamicUpdates()
	{
		if ( world.isRemote && state != null )
		{
			final VoxelNeighborRenderTracker vns = getNeighborRenderTracker();

			// will it update anyway?
			if ( vns != null && vns.isDynamic() )
			{
				return;
			}

			for ( final Direction f : Direction.values() )
			{
				final BlockPos p = getPos().offset( f );
				if ( world.isBlockLoaded( p ) )
				{
					final TileEntity te = world.getTileEntity( p );
					if ( te instanceof TileEntityBlockChiseledTESR )
					{
						final TileEntityBlockChiseledTESR tesr = (TileEntityBlockChiseledTESR) te;

						if ( tesr.getRenderChunk() != null )
						{
							tesr.getRenderChunk().rebuild( false );
						}
					}
				}
			}
		}
	}

	public boolean readChisleData(
			final CompoundNBT tag )
	{
		final NBTBlobConverter converter = new NBTBlobConverter( false, this );
		final boolean changed = converter.readChisleData( tag, VoxelBlob.VERSION_COMPACT );

		final VoxelNeighborRenderTracker vns = getNeighborRenderTracker();

		if ( vns != null )
		{
			vns.triggerUpdate();
		}

		return changed;
	}

	public void writeChisleData(
			final CompoundNBT tag )
	{
		new NBTBlobConverter( false, this ).writeChisleData( tag, false );
	}

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        final CompoundNBT nbt = super.write(compound);
        writeChisleData(nbt);
        return nbt;
    }

    @Override
    public void read(final BlockState state, final CompoundNBT nbt)
    {
        super.read(state, nbt);
        readChisleData(nbt);
    }

    @Override
	public CompoundNBT writeTileEntityToTag(
			final CompoundNBT tag,
			final boolean crossWorld )
	{
		final CompoundNBT superNbt = super.write(tag);
		new NBTBlobConverter( false, this ).writeChisleData( superNbt, crossWorld );
        superNbt.putBoolean( "cw", crossWorld );
		return superNbt;
	}

	@Override
	public void mirror(
			final Mirror p_189668_1_ )
	{
		switch ( p_189668_1_ )
		{
			case FRONT_BACK:
				setBlob( getBlob().mirror( Axis.X ), true );
				break;
			case LEFT_RIGHT:
				setBlob( getBlob().mirror( Axis.Z ), true );
				break;
			case NONE:
			default:
				break;

		}
	}

	@Override
	public void rotate(
			final Rotation p_189667_1_ )
	{
		VoxelBlob blob = ModUtil.rotate( getBlob(), Axis.Y, p_189667_1_ );
		if ( blob != null )
		{
			setBlob( blob, true );
		}
	}

	public void fillWith(
			final BlockState blockType )
	{
		final int ref = ModUtil.getStateId( blockType );

		sideState = 0xff;
		lightlevel = DeprecationHelper.getLightValue( blockType );
		isNormalCube = ModUtil.isNormalCube( blockType );

		BlockState defaultState = getBasicState();
			        // .withProperty( BlockChiseled.UProperty_VoxelBlob, );

		final VoxelNeighborRenderTracker tracker = getNeighborRenderTracker();

		if ( tracker == null )
		{
		    setNeighborRenderTracker(new VoxelNeighborRenderTracker());
		}
		else
		{
			tracker.isDynamic();
		}

		// required for placing bits
		if ( ref != 0 )
		{
		    setPrimaryBlockStateId(ref);
		}

		setState( defaultState, new VoxelBlobStateReference( ModUtil.getStateId( blockType ), getPositionRandom( pos ) )  );

		getTileContainer().saveData();
	}

	public static long getPositionRandom(
			final BlockPos pos )
	{
		if ( pos != null && EffectiveSide.get().isClient())
		{
			return MathHelper.getPositionRandom( pos );
		}

		return 0;
	}

	public VoxelBlob getBlob()
	{
		VoxelBlob vb = null;
		final VoxelBlobStateReference vbs = getBlobStateReference();

		if ( vbs != null )
		{
			vb = vbs.getVoxelBlob();

			if ( vb == null )
			{
				vb = new VoxelBlob();
				vb.fill( ModUtil.getStateId( Blocks.COBBLESTONE.getDefaultState() ) );
			}
		}
		else
		{
			vb = new VoxelBlob();
		}

		return vb;
	}

	public BlockState getPreferedBlock()
	{
		return ChiselsAndBits.getBlocks().getConversionWithDefault( getBlockState( Blocks.STONE ) ).getDefaultState();
	}

	public void setBlob(
			final VoxelBlob vb )
	{
		setBlob( vb, true );
	}

	public boolean updateBlob(
			final NBTBlobConverter converter,
			final boolean triggerUpdates )
	{
		final int oldLV = getLightValue();
		final boolean oldNC = isNormalCube();
		final int oldSides = sideState;

		final VoxelBlobStateReference originalRef = getBlobStateReference();

		VoxelBlobStateReference voxelRef = null;

		sideState = converter.getSideState();
		final int b = converter.getPrimaryBlockStateID();
		lightlevel = converter.getLightValue();
		isNormalCube = converter.isNormalCube();

		try
		{
			voxelRef = converter.getVoxelRef( VoxelBlob.VERSION_COMPACT, getPositionRandom( pos ) );
		}
		catch ( final Exception e )
		{
			if ( getPos() != null )
			{
				Log.logError( "Unable to read blob at " + getPos(), e );
			}
			else
			{
				Log.logError( "Unable to read blob.", e );
			}

			voxelRef = new VoxelBlobStateReference( 0, getPositionRandom( pos ) );
		}


		setNeighborRenderTracker(new VoxelNeighborRenderTracker());
		setPrimaryBlockStateId(b);
		setBlobStateReference(voxelRef);
		setState( getBasicState(), voxelRef );

		if ( getWorld() != null && triggerUpdates )
		{
			if ( oldLV != getLightValue() || oldNC != isNormalCube() )
			{
				getWorld().getLightManager().checkBlock( pos );

				// update block state to reflect lighting characteristics
				final BlockState state = getWorld().getBlockState( pos );
				if ( state.isNormalCube(new SingleBlockBlockReader(state), BlockPos.ZERO) != isNormalCube && state.getBlock() instanceof BlockChiseled )
				{
					getWorld().setBlockState( pos, state.with( BlockChiseled.LProperty_FullBlock, isNormalCube ) );
				}
			}

			if ( oldSides != sideState )
			{
				world.notifyNeighborsOfStateChange( pos, world.getBlockState( pos ).getBlock() );
			}
		}

		return voxelRef != null ? !voxelRef.equals( originalRef ) : true;
	}

	public void setBlob(
			final VoxelBlob vb,
			final boolean triggerUpdates )
	{
		final Integer olv = getLightValue();
		final Boolean oldNC = isNormalCube();

		final VoxelStats common = vb.getVoxelStats();
		final float light = common.blockLight;
		final boolean nc = common.isNormalBlock;
		final int lv = Math.max( 0, Math.min( 15, (int) ( light * 15 ) ) );

		// are most of the bits in the center solid?
		final int sideFlags = vb.getSideFlags( 5, 11, 4 * 4 );

		if ( getWorld() == null )
		{
			if ( common.mostCommonState == 0 )
			{
				Integer i = getPrimaryBlockStateId();
				if ( i != null )
				{
					common.mostCommonState = i;
				}
				else
				{
					// default to some other non-zero state.
					common.mostCommonState = ModUtil.getStateId( Blocks.STONE.getDefaultState() );
				}
			}

			sideState = sideFlags;
			lightlevel = lv;
			isNormalCube = nc;

			setNeighborRenderTracker(new VoxelNeighborRenderTracker());
            setBlobStateReference(new VoxelBlobStateReference( vb.blobToBytes( VoxelBlob.VERSION_COMPACT ), getPositionRandom( pos ) ) );
            setPrimaryBlockStateId(common.mostCommonState);
			setState( getBasicState(), getBlobStateReference() );
			return;
		}

		if ( common.isFullBlock )
		{
		    setBlobStateReference(new VoxelBlobStateReference( common.mostCommonState, getPositionRandom( pos ) ) );
			setState( getBasicState(), getBlobStateReference());

			final BlockState newState = ModUtil.getStateById( common.mostCommonState );
			if ( ChiselsAndBits.getConfig().getServer().canRevertToBlock( newState ) )
			{
				if ( !MinecraftForge.EVENT_BUS.post( new EventFullBlockRestoration( world, pos, newState ) ) )
				{
					world.setBlockState( pos, newState, triggerUpdates ? 3 : 0 );
				}
			}
		}
		else if ( common.mostCommonState != 0 )
		{
			sideState = sideFlags;
			lightlevel = lv;
			isNormalCube = nc;

			setBlobStateReference(new VoxelBlobStateReference( vb.blobToBytes( VoxelBlob.VERSION_COMPACT ), getPositionRandom( pos ) ) );
			setPrimaryBlockStateId(common.mostCommonState);
			setState( getBasicState(), getBlobStateReference());

			getTileContainer().saveData();
			getTileContainer().sendUpdate();

			// since its possible for bits to occlude parts.. update every time.
			final Block blk = world.getBlockState( pos ).getBlock();
			// worldObj.notifyBlockOfStateChange( pos, blk, false );

			if ( triggerUpdates )
			{
				world.notifyNeighborsOfStateChange( pos, blk );
			}
		}
		else
		{
		    setBlobStateReference(new VoxelBlobStateReference( 0, getPositionRandom( pos ) ) );
			setState( getBasicState(), getBlobStateReference());

			ModUtil.removeChiseledBlock( world, pos );
		}

		if ( olv != lv || oldNC != nc )
		{
			world.getLightManager().checkBlock( pos );

			// update block state to reflect lighting characteristics
			final BlockState state = world.getBlockState( pos );
			if ( state.isNormalCube(new SingleBlockBlockReader(state), BlockPos.ZERO) != isNormalCube && state.getBlock() instanceof BlockChiseled )
			{
				world.setBlockState( pos, state.with( BlockChiseled.LProperty_FullBlock, isNormalCube ) );
			}
		}
	}

	static private class ItemStackGeneratedCache
	{
		public ItemStackGeneratedCache(
				final ItemStack itemstack,
				final VoxelBlobStateReference blobStateReference,
				final int rotations2 )
		{
			out = itemstack == null ? null : itemstack.copy();
			ref = blobStateReference;
			rotations = rotations2;
		}

		final ItemStack out;
		final VoxelBlobStateReference ref;
		final int rotations;

		public ItemStack getItemStack()
		{
			return out == null ? null : out.copy();
		}
	};

	/**
	 * prevent mods that constantly ask for pick block from killing the
	 * client... ( looking at you waila )
	 **/
	private ItemStackGeneratedCache pickcache = null;

	public ItemStack getItemStack(
			final PlayerEntity player )
	{
		final ItemStackGeneratedCache cache = pickcache;

		if ( player != null )
		{
			Direction enumfacing = ModUtil.getPlaceFace( player );
			final int rotations = ModUtil.getRotationIndex( enumfacing );

			if ( cache != null && cache.rotations == rotations && cache.ref == getBlobStateReference() && cache.out != null )
			{
				return cache.getItemStack();
			}

			VoxelBlob vb = getBlob();

			int countDown = rotations;
			while ( countDown > 0 )
			{
				countDown--;
				enumfacing = enumfacing.rotateYCCW();
				vb = vb.spin( Axis.Y );
			}

			final BitAccess ba = new BitAccess( null, null, vb, VoxelBlob.NULL_BLOB );
			final ItemStack itemstack = ba.getBitsAsItem( enumfacing, ItemType.CHISLED_BLOCK, false );

			pickcache = new ItemStackGeneratedCache( itemstack, getBlobStateReference(), rotations );
			return itemstack;
		}
		else
		{
			if ( cache != null && cache.rotations == 0 && cache.ref == getBlobStateReference() )
			{
				return cache.getItemStack();
			}

			final BitAccess ba = new BitAccess( null, null, getBlob(), VoxelBlob.NULL_BLOB );
			final ItemStack itemstack = ba.getBitsAsItem( null, ItemType.CHISLED_BLOCK, false );

			pickcache = new ItemStackGeneratedCache( itemstack, getBlobStateReference(), 0 );
			return itemstack;
		}
	}

	public boolean isNormalCube()
	{
		return isNormalCube;
	}

	public boolean isSideSolid(
			final Direction side )
	{
		return ( sideState & 1 << side.ordinal() ) != 0;
	}

	public boolean isSideOpaque(
			final Direction side )
	{
		if ( this.getWorld() != null && this.getWorld().isRemote )
		{
			return isInnerSideOpaque( side );
		}

		return false;
	}

	@OnlyIn( Dist.CLIENT )
	public boolean isInnerSideOpaque(
			final Direction side )
	{
		final VoxelNeighborRenderTracker vns = getNeighborRenderTracker();
		if ( vns != null && vns.isDynamic() )
		{
			return false;
		}

		final Integer sideFlags = ChiseledBlockSmartModel.getSides( this );
		return ( sideFlags & 1 << side.ordinal() ) != 0;
	}

	public void completeEditOperation(
			final VoxelBlob vb )
	{
		final VoxelBlobStateReference before = getBlobStateReference();
		setBlob( vb );
		final VoxelBlobStateReference after = getBlobStateReference();

		if ( world != null )
		{
			world.markBlockRangeForRenderUpdate( pos, world.getBlockState(pos), Blocks.AIR.getDefaultState() );
			triggerDynamicUpdates();
		}

		UndoTracker.getInstance().add( getWorld(), getPos(), before, after );
	}

	//TODO: Figure this out.
	public void rotateBlock(
			final Rotation axis )
	{
		final VoxelBlob occluded = new VoxelBlob();

		VoxelBlob postRotation = getBlob();
		int maxRotations = 4;
		while ( --maxRotations > 0 )
		{
			postRotation = postRotation.spin( Axis.Y );

			if ( occluded.canMerge( postRotation ) )
			{
				setBlob( postRotation );
				return;
			}
		}
	}

	public boolean canMerge(
			final VoxelBlob voxelBlob )
	{
		final VoxelBlob vb = getBlob();
		final IChiseledTileContainer occ = getTileContainer();

		if ( vb.canMerge( voxelBlob ) && !occ.isBlobOccluded( voxelBlob ) )
		{
			return true;
		}

		return false;
	}

	@Override
	public Collection<AxisAlignedBB> getBoxes(
			final BoxType type )
	{
		final VoxelBlobStateReference ref = getBlobStateReference();

		if ( ref != null )
		{
			return ref.getBoxes( type );
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		final BlockPos p = getPos();
		return new AxisAlignedBB( p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1 );
	}

	public void setNormalCube(
			final boolean b )
	{
		isNormalCube = b;
	}

	public static void setLightFromBlock(
			final BlockState defaultState )
	{
		if ( defaultState == null )
		{
			localLightLevel.remove();
		}
		else
		{
			localLightLevel.set( DeprecationHelper.getLightValue( defaultState ) );
		}
	}

	public int getLightValue()
	{
		// first time requested, pull from local, or default to 0
		if ( lightlevel < 0 )
		{
			final Integer tmp = localLightLevel.get();
			lightlevel = tmp == null ? 0 : tmp;
		}

		return lightlevel;
	}

    @Override
    public void remove()
    {
        super.remove();
        if ( world != null )
        {
            triggerDynamicUpdates();
        }
    }

	public void finishUpdate()
	{
		// nothin.
	}

	@Override
	public IBitAccess getBitAccess()
	{
		VoxelBlob mask = VoxelBlob.NULL_BLOB;

		if ( world != null )
		{
			mask = new VoxelBlob();
		}

		return new BitAccess( world, pos, getBlob(), mask );
	}

}
