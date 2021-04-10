package mod.chiselsandbits;

import mod.chiselsandbits.api.ChiselsAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LanguageHandler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.core.ChiselsAndBitsClient;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.registrars.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

@Mod(Constants.MOD_ID)
public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private Configuration configuration = null;

	public ChiselsAndBits()
	{
	    instance = this;

        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        this.configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
        IChiselsAndBitsAPI.Holder.setInstance(ChiselsAndBitsAPI.getInstance());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::onModelRegistry));

        ModBlocks.onModConstruction();
        ModItems.onModConstruction();
        ModTileEntityTypes.onModConstruction();
        ModChiselModes.onModConstruction();
        ModTags.init();

        networkChannel.registerCommonMessages();
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public NetworkChannel getNetworkChannel() {
	    return networkChannel;
    }

}
