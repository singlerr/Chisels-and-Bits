package mod.chiselsandbits.data.init;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GameInitializationManager
{
    private static final GameInitializationManager INSTANCE = new GameInitializationManager();

    public static GameInitializationManager getInstance()
    {
        return INSTANCE;
    }

    private boolean initialized = false;

    private GameInitializationManager()
    {
    }

    public void initialize(final ExistingFileHelper existingFileHelper) {
        if (initialized)
            return;

        initialized = true;

        GLFWInitializationManager.getInstance().initialize();
        MinecraftInstanceManager.getInstance().initialize(existingFileHelper);

        ModelLoadingHandler.loadAndBake();
    }
}
