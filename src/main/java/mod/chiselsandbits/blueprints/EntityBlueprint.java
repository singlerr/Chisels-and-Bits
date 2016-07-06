package mod.chiselsandbits.blueprints;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityBlueprint extends Entity
{

	ItemStack item;

	int sizeX, sizeY, sizeZ;

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
			worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, item ) );
			setDead();
		}

		return false;
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
	protected void entityInit()
	{
		setEntityBoundingBox( getEntityBoundingBox() );
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox()
	{
		return new AxisAlignedBB( -0.5, -0.5, -0.5, 0.5, 0.5, 0.5 ).offset( posX, posY, posZ );
	}

	@Override
	protected void readEntityFromNBT(
			final NBTTagCompound tagCompund )
	{
		item = ItemStack.loadItemStackFromNBT( tagCompund.getCompoundTag( "item" ) );
		sizeX = tagCompund.getInteger( "sizeX" );
		sizeY = tagCompund.getInteger( "sizeY" );
		sizeZ = tagCompund.getInteger( "sizeZ" );
	}

	@Override
	protected void writeEntityToNBT(
			final NBTTagCompound tagCompound )
	{
		item.writeToNBT( tagCompound.getCompoundTag( "item" ) );
		tagCompound.setInteger( "sizeX", sizeX );
		tagCompound.setInteger( "sizeY", sizeY );
		tagCompound.setInteger( "sizeZ", sizeZ );
	}

}
