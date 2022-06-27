package mod.chiselsandbits.forge;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.forge.platform.ForgeChiselsAndBitsPlatform;
import mod.chiselsandbits.forge.platform.block.ForgeChiseledBlock;
import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ForgeChiselsAndBits
{
	private static ChiselsAndBits instance;
    private static ForgeChiselsAndBitsPlatform platform;

	public ForgeChiselsAndBits()
	{
        platform = new ForgeChiselsAndBitsPlatform();
        IChiselsAndBitsPlatformCore.Holder.setInstance(platform);

	    instance = new ChiselsAndBits(ForgeChiseledBlock::new);
	}

    public static ChiselsAndBits getInstance()
    {
        return instance;
    }
}
