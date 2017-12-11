package mod.chiselsandbits.config;

public enum EnumPasteBinPrivacyMode
{
	PUBLIC( "0" ),
	UNLISTED( "1" ),
	PRIVATE( "2" );

	public final String value;

	private EnumPasteBinPrivacyMode(
			String v )
	{
		value = v;
	}
}
