package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;

public class ModRegistry
{

	public static final String unlocalizedPrefix = "mod." + ChiselsAndBits.MODID + ".";

	static ModCreativeTab creativeTab = new ModCreativeTab();
	static CreativeClipboardTab creativeClipboard = null;

	static
	{
		if ( ChiselsAndBits.getConfig().creativeClipboardSize > 0 )
		{
			creativeClipboard = new CreativeClipboardTab();
		}
	}

}
