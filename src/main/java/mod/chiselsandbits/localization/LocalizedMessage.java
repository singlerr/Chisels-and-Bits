package mod.chiselsandbits.localization;

import java.util.Arrays;

public class LocalizedMessage
{
	private ILocalizeable msg;
	private Object[] args;

	public LocalizedMessage(
			final ILocalizeable m,
			final Object... args )
	{
		this.msg = m;
		this.args = args;
	}

	@Override
	public int hashCode()
	{
		return msg.hashCode() ^ Arrays.hashCode( args );
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		LocalizedMessage a = (LocalizedMessage) obj;

		if ( a.msg == this.msg )
		{
			return Arrays.equals( args, a.args );
		}

		return false;
	}

	@Override
	public String toString()
	{
		return msg.getLocal( args );
	}

	public static LocalizedMessage PreLocalized(
			final String localizedMessage )
	{
		return new LocalizedMessage( new ILocalizeable() {

			@Override
			public String getLocal()
			{
				return localizedMessage;
			}

			@Override
			public String getLocal(
					Object... args )
			{
				return localizedMessage;
			}

		} );
	}
};
