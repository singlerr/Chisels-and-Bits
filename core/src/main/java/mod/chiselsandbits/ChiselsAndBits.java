package mod.chiselsandbits;

import mod.chiselsandbits.apiipml.ChiselsAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.block.IBlockConstructionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityOptions;
import mod.chiselsandbits.api.config.IChiselsAndBitsConfiguration;
import mod.chiselsandbits.api.inventory.bit.IAdaptingBitInventoryManager;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.config.ChiselsAndBitsConfiguration;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.plugin.PluginManger;
import mod.chiselsandbits.registrars.*;
import mod.chiselsandbits.utils.LanguageHandler;

public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private final IChiselsAndBitsConfiguration configuration;

	public ChiselsAndBits(
            IEligibilityOptions eligibilityOptions,
            IAdaptingBitInventoryManager adaptingBitInventoryManager,
            IPluginDiscoverer pluginDiscoverer,
            IBlockConstructionManager blockConstructionManager)
	{
	    instance = this;
        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        this.configuration = new ChiselsAndBitsConfiguration();
        IChiselsAndBitsAPI.Holder.setInstance(new ChiselsAndBitsAPI(
                eligibilityOptions,
                adaptingBitInventoryManager,
                pluginDiscoverer,
                blockConstructionManager
        ));

        ModBlockEntityTypes.onModConstruction();
        ModBlocks.onModConstruction();
        ModChiselModeGroups.onModConstruction();
        ModChiselModes.onModConstruction();
        ModContainerTypes.onModConstruction();
        ModCreativeTabs.onModConstruction();
        ModItems.onModConstruction();
        ModMetadataKeys.onModConstruction();
        ModModelProperties.onModConstruction();
        ModModificationOperation.onModConstruction();
        ModModificationOperationGroups.onModConstruction();
        ModPatternPlacementTypes.onModConstruction();
        ModRecipeSerializers.onModConstruction();
        ModTags.onModConstruction();
        ModRecipeTypes.onModConstruction();

        ModEventHandler.onModConstruction();

        networkChannel.registerCommonMessages();

        PluginManger.getInstance().detect();
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onConstruction);
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

    public void onInitialize() {
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onInitialize);
    }
}
