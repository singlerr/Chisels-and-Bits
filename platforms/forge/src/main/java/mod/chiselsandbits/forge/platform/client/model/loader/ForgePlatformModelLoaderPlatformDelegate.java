package mod.chiselsandbits.forge.platform.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecification;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecificationLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.jetbrains.annotations.NotNull;

public final class ForgePlatformModelLoaderPlatformDelegate<L extends IModelSpecificationLoader<T>, T extends IModelSpecification<T>>
  implements IModelLoader<ForgeModelGeometryToSpecificationPlatformDelegator<T>>
{

    private final L delegate;

    public ForgePlatformModelLoaderPlatformDelegate(final L delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public @NotNull ForgeModelGeometryToSpecificationPlatformDelegator<T> read(final @NotNull JsonDeserializationContext deserializationContext, final @NotNull JsonObject modelContents)
    {
        return new ForgeModelGeometryToSpecificationPlatformDelegator<>(delegate.read(deserializationContext, modelContents));
    }

    @Override
    public void onResourceManagerReload(final @NotNull ResourceManager p_10758_)
    {
        delegate.onResourceManagerReload(p_10758_);
    }
}
