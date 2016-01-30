package mod.chiselsandbits.bitbag;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;

public class BagCapabilityProvider extends BagStorage implements ICapabilitySerializable<NBTTagCompound>
{

	public BagCapabilityProvider(
			final ItemStack stack,
			final NBTTagCompound nbt )
	{
		// migration.
		if ( stack.hasTagCompound() )
		{
			final NBTTagCompound compound = stack.getTagCompound();

			// port old implementation into capability.
			if ( compound.hasKey( "contents" ) )
			{
				deserializeNBT( compound );
				compound.removeTag( "contents" );
			}
		}
	}

	@Override
	public boolean hasCapability(
			final Capability<?> capability,
			final EnumFacing facing )
	{
		if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return true;
		}

		return false;
	}

	@Override
	public <T> T getCapability(
			final Capability<T> capability,
			final EnumFacing facing )
	{
		if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return (T) this;
		}

		return null;
	}

}
