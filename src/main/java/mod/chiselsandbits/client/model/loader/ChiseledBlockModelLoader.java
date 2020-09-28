package mod.chiselsandbits.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import mod.chiselsandbits.client.model.ChiseledBlockModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public final class ChiseledBlockModelLoader implements IModelLoader
{

    private static final ChiseledBlockModelLoader INSTANCE = new ChiseledBlockModelLoader();

    public static ChiseledBlockModelLoader getInstance()
    {
        return INSTANCE;
    }

    private ChiseledBlockModelLoader()
    {
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager)
    {
        ChiselsAndBits.getInstance().clearCache();
    }

    @Override
    public IModelGeometry read(final JsonDeserializationContext deserializationContext, final JsonObject modelContents)
    {
        return new ChiseledBlockModel();
    }
}
