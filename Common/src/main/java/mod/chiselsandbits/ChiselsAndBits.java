package mod.chiselsandbits;

import mod.chiselsandbits.api.ChiselsAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.config.IChiselsAndBitsConfiguration;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.config.ChiselsAndBitsConfiguration;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.registrars.*;

public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private final IChiselsAndBitsConfiguration configuration;

	public ChiselsAndBits()
	{
	    instance = this;

        this.configuration = new ChiselsAndBitsConfiguration();
        IChiselsAndBitsAPI.Holder.setInstance(ChiselsAndBitsAPI.getInstance());

        ModBlockEntityTypes.onModConstruction();
        ModBlocks.onModConstruction();
        ModChiselModeGroups.onModConstruction();
        ModChiselModes.onModConstruction();
        ModContainerTypes.onModConstruction();
        ModItemGroups.onModConstruction();
        ModItems.onModConstruction();
        ModMetadataKeys.onModConstruction();
        ModModelProperties.onModConstruction();
        ModModificationOperation.onModConstruction();
        ModModificationOperationGroups.onModConstruction();
        ModPatternPlacementTypes.onModConstruction();
        ModRecipeSerializers.onModConstruction();
        ModRecipeTypes.onModConstruction();
        ModTags.onModConstruction();

        networkChannel.registerCommonMessages();
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}

    public IChiselsAndBitsConfiguration getConfiguration()
    {
        return configuration;
    }

    public NetworkChannel getNetworkChannel() {
	    return networkChannel;
    }
}
