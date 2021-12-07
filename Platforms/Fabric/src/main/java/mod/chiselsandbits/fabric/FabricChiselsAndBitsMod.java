package mod.chiselsandbits.fabric;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.fabric.platform.FabricChiselsAndBitsPlatform;
import mod.chiselsandbits.fabric.platform.server.FabricServerLifecycleManager;
import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricChiselsAndBitsMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

    private IChiselsAndBitsPlatformCore platform;
    private ChiselsAndBits instance;

	@Override
	public void onInitialize() {
        platform = FabricChiselsAndBitsPlatform.getInstance();
        IChiselsAndBitsPlatformCore.Holder.setInstance(platform);

        instance = new ChiselsAndBits();
	}
}
