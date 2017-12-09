package mod.chiselsandbits.localization;

import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.ModUtil;

public interface ILocalizeable
{

	public default String getLocal()
	{
		return DeprecationHelper.translateToLocal( toString() );
	}

	public default String getLocal(
			Object... args )
	{
		return ModUtil.localizeAndInsertVars( toString(), args );
	}

}
