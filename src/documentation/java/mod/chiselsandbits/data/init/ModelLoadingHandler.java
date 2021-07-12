package mod.chiselsandbits.data.init;

import net.minecraft.client.Minecraft;

public class ModelLoadingHandler
{
    static void loadAndBake()
    {
        final ExtendedModelManager extendedModelManager = (ExtendedModelManager) Minecraft.getInstance().getModelManager();
        extendedModelManager.loadModels();
    }
}