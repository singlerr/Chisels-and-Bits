package mod.chiselsandbits.blueprints;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.modes.WrenchModes;
import mod.chiselsandbits.network.packets.PacketAdjustBlueprint;
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
import net.minecraft.world.World;

public class EntityBlueprint extends Entity
{

	private static final DataParameter<Optional<ItemStack>> BLUEPRINT_ITEMSTACK = EntityDataManager.<Optional<ItemStack>> createKey( EntityBlueprint.class, DataSerializers.OPTIONAL_ITEM_STACK );

	private static final DataParameter<EnumFacing> BLUEPRINT_AXIS_X = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );
	private static final DataParameter<EnumFacing> BLUEPRINT_AXIS_Y = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );
	private static final DataParameter<EnumFacing> BLUEPRINT_AXIS_Z = EntityDataManager.<EnumFacing> createKey( EntityBlueprint.class, DataSerializers.FACING );

	private static final DataParameter<Integer> BLUEPRINT_MIN_X = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	private static final DataParameter<Integer> BLUEPRINT_MIN_Y = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	private static final DataParameter<Integer> BLUEPRINT_MIN_Z = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	private static final DataParameter<Integer> BLUEPRINT_MAX_X = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	private static final DataParameter<Integer> BLUEPRINT_MAX_Y = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );
	private static final DataParameter<Integer> BLUEPRINT_MAX_Z = EntityDataManager.<Integer> createKey( EntityBlueprint.class, DataSerializers.VARINT );

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
			final ItemStack handStack,
			final EnumHand hand )
	{
		final AxisAlignedBB box = getEntityBoundingBox();

		final Pair<Vec3d, Vec3d> ray = ModUtil.getPlayerRay( player );
		final RayTraceResult rtr = box.calculateIntercept( ray.getLeft(), ray.getRight() );

		if ( rtr == null )
		{
			return EnumActionResult.PASS;
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

		return EnumActionResult.PASS;
	}

	private void adjustSize(
			final EnumFacing xOld,
			final EnumFacing yOld,
			final EnumFacing zOld )
	{
		final EnumFacing x = getDataManager().get( BLUEPRINT_AXIS_X );
		final EnumFacing y = getDataManager().get( BLUEPRINT_AXIS_Y );
		final EnumFacing z = getDataManager().get( BLUEPRINT_AXIS_Z );

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
		if ( player.getEntityWorld().isRemote )
		{

			final BlueprintData data = ChiselsAndBits.getItems().itemBlueprint.getStackData( getItemStack() );
			if ( data.getState() == EnumLoadState.LOADED )
			{
				final BlockPos center = getPosition();

			}
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

	public void handlePacket(
			final PacketAdjustBlueprint packetAdjustBlueprint )
	{
		// TODO Auto-generated method stub

	}

}
