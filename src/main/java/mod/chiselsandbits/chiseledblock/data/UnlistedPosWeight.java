
package mod.chiselsandbits.chiseledblock.data;

import net.minecraftforge.common.property.IUnlistedProperty;


public final class UnlistedPosWeight implements IUnlistedProperty<Long>
{
	@Override
	public String getName()
	{
		return "l";
	}

	@Override
	public boolean isValid(
			final Long value )
	{
		return true;
	}

	@Override
	public Class<Long> getType()
	{
		return Long.class;
	}

	@Override
	public String valueToString(
			final Long value )
	{
		return Long.toString( value );
	}
}