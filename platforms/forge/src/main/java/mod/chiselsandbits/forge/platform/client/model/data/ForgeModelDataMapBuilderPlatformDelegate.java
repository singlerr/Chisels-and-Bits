package mod.chiselsandbits.forge.platform.client.model.data;

import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataBuilder;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataKey;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class ForgeModelDataMapBuilderPlatformDelegate implements IModelDataBuilder
{

    private final ModelDataMap.Builder delegate;

    public ForgeModelDataMapBuilderPlatformDelegate() {
        delegate = new ModelDataMap.Builder();
    }

    @Override
    public IBlockModelData build()
    {
        return new ForgeBlockModelDataPlatformDelegate(delegate.build());
    }

    @Override
    public <T> IModelDataBuilder withInitial(final IModelDataKey<T> key, final T value)
    {
        if (!(key instanceof ForgeModelPropertyPlatformDelegate))
            throw new IllegalArgumentException("The given key is not a Forge platform compatible model data key.");

        final ModelProperty<T> property = ((ForgeModelPropertyPlatformDelegate<T>) key).getProperty();
        delegate.withInitial(property, value);

        return this;
    }
}
