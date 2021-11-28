package mod.chiselsandbits.forge;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.forge.platform.ForgeChiselsAndBitsPlatform;
import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.utils.LanguageHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;

@Mod(Constants.MOD_ID)
public class ForgeChiselsAndBits
{
	private static ChiselsAndBits instance;
    private static ForgeChiselsAndBitsPlatform platform;

	public ForgeChiselsAndBits()
	{
        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        platform = new ForgeChiselsAndBitsPlatform();
        IChiselsAndBitsPlatformCore.Holder.setInstance(platform);

	    instance = new ChiselsAndBits();
	}

    public static ChiselsAndBits getInstance()
    {
        return instance;
    }
}
