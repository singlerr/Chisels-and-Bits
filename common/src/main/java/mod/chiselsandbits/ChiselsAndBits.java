package mod.chiselsandbits;

import mod.chiselsandbits.api.ChiselsAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.config.IChiselsAndBitsConfiguration;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.config.ChiselsAndBitsConfiguration;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.plugin.PluginManger;
import mod.chiselsandbits.registrars.*;
import mod.chiselsandbits.utils.LanguageHandler;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ChiselsAndBits
{
	private static ChiselsAndBits     instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private final IChiselsAndBitsConfiguration configuration;

	public ChiselsAndBits(Function<BlockBehaviour.Properties, ChiseledBlock> chiseledBlockFactory)
	{
	    instance = this;
        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json");

        this.configuration = new ChiselsAndBitsConfiguration();
        IChiselsAndBitsAPI.Holder.setInstance(ChiselsAndBitsAPI.getInstance());

        ModBlockEntityTypes.onModConstruction();
        ModBlocks.onModConstruction(chiseledBlockFactory);
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

    public void onCommonConstruction() {
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onCommonSetup);
    }
}
