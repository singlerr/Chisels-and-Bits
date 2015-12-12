
package mod.chiselsandbits.chiseledblock.data;

import net.minecraftforge.common.property.IUnlistedProperty;

public final class UnlistedLightOpacity implements IUnlistedProperty<Float>
{
	@Override
	public String getName()
	{
		return "l";
	}

	@Override
	public boolean isValid(
			final Float value )
	{
		return true;
	}

	@Override
	public Class<Float> getType()
	{
		return Float.class;
	}

	@Override
	public String valueToString(
			final Float value )
	{
		return Float.toString( value );
	}
}