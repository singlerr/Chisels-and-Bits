package mod.chiselsandbits.blueprints;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityBlueprint extends Entity
{

	private static final DataParameter<Optional<ItemStack>> BLUEPRINT_ITEMSTACK = EntityDataManager.<Optional<ItemStack>> createKey( EntityBlueprint.class, DataSerializers.OPTIONAL_ITEM_STACK );

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

		final int direction = player.isSneaking() ? 1 : -1;
		if ( getItem().isPresent() && getItem().get().hasTagCompound() )
		{
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
		else
		{
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

		return EnumActionResult.PASS;
	}

	private void slide(
			final DataParameter<Integer> paramA,
			final DataParameter<Integer> paramB,
			final int direction )
	{
		final int a = getDataManager().get( paramA ) + direction;
		final int b = getDataManager().get( paramB ) - direction;

		if ( a >= 0 && b >= 0 )
		{
			getDataManager().set( paramA, a );
			getDataManager().set( paramB, b );
		}
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
		setEntityBoundingBox( getEntityBoundingBox() );
	}

	public AxisAlignedBB getBox()
	{
		final double minX = getDataManager().get( BLUEPRINT_MIN_X );
		final double maxX = getDataManager().get( BLUEPRINT_MAX_X );
		final double minY = getDataManager().get( BLUEPRINT_MIN_Y );
		final double maxY = getDataManager().get( BLUEPRINT_MAX_Y );
		final double minZ = getDataManager().get( BLUEPRINT_MIN_Z );
		final double maxZ = getDataManager().get( BLUEPRINT_MAX_Z );

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
		getDataManager().set( BLUEPRINT_MIN_X, lowX );
		getDataManager().set( BLUEPRINT_MAX_X, x - lowX );
		getDataManager().set( BLUEPRINT_MIN_Z, lowZ );
		getDataManager().set( BLUEPRINT_MAX_Z, z - lowZ );

		// place it on the floor...
		getDataManager().set( BLUEPRINT_MIN_Y, 0 );
		getDataManager().set( BLUEPRINT_MAX_Y, y );
	}

}
