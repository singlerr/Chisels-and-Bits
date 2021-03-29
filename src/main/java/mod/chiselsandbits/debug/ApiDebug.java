package mod.chiselsandbits.debug;

import mod.chiselsandbits.api.addons.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.addons.IChiselsAndBitsAddon;

@ChiselsAndBitsAddon
public class ApiDebug implements IChiselsAndBitsAddon
{

	@Override
	public void commonSetup(
			final IChiselAndBitsAPI api )
	{
		DebugAction.api = api;
	}

}
