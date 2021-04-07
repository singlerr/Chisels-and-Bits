package mod.chiselsandbits.core;

import mod.chiselsandbits.api.util.LanguageHandler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModChiselModes;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModTileEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	public ChiselsAndBits()
	{
	    instance = this;

        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::onModelRegistry));

        ModBlocks.onModConstruction();
        ModItems.onModConstruction();
        ModTileEntityTypes.onModConstruction();
        ModChiselModes.onModConstruction();

        networkChannel.registerCommonMessages();
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}


	public static NetworkChannel getNetworkChannel() {
	    return instance.networkChannel;
    }

}
