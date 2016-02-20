package mod.chiselsandbits.chiseledblock.properties;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedBlockNormalCube implements IUnlistedProperty<Boolean>
{
	final String name;

	public UnlistedBlockNormalCube(
			final String s )
	{
		name = s;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isValid(
			final Boolean value )
	{
		return true;
	}

	@Override
	public Class<Boolean> getType()
	{
		return Boolean.class;
	}

	@Override
	public String valueToString(
			final Boolean value )
	{
		return Boolean.toString( value );
	}
}