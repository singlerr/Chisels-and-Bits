package mod.chiselsandbits.chiseledblock.properties;

import net.minecraftforge.common.property.IUnlistedProperty;

public final class UnlistedLightValue implements IUnlistedProperty<Integer>
{
	@Override
	public String getName()
	{
		return "lv";
	}

	@Override
	public boolean isValid(
			final Integer value )
	{
		return true;
	}

	@Override
	public Class<Integer> getType()
	{
		return Integer.class;
	}

	@Override
	public String valueToString(
			final Integer value )
	{
		return Float.toString( value );
	}
}