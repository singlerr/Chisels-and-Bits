package mod.chiselsandbits.blueprints;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.localization.LocalStrings;
import mod.chiselsandbits.modes.WrenchModes;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketCompleteBlueprint;
import mod.chiselsandbits.network.packets.PacketShiftBluePrint;
import mod.chiselsandbits.network.packets.PacketUndo;
import mod.chiselsandbits.network.packets.WriteBlueprintPacket;
import mod.chiselsandbits.share.ShareGenerator;
import mod.chiselsandbits.share.output.ClipBoardText;
import mod.chiselsandbits.share.output.IShareOutput;
import mod.chiselsandbits.share.output.LocalPNGFile;
import mod.chiselsandbits.share.output.LocalTextFile;
import mod.chiselsandbits.voxelspace.IVoxelSrc;
import mod.chiselsandbits.voxelspace.VoxelCompressedProviderWorld;
import mod.chiselsandbits.voxelspace.VoxelOffsetRegion;
import mod.chiselsandbits.voxelspace.VoxelRegionSrc;
import mod.chiselsandbits.voxelspace.VoxelTransformedRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class EntityBlueprint extends Entity
{

	private static final DataParameter<ItemStack> BLUEPRINT_ITEMSTACK = EntityDataManager.<ItemStack> createKey( EntityBlueprint.class, DataSerializers.OPTIONAL_ITEM_STACK );

	private static final DataParameter<Boolean> BLUEPRINT_PLACING = EntityDataManager.<Boolean> createKey( EntityBlueprint.class, DataSerializers.BOOLEAN );

	static final DataParameter<EnumFacing> BLUEPRINT_AXIS_X = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );
	static final DataParameter<EnumFacing> BLUEPRINT_AXIS_Y = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );
	static final DataParameter<EnumFacing> BLUEPRINT_AXIS_Z = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );

	static final DataParameter<Integer> BLUEPRINT_MIN_X = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	static final DataParameter<Integer> BLUEPRINT_MIN_Y = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	static final DataParameter<Integer> BLUEPRINT_MIN_Z = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	static final DataParameter<Integer> BLUEPRINT_MAX_X = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	static final DataParameter<Integer> BLUEPRINT_MAX_Y = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	static final DataParameter<Integer> BLUEPRINT_MAX_Z = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );

	Object renderData = null;

	public EntityBlueprint(
			final World worldIn )
	{
		super( worldIn );
		this.setSize( 0.75F, 0.75F );
	}

	@Override
	public boolean hitByEntity(
			final Entity entityIn )
	{
		if ( !worldObj.isRemote )
		{
			if ( entityIn instanceof EntityPlayer )
			{
				if ( ( (EntityPlayer) entityIn ).capabilities.isCreativeMode )
				{
					setDead();
					return false;
				}
			}

			worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, getItem() ) );
			setDead();
		}

		return false;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public boolean isPushedByWater()
	{
		return false;
	}

	@Override
	public boolean handleWaterMovement()
	{
		return false;
	}

	public EnumActionResult hitWithTool(
			EntityPlayer player,
			ItemStack handStack )
	{
		final AxisAlignedBB box = getBlueprintBox();

		final Pair<Vec3d, Vec3d> ray = ModUtil.getPlayerRay( player );
		final RayTraceResult rtr = box.calculateIntercept( ray.getLeft(), ray.getRight() );

		int direction = player.isSneaking() ? 1 : -1;
		if ( !ModUtil.isEmpty( getItem() ) && getItem().hasTagCompound() )
		{
			// deployment

			if ( usingMallet( handStack ) )
			{
				beginPlacement( player );
				return EnumActionResult.SUCCESS;
			}
			else if ( handStack.getItem() == ChiselsAndBits.getItems().itemWrench )
			{
				if ( rtr == null )
				{
					return EnumActionResult.PASS;
				}

				if ( getDataManager().get( BLUEPRINT_PLACING ) == true )
				{
					player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintCannotMove.toString() ) );
					return EnumActionResult.FAIL;
				}

				final WrenchModes mode = WrenchModes.getMode( handStack );

				if ( mode == WrenchModes.ROTATE )
				{
					final EnumFacing side = rtr.sideHit;
					final Axis around = side.getAxis();

					final EnumFacing x = getDataManager().get( BLUEPRINT_AXIS_X );
					final EnumFacing y = getDataManager().get( BLUEPRINT_AXIS_Y );
					final EnumFacing z = getDataManager().get( BLUEPRINT_AXIS_Z );

					rotate( BLUEPRINT_AXIS_X, around );
					rotate( BLUEPRINT_AXIS_Y, around );
					rotate( BLUEPRINT_AXIS_Z, around );

					adjustSize( x, y, z );
				}
				else
				{
					if ( mode == WrenchModes.NUDGE_BLOCK )
					{
						direction *= 16;
					}

					switch ( rtr.sideHit )
					{
						case DOWN:
							slide( BLUEPRINT_MIN_Y, BLUEPRINT_MAX_Y, direction );
							break;
						case EAST:
							slide( BLUEPRINT_MAX_X, BLUEPRINT_MIN_X, direction );
							break;
						case NORTH:
							slide( BLUEPRINT_MIN_Z, BLUEPRINT_MAX_Z, direction );
							break;
						case SOUTH:
							slide( BLUEPRINT_MAX_Z, BLUEPRINT_MIN_Z, direction );
							break;
						case UP:
							slide( BLUEPRINT_MAX_Y, BLUEPRINT_MIN_Y, direction );
							break;
						case WEST:
							slide( BLUEPRINT_MIN_X, BLUEPRINT_MAX_X, direction );
							break;
						default:
							break;
					}
				}

				sendUpdate();
				return EnumActionResult.SUCCESS;
			}
		}
		else
		{
			// capture

			if ( usingMallet( handStack ) )
			{
				beginCapture( player );
				return EnumActionResult.SUCCESS;
			}
			else if ( handStack.getItem() == ChiselsAndBits.getItems().itemWrench )
			{
				if ( rtr == null )
				{
					return EnumActionResult.PASS;
				}

				if ( getDataManager().get( BLUEPRINT_PLACING ) == true )
				{
					if ( player.getEntityWorld().isRemote )
					{
						player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintCannotMove.toString() ) );
					}

					return EnumActionResult.FAIL;
				}

				final WrenchModes mode = WrenchModes.getMode( handStack );

				if ( mode == WrenchModes.ROTATE )
				{
					final EnumFacing side = rtr.sideHit;
					final Axis around = side.getAxis();

					final EnumFacing x = getDataManager().get( BLUEPRINT_AXIS_X );
					final EnumFacing y = getDataManager().get( BLUEPRINT_AXIS_Y );
					final EnumFacing z = getDataManager().get( BLUEPRINT_AXIS_Z );

					rotate( BLUEPRINT_AXIS_X, around );
					rotate( BLUEPRINT_AXIS_Y, around );
					rotate( BLUEPRINT_AXIS_Z, around );

					adjustSize( x, y, z );
				}
				else
				{
					if ( mode == WrenchModes.NUDGE_BLOCK )
					{
						direction *= 16;
					}
					else
					{
						if ( player.getEntityWorld().isRemote )
						{
							player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintBlockOnly.toString() ) );
						}

						return EnumActionResult.FAIL;
					}

					switch ( rtr.sideHit )
					{
						case DOWN:
							resize( BLUEPRINT_MIN_Y, direction );
							break;
						case EAST:
							resize( BLUEPRINT_MAX_X, direction );
							break;
						case NORTH:
							resize( BLUEPRINT_MIN_Z, direction );
							break;
						case SOUTH:
							resize( BLUEPRINT_MAX_Z, direction );
							break;
						case UP:
							resize( BLUEPRINT_MAX_Y, direction );
							break;
						case WEST:
							resize( BLUEPRINT_MIN_X, direction );
							break;
						default:
							break;
					}
				}

				sendUpdate();
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	private void sendUpdate()
	{
		if ( this.getEntityWorld().isRemote )
			NetworkRouter.instance.sendToServer( getConfiguration() );
	}

	private boolean usingMallet(
			ItemStack handStack )
	{
		return ModUtil.notEmpty( handStack ) && handStack.getItem() == ChiselsAndBits.getItems().itemMallet;
	}

	private void adjustSize(
			final EnumFacing xOld,
			final EnumFacing yOld,
			final EnumFacing zOld )
	{
		final EnumFacing x = getDataManager().get( BLUEPRINT_AXIS_X );
		final EnumFacing y = getDataManager().get( BLUEPRINT_AXIS_Y );
		final EnumFacing z = getDataManager().get( BLUEPRINT_AXIS_Z );

		final int minX = getDataManager().get( BLUEPRINT_MIN_X );
		final int maxX = getDataManager().get( BLUEPRINT_MAX_X );
		final int minY = getDataManager().get( BLUEPRINT_MIN_Y );
		final int maxY = getDataManager().get( BLUEPRINT_MAX_Y );
		final int minZ = getDataManager().get( BLUEPRINT_MIN_Z );
		final int maxZ = getDataManager().get( BLUEPRINT_MAX_Z );

		// move sizes around...
		swapSides( x, getSide( xOld, minX, minY, minZ ), getSide( xOld, maxX, maxY, maxZ ) );
		swapSides( y, getSide( yOld, minX, minY, minZ ), getSide( yOld, maxX, maxY, maxZ ) );
		swapSides( z, getSide( zOld, minX, minY, minZ ), getSide( zOld, maxX, maxY, maxZ ) );
	}

	private int getSide(
			final EnumFacing axis,
			final int X,
			final int Y,
			final int Z )
	{
		switch ( axis )
		{
			case DOWN:
				return Y;
			case EAST:
				return X;
			case NORTH:
				return Z;
			case SOUTH:
				return Z;
			case UP:
				return Y;
			case WEST:
				return X;
		}
		throw new NullPointerException();
	}

	private void swapSides(
			final EnumFacing newAxis,
			final int min,
			final int max )
	{
		switch ( newAxis )
		{
			case DOWN:
			case UP:
				getDataManager().set( BLUEPRINT_MIN_Y, min );
				getDataManager().set( BLUEPRINT_MAX_Y, max );
				return;
			case NORTH:
			case SOUTH:
				getDataManager().set( BLUEPRINT_MIN_Z, min );
				getDataManager().set( BLUEPRINT_MAX_Z, max );
				return;
			case EAST:
			case WEST:
				getDataManager().set( BLUEPRINT_MIN_X, min );
				getDataManager().set( BLUEPRINT_MAX_X, max );
				return;
		}
	}

	private void rotate(
			final DataParameter<EnumFacing> param,
			final Axis around )
	{
		getDataManager().set( param, getDataManager().get( param ).rotateAround( around ) );
	}

	private void beginCapture(
			final EntityPlayer player )
	{
		if ( player.getEntityWorld().isRemote )
		{
			if ( getDataManager().get( BLUEPRINT_PLACING ) )
				return;

			getDataManager().set( BLUEPRINT_PLACING, true );
			sendUpdate();
			player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintBeginCapture.toString() ) );

			final World clientWorld = Minecraft.getMinecraft().theWorld;

			IShareOutput out = null;

			try
			{
				switch ( ChiselsAndBits.getConfig().shareOutput )
				{
					case IMAGE_FILE_WITH_SCREENSHOT:
						out = new LocalPNGFile( newFileName( ChiselsAndBits.getConfig().getShareFileOutputFolder(), ".png" ) );
						break;
					default:
					case TEXT_CLIPBOARD:
						out = new ClipBoardText();
						break;
					case TEXT_FILE:
						out = new LocalTextFile( newFileName( ChiselsAndBits.getConfig().getShareFileOutputFolder(), ".txt" ) );
						break;
				}

				BlueprintPosition bpos = getBlueprintPos();

				final IShareOutput sout = out;
				final ShareGenerator sg = new ShareGenerator( clientWorld, bpos.min, bpos.max, out );

				sg.start( new Runnable() {

					@Override
					public void run()
					{
						try
						{
							WriteBlueprintPacket wp = new WriteBlueprintPacket();

							BlueprintData data = sout.getData();
							wp.setFrom( getEntityId(), data );

							NetworkRouter.instance.sendToServer( wp );
							player.addChatMessage( new TextComponentString( sg.message.toString() ) );
						}
						catch ( IOException ioe )
						{
							player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintFailedToWrite.toString() ) );
						}
					}

				}, null );
			}
			catch ( NoSuchFileException e )
			{
				player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintNoSuchPath.toString() ) );
			}
		}
	}

	private File newFileName(
			String shareFileOutputFolder,
			String type ) throws NoSuchFileException
	{
		DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss" );
		String extra = "";

		int loops = 0;
		while ( loops++ < 1000 )
		{
			File o = new File( shareFileOutputFolder, dateFormat.format( new Date() ) + extra + type );
			if ( !o.exists() )
				return o;

			extra = "" + loops;
		}

		throw new NoSuchFileException( shareFileOutputFolder );
	}

	int completed = 0;
	int passNumber = 1;

	private void beginPlacement(
			final EntityPlayer player )
	{
		if ( getDataManager().get( BLUEPRINT_PLACING ) == false )
		{
			getDataManager().set( BLUEPRINT_PLACING, true );
			sendUpdate();
			player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintBeginPlace.toString() ) );
		}

		if ( player.getEntityWorld().isRemote )
		{
			final BlueprintData data = ChiselsAndBits.getItems().itemBlueprint.getStackData( getItemStack() );
			if ( data.getState() == EnumLoadState.LOADED )
			{
				if ( calculatedSpace == null )
				{
					calculateSpace( data );
				}

				if ( calculatedSpace != null )
				{
					final long now = System.currentTimeMillis();
					if ( calculatedSpace.size() == completed )
					{
						PacketCompleteBlueprint p = new PacketCompleteBlueprint();
						p.EntityID = this.getEntityId();
						NetworkRouter.instance.sendToServer( p );
						player.addChatMessage( new TextComponentTranslation( LocalStrings.BlueprintFinished.toString() ) );
						return;
					}

					boolean matches = false;

					for ( final Entry<BlockPos, QueuedChanges> box : calculatedSpace.entrySet() )
					{
						final QueuedChanges qc = box.getValue();

						if ( qc.complete )
						{
							// server side!
							continue;
						}

						if ( qc.lastAttempt != passNumber )
						{
							qc.lastAttempt = passNumber;
							matches = true;

							box.getKey();
							final VoxelBlob a = VoxelBlob.getBlobAt( source, qc.srcOffset );
							final VoxelBlob b = VoxelBlob.getBlobAt( application, qc.srcOffset );

							if ( a.equals( b ) )
							{
								qc.complete = true;
								completed++;
							}
							else
							{
								final PacketUndo p = new PacketUndo( box.getKey(), new VoxelBlobStateReference( a, 0 ), new VoxelBlobStateReference( b, 0 ) );
								final ActingPlayer testPlayer = ActingPlayer.testingAs( player, EnumHand.MAIN_HAND );

								// we don't want to get a billion out of range
								// errors.
								if ( p.inRange( testPlayer, box.getKey() ) )
								{
									final boolean result = p.preformAction( testPlayer, false );

									if ( result )
									{
										final ActingPlayer actingPlayer = ActingPlayer.actingAs( player, EnumHand.MAIN_HAND );
										if ( p.preformAction( actingPlayer, true ) )
										{
											NetworkRouter.instance.sendToServer( p );
											qc.complete = true;
											completed++;
										}
									}
									else
									{
										testPlayer.displayError();
										break;
									}
								}
							}

							final long now2 = System.currentTimeMillis();
							if ( now2 - now > 64 )
							{
								break;
							}
						}
					}

					if ( !matches )
						passNumber++;
				}
			}
		}
	}

	private class QueuedChanges
	{

		public QueuedChanges(
				final BlockPos offset )
		{
			srcOffset = offset;
		}

		final BlockPos srcOffset;
		long lastAttempt = 0;
		boolean complete = false;
	};

	IVoxelSrc source;
	IVoxelSrc application;
	Map<BlockPos, QueuedChanges> calculatedSpace = null;

	BlueprintPosition getBlueprintPos()
	{
		final int bitsPerBlock = 16;
		final int bitsPerBlock_Minus1 = bitsPerBlock - 1;

		final int minX = ( getDataManager().get( BLUEPRINT_MIN_X ) + bitsPerBlock_Minus1 ) / bitsPerBlock;
		final int maxX = ( getDataManager().get( BLUEPRINT_MAX_X ) + bitsPerBlock_Minus1 ) / bitsPerBlock;
		final int minY = ( getDataManager().get( BLUEPRINT_MIN_Y ) + bitsPerBlock_Minus1 ) / bitsPerBlock;
		final int maxY = ( getDataManager().get( BLUEPRINT_MAX_Y ) + bitsPerBlock_Minus1 ) / bitsPerBlock;
		final int minZ = ( getDataManager().get( BLUEPRINT_MIN_Z ) + bitsPerBlock_Minus1 ) / bitsPerBlock;
		final int maxZ = ( getDataManager().get( BLUEPRINT_MAX_Z ) + bitsPerBlock_Minus1 ) / bitsPerBlock;

		final EnumFacing axisX = getDataManager().get( BLUEPRINT_AXIS_X );
		final EnumFacing axisY = getDataManager().get( BLUEPRINT_AXIS_Y );
		final EnumFacing axisZ = getDataManager().get( BLUEPRINT_AXIS_Z );

		BlockPos bitOffset = new BlockPos( getDataManager().get( BLUEPRINT_MIN_X ), getDataManager().get( BLUEPRINT_MIN_Y ), getDataManager().get( BLUEPRINT_MIN_Z ) );
		bitOffset = bitOffset.add( -minX * bitsPerBlock, -minY * bitsPerBlock, -minZ * bitsPerBlock );

		final int axis_x = 15 + getDataManager().get( BLUEPRINT_MIN_X ) + getDataManager().get( BLUEPRINT_MAX_X );
		final int axis_y = 15 + getDataManager().get( BLUEPRINT_MIN_Y ) + getDataManager().get( BLUEPRINT_MAX_Y );
		final int axis_z = 15 + getDataManager().get( BLUEPRINT_MIN_Z ) + getDataManager().get( BLUEPRINT_MAX_Z );

		BlockPos afterOffset = BlockPos.ORIGIN;
		afterOffset = adjustAxis( afterOffset, axisX, Axis.X, axis_x, axis_y, axis_z );
		afterOffset = adjustAxis( afterOffset, axisY, Axis.Y, axis_x, axis_y, axis_z );
		afterOffset = adjustAxis( afterOffset, axisZ, Axis.Z, axis_x, axis_y, axis_z );

		final BlockPos center = getPosition().down(); // this adds 0.5 to y
		final BlockPos min = center.add( -minX, -minY, -minZ );
		final BlockPos max = center.add( maxX, maxY, maxZ );

		return new BlueprintPosition( center, min, max, bitOffset, axisX, axisY, axisZ, afterOffset );
	}

	private void calculateSpace(
			final BlueprintData bd )
	{
		calculatedSpace = new HashMap<BlockPos, QueuedChanges>();

		BlueprintPosition bpos = getBlueprintPos();
		source = new VoxelRegionSrc( new VoxelCompressedProviderWorld( worldObj ), bpos.min, bpos.max, bpos.min );

		final IVoxelSrc data = new VoxelRegionSrc( bd, BlockPos.ORIGIN, new BlockPos( bd.getXSize(), bd.getYSize(), bd.getZSize() ), BlockPos.ORIGIN );
		final IVoxelSrc offset = new VoxelTransformedRegion( data, bpos.axisX, bpos.axisY, bpos.axisZ, bpos.afterOffset );
		application = new VoxelOffsetRegion( offset, bpos.bitOffset );

		for ( final BlockPos p : BlockPos.getAllInBox( bpos.min, bpos.max ) )
		{
			calculatedSpace.put( p.toImmutable(), new QueuedChanges( p.subtract( bpos.min ) ) );
		}
	}

	private void slide(
			final DataParameter<Integer> paramA,
			final DataParameter<Integer> paramB,
			final int direction )
	{
		int a = getDataManager().get( paramA ) + direction;
		int b = getDataManager().get( paramB ) - direction;

		if ( a < 0 )
		{
			b += a;
			a = 0;
		}

		if ( b < 0 )
		{
			a += b;
			b = 0;
		}

		getDataManager().set( paramA, a );
		getDataManager().set( paramB, b );
	}

	private void resize(
			final DataParameter<Integer> param,
			final int direction )
	{
		int newValue = getDataManager().get( param ) + direction;

		if ( newValue < 0 )
		{
			newValue = 0;
		}

		getDataManager().set( param, newValue );
	}

	@Override
	protected void entityInit()
	{
		getDataManager().register( BLUEPRINT_PLACING, false );
		getDataManager().register( BLUEPRINT_ITEMSTACK, null );
		getDataManager().register( BLUEPRINT_MIN_X, 0 );
		getDataManager().register( BLUEPRINT_MAX_X, 0 );
		getDataManager().register( BLUEPRINT_MIN_Y, 0 );
		getDataManager().register( BLUEPRINT_MAX_Y, 0 );
		getDataManager().register( BLUEPRINT_MIN_Z, 0 );
		getDataManager().register( BLUEPRINT_MAX_Z, 0 );
		getDataManager().register( BLUEPRINT_AXIS_X, EnumFacing.EAST );
		getDataManager().register( BLUEPRINT_AXIS_Y, EnumFacing.UP );
		getDataManager().register( BLUEPRINT_AXIS_Z, EnumFacing.SOUTH );
		setEntityBoundingBox( getEntityBoundingBox() );
	}

	public AxisAlignedBB getBox()
	{
		final double bitSize = 1.0 / 16.0;

		final double minX = bitSize * getDataManager().get( BLUEPRINT_MIN_X );
		final double maxX = bitSize * getDataManager().get( BLUEPRINT_MAX_X );
		final double minY = bitSize * getDataManager().get( BLUEPRINT_MIN_Y );
		final double maxY = bitSize * getDataManager().get( BLUEPRINT_MAX_Y );
		final double minZ = bitSize * getDataManager().get( BLUEPRINT_MIN_Z );
		final double maxZ = bitSize * getDataManager().get( BLUEPRINT_MAX_Z );

		return new AxisAlignedBB( -0.5 - minX, -0.5 - minY, -0.5 - minZ, 0.5 + maxX, 0.5 + maxY, 0.5 + maxZ );
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox()
	{
		return super.getEntityBoundingBox();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return getBlueprintBox();
	}

	public AxisAlignedBB getBlueprintBox()
	{
		return getBox().offset( posX, posY, posZ );
	}

	@Override
	protected void readEntityFromNBT(
			final NBTTagCompound tagCompund )
	{
		setItemStack( new ItemStack( tagCompund.getCompoundTag( "item" ) ) );
		getDataManager().set( BLUEPRINT_PLACING, tagCompund.getBoolean( "placing" ) );
		getDataManager().set( BLUEPRINT_MIN_X, tagCompund.getInteger( "minX" ) );
		getDataManager().set( BLUEPRINT_MAX_X, tagCompund.getInteger( "maxX" ) );
		getDataManager().set( BLUEPRINT_MIN_Y, tagCompund.getInteger( "minY" ) );
		getDataManager().set( BLUEPRINT_MAX_Y, tagCompund.getInteger( "maxY" ) );
		getDataManager().set( BLUEPRINT_MIN_Z, tagCompund.getInteger( "minZ" ) );
		getDataManager().set( BLUEPRINT_MAX_Z, tagCompund.getInteger( "maxZ" ) );
	}

	@Override
	protected void writeEntityToNBT(
			final NBTTagCompound tagCompound )
	{
		final NBTTagCompound itemNBT = new NBTTagCompound();
		if ( ModUtil.notEmpty( getItem() ) )
		{
			getItem().writeToNBT( itemNBT );
		}
		tagCompound.setTag( "item", itemNBT );
		tagCompound.setBoolean( "placing", getDataManager().get( BLUEPRINT_PLACING ) );
		tagCompound.setInteger( "minX", getDataManager().get( BLUEPRINT_MIN_X ) );
		tagCompound.setInteger( "maxX", getDataManager().get( BLUEPRINT_MAX_X ) );
		tagCompound.setInteger( "minY", getDataManager().get( BLUEPRINT_MIN_Y ) );
		tagCompound.setInteger( "maxY", getDataManager().get( BLUEPRINT_MAX_Y ) );
		tagCompound.setInteger( "minZ", getDataManager().get( BLUEPRINT_MIN_Z ) );
		tagCompound.setInteger( "maxZ", getDataManager().get( BLUEPRINT_MAX_Z ) );
	}

	public ItemStack getItemStack()
	{
		return getDataManager().get( BLUEPRINT_ITEMSTACK );
	}

	public void setItemStack(
			final ItemStack copy )
	{
		getDataManager().set( BLUEPRINT_ITEMSTACK, copy );
	}

	public ItemStack getItem()
	{
		return getDataManager().get( BLUEPRINT_ITEMSTACK );
	}

	float age = 0;

	@Override
	public void onUpdate()
	{
		++age;
	}

	public float getRotation()
	{
		return age;
	}

	public void setSize(
			int x,
			int y,
			int z )
	{
		// remove one, min size is 1
		--x;
		--y;
		--z;

		final int lowX = x / 2;
		final int lowZ = z / 2;

		// center it horizontally.
		getDataManager().set( BLUEPRINT_MIN_X, lowX * 16 );
		getDataManager().set( BLUEPRINT_MAX_X, ( x - lowX ) * 16 );
		getDataManager().set( BLUEPRINT_MIN_Z, lowZ * 16 );
		getDataManager().set( BLUEPRINT_MAX_Z, ( z - lowZ ) * 16 );

		// place it on the floor...
		getDataManager().set( BLUEPRINT_MIN_Y, 0 );
		getDataManager().set( BLUEPRINT_MAX_Y, y * 16 );
	}

	private BlockPos adjustAxis(
			final BlockPos offset,
			final EnumFacing axis,
			final Axis which,
			final int x,
			final int y,
			final int z )
	{
		switch ( axis )
		{
			case WEST:
				return offset.add( which == Axis.X ? x : 0, which == Axis.Y ? x : 0, which == Axis.Z ? x : 0 );
			case DOWN:
				return offset.add( which == Axis.X ? y : 0, which == Axis.Y ? y : 0, which == Axis.Z ? y : 0 );
			case NORTH:
				return offset.add( which == Axis.X ? z : 0, which == Axis.Y ? z : 0, which == Axis.Z ? z : 0 );
			default:
				break;
		}

		return offset;
	}

	public PacketShiftBluePrint getConfiguration()
	{
		PacketShiftBluePrint p = new PacketShiftBluePrint();

		p.EntityID = getEntityId();

		p.placing = getDataManager().get( BLUEPRINT_PLACING );

		p.x = getDataManager().get( BLUEPRINT_AXIS_X );
		p.y = getDataManager().get( BLUEPRINT_AXIS_Y );
		p.z = getDataManager().get( BLUEPRINT_AXIS_Z );

		p.min_x = getDataManager().get( BLUEPRINT_MIN_X );
		p.min_y = getDataManager().get( BLUEPRINT_MIN_Y );
		p.min_z = getDataManager().get( BLUEPRINT_MIN_Z );
		p.max_x = getDataManager().get( BLUEPRINT_MAX_X );
		p.max_y = getDataManager().get( BLUEPRINT_MAX_Y );
		p.max_z = getDataManager().get( BLUEPRINT_MAX_Z );

		return p;
	}

	public void setConfiguration(
			PacketShiftBluePrint p )
	{
		getDataManager().set( BLUEPRINT_PLACING, p.placing );

		getDataManager().set( BLUEPRINT_AXIS_X, p.x );
		getDataManager().set( BLUEPRINT_AXIS_Y, p.y );
		getDataManager().set( BLUEPRINT_AXIS_Z, p.z );

		getDataManager().set( BLUEPRINT_MIN_X, p.min_x );
		getDataManager().set( BLUEPRINT_MIN_Y, p.min_y );
		getDataManager().set( BLUEPRINT_MIN_Z, p.min_z );
		getDataManager().set( BLUEPRINT_MAX_X, p.max_x );
		getDataManager().set( BLUEPRINT_MAX_Y, p.max_y );
		getDataManager().set( BLUEPRINT_MAX_Z, p.max_z );
	}

	public void dropItem(
			NBTTagCompound tag )
	{
		ItemStack stack = getItem();
		stack.setTagCompound( tag );
		worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, stack ) );
		setDead();
	}

}
