
package mod.chiselsandbits.chiseledblock.data;

import net.minecraftforge.common.property.IUnlistedProperty;

public final class UnlistedBlockFlags implements IUnlistedProperty<Integer>
{
	final String name;

	public UnlistedBlockFlags(
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
		return Integer.toString( value );
	}
}