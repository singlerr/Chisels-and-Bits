package mod.chiselsandbits.blueprints;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.modes.WrenchModes;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketUndo;
import mod.chiselsandbits.voxelspace.IVoxelSrc;
import mod.chiselsandbits.voxelspace.VoxelCompressedProviderWorld;
import mod.chiselsandbits.voxelspace.VoxelOffsetRegion;
import mod.chiselsandbits.voxelspace.VoxelRegionSrc;
import mod.chiselsandbits.voxelspace.VoxelTransformedRegion;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class EntityBlueprint extends Entity
{

	private static final DataParameter<Optional<ItemStack>> BLUEPRINT_ITEMSTACK = EntityDataManager.<Optional<ItemStack>> createKey( EntityBlueprint.class, DataSerializers.OPTIONAL_ITEM_STACK );

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

			worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, getItem().orNull() ) );
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

	@Override
	public EnumActionResult applyPlayerInteraction(
			final EntityPlayer player,
			final Vec3d vec,
			ItemStack handStack,
			final EnumHand hand )
	{
		final AxisAlignedBB box = getEntityBoundingBox();

		final Pair<Vec3d, Vec3d> ray = ModUtil.getPlayerRay( player );
		final RayTraceResult rtr = box.calculateIntercept( ray.getLeft(), ray.getRight() );

		if ( rtr == null )
		{
			return EnumActionResult.PASS;
		}

		if ( handStack == null )
		{
			handStack = player.getHeldItem( hand.MAIN_HAND );
		}

		int direction = player.isSneaking() ? 1 : -1;
		if ( getItem().isPresent() && getItem().get().hasTagCompound() )
		{
			// deployment

			if ( handStack == null )
			{
				beginPlacement( player );
			}
			else if ( handStack.getItem() == ChiselsAndBits.getItems().itemWrench )
			{
				if ( getDataManager().get( BLUEPRINT_PLACING ) == true )
				{
					player.addChatComponentMessage( new TextComponentTranslation( LocalStrings.BlueprintCannotMove.toString() ) );
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
			}
		}
		else
		{
			// capture

			if ( handStack == null )
			{
				beginCapture( player );
			}
			else if ( handStack.getItem() == ChiselsAndBits.getItems().itemWrench )
			{
				if ( getDataManager().get( BLUEPRINT_PLACING ) == true )
				{
					player.addChatComponentMessage( new TextComponentTranslation( LocalStrings.BlueprintCannotMove.toString() ) );
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
			}
		}

		return EnumActionResult.SUCCESS;
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

		}
	}

	private void beginPlacement(
			final EntityPlayer player )
	{
		if ( getDataManager().get( BLUEPRINT_PLACING ) == false )
		{
			getDataManager().set( BLUEPRINT_PLACING, true );
			player.addChatComponentMessage( new TextComponentTranslation( LocalStrings.BlueprintBeginPlace.toString() ) );
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
					for ( final Entry<BlockPos, QueuedChanges> box : calculatedSpace.entrySet() )
					{
						final QueuedChanges qc = box.getValue();

						if ( qc.complete )
						{
							continue;
						}

						if ( qc.lastAttempt == 0 || now - qc.lastAttempt > 1500 )
						{
							qc.lastAttempt = now;

							box.getKey();
							final VoxelBlob a = VoxelBlob.getBlobAt( source, qc.srcOffset );
							final VoxelBlob b = VoxelBlob.getBlobAt( application, qc.srcOffset );

							if ( a.equals( b ) )
							{
								qc.complete = true;
							}
							else
							{
								final PacketUndo p = new PacketUndo( box.getKey(), new VoxelBlobStateReference( a, 0 ), new VoxelBlobStateReference( b, 0 ) );

								final ActingPlayer testPlayer = ActingPlayer.testingAs( player, EnumHand.MAIN_HAND );
								final boolean result = p.preformAction( testPlayer, false );

								if ( result )
								{
									final ActingPlayer actingPlayer = ActingPlayer.actingAs( player, EnumHand.MAIN_HAND );
									if ( p.preformAction( actingPlayer, true ) )
									{
										NetworkRouter.instance.sendToServer( p );
										qc.complete = true;
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

	private void calculateSpace(
			final BlueprintData bd )
	{
		calculatedSpace = new HashMap<BlockPos, QueuedChanges>();

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

		source = new VoxelRegionSrc( new VoxelCompressedProviderWorld( worldObj ), min, max, min );

		final IVoxelSrc data = new VoxelRegionSrc( bd, BlockPos.ORIGIN, new BlockPos( bd.getXSize(), bd.getYSize(), bd.getZSize() ), BlockPos.ORIGIN );
		final IVoxelSrc offset = new VoxelTransformedRegion( data, axisX, axisY, axisZ, afterOffset );
		application = new VoxelOffsetRegion( offset, bitOffset );

		for ( final BlockPos p : BlockPos.getAllInBox( min, max ) )
		{
			calculatedSpace.put( p.toImmutable(), new QueuedChanges( p.subtract( min ) ) );
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
		getDataManager().set( param, getDataManager().get( param ) + direction );
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
		return getBox().offset( posX, posY, posZ );
	}

	@Override
	protected void readEntityFromNBT(
			final NBTTagCompound tagCompund )
	{
		setItemStack( ItemStack.loadItemStackFromNBT( tagCompund.getCompoundTag( "item" ) ) );
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
		if ( getItem().isPresent() )
		{
			getItem().get().writeToNBT( itemNBT );
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
		return getDataManager().get( BLUEPRINT_ITEMSTACK ).orNull();
	}

	public void setItemStack(
			final ItemStack copy )
	{
		getDataManager().set( BLUEPRINT_ITEMSTACK, Optional.fromNullable( copy ) );
	}

	public Optional<ItemStack> getItem()
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

}
