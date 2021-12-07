package mod.chiselsandbits.fabric.platform.client.rendering.model.loader;

import com.google.gson.*;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecification;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecificationLoader;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public final class FabricPlatformModelLoaderPlatformDelegate<L extends IModelSpecificationLoader<S>, S extends IModelSpecification<S>> implements ModelResourceProvider
{

    private final Gson gson;
    private final L delegate;

    public FabricPlatformModelLoaderPlatformDelegate(final L delegate)
    {
        this.delegate = delegate;
        this.gson = new GsonBuilder()
                      .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
                      .registerTypeHierarchyAdapter(IModelSpecification.class, (JsonDeserializer<IModelSpecification<?>>) (json, typeOfT, context) -> {
                          if (!json.isJsonObject())
                              throw new JsonParseException("Model specification must be an object");

                          return delegate.read(context, json.getAsJsonObject());
                      })
                      .disableHtmlEscaping()
                      .create();
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(
      final ResourceLocation resourceLocation, final ModelProviderContext modelProviderContext) throws ModelProviderException
    {
        try
        {
            final Resource resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            final InputStream inputStream = resource.getInputStream();
            final InputStreamReader streamReader = new InputStreamReader(inputStream);

            final IModelSpecification<?> modelSpecification = gson.fromJson(streamReader, IModelSpecification.class);

            streamReader.close();
            inputStream.close();
            resource.close();

            return new FabricModelSpecificationUnbakedModelDelegate<?>(modelSpecification);
        }
        catch (IOException e)
        {
            throw new ModelProviderException("Failed to find and read resource", e);
        }

        return ;
    }
}
