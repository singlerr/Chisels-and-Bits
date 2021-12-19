package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class CreateModelUpdateHolder
{
    private       IBlockModelData                  modelData      = null;
    private final Collection<Consumer<IBlockModelData>> onSetConsumers = new ArrayList<>();

    public CreateModelUpdateHolder()
    {
    }

    public void setModelData(final IBlockModelData modelData)
    {
        this.modelData = modelData;
        this.onSetConsumers.forEach(consumer -> consumer.accept(this.modelData));
    }

    public void addConsumer(final Consumer<IBlockModelData> modelDataConsumer) {
        if (this.modelData != null)
            modelDataConsumer.accept(this.modelData);

        this.onSetConsumers.add(modelDataConsumer);
    }
}
