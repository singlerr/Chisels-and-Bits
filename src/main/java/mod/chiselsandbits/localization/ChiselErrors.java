package mod.chiselsandbits.localization;

public enum ChiselErrors implements ILocalizeable
{
	OUT_OF_RANGE( "mod.chiselsandbits.result.out_of_range" ),
	NO_BAG_SPACE( "mod.chiselsandbits.result.no_bag_space" ),
	HAS_CHANGED( "mod.chiselsandbits.result.has_changed" ),
	NO_CHISELS( "mod.chiselsandbits.result.missing_chisels" ),
	NO_BITS( "mod.chiselsandbits.result.missing_bits" ),
	NOTHING_TO_UNDO( "mod.chiselsandbits.result.nothing_to_undo" ),
	NOTHING_TO_REDO( "mod.chiselsandbits.result.nothing_to_redo" );

	private final String msg;

	private ChiselErrors(
			String str )
	{
		msg = str;
	}

	@Override
	public String toString()
	{
		return msg;
	}

}
