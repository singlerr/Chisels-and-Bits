package mod.chiselsandbits.helpers;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.core.ChiselsAndBits;

public class BitName
{
	private int state;

	public BitName(
			int state )
	{
		this.state = state;
	}

	@Override
	public boolean equals(
			Object obj )
	{
		if ( obj instanceof BitName )
		{
			return this.state == ( (BitName) obj ).state;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return state;
	}

	@Override
	public String toString()
	{
		try
		{
			return ChiselsAndBits.getApi().getBitItem( ModUtil.getStateById( state ) ).getDisplayName();
		}
		catch ( InvalidBitItem e )
		{
			return "No Such Bit.";
		}
	}
}
