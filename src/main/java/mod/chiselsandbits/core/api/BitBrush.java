package mod.chiselsandbits.core.api;

import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;

public class BitBrush implements IBitBrush
{

	protected final int stateID;

	public BitBrush(
			final int BitBrush )
	{
		stateID = BitBrush;
	}

	@Override
	public ItemStack getItemStack(
			final int count )
	{
		if ( stateID == 0 )
		{
			return null;
		}

		return ItemChiseledBit.createStack( stateID, count, true );
	}

	@Override
	public boolean isAir()
	{
		return stateID == 0;
	}

}
