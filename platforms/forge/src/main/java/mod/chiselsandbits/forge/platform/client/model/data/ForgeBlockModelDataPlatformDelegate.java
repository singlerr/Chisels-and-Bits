package mod.chiselsandbits.forge.platform.client.model.data;

import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataKey;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class ForgeBlockModelDataPlatformDelegate implements IBlockModelData
{

    private final net.minecraftforge.client.model.data.IModelData delegate;

    public ForgeBlockModelDataPlatformDelegate(final net.minecraftforge.client.model.data.IModelData delegate) {this.delegate = delegate;}

    @Override
    public boolean hasProperty(final IModelDataKey<?> prop)
    {
        if (!(prop instanceof ForgeModelPropertyPlatformDelegate))
            throw new IllegalArgumentException("The given key is not a Forge platform compatible model data key.");

        final ModelProperty<?> property = ((ForgeModelPropertyPlatformDelegate<?>) prop).getProperty();

        return delegate.hasProperty(property);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getData(final IModelDataKey<T> prop)
    {
        if (!(prop instanceof ForgeModelPropertyPlatformDelegate))
            throw new IllegalArgumentException("The given key is not a Forge platform compatible model data key.");

        final ModelProperty<?> property = ((ForgeModelPropertyPlatformDelegate<?>) prop).getProperty();

        return (T) delegate.getData(property);
    }

    @Override
    public <T> @Nullable T setData(final IModelDataKey<T> prop, final T data)
    {
        if (!(prop instanceof ForgeModelPropertyPlatformDelegate))
            throw new IllegalArgumentException("The given key is not a Forge platform compatible model data key.");

        final ModelProperty<T> property = ((ForgeModelPropertyPlatformDelegate<T>) prop).getProperty();

        return (T) delegate.setData(property, data);
    }

    public IModelData getDelegate()
    {
        return delegate;
    }
}
