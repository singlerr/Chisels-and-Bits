package mod.chiselsandbits;

import mod.chiselsandbits.api.ChiselsAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LanguageHandler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.registrars.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private final Configuration configuration;

	public ChiselsAndBits()
	{
	    instance = this;

        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        this.configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
        IChiselsAndBitsAPI.Holder.setInstance(ChiselsAndBitsAPI.getInstance());

        ModBlocks.onModConstruction();
        ModItems.onModConstruction();
        ModTileEntityTypes.onModConstruction();
        ModChiselModes.onModConstruction();
        ModContainerTypes.onModConstruction();
        ModRecipeSerializers.onModConstruction();
        ModModificationOperation.onModConstruction();
        ModMetadataKeys.onModConstruction();

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
