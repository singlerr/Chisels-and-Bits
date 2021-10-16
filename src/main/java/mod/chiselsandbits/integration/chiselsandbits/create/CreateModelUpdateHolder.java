package mod.chiselsandbits.integration.chiselsandbits.create;

import net.minecraftforge.client.model.data.IModelData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class CreateModelUpdateHolder
{
    private IModelData modelData = null;
    private final Collection<Consumer<IModelData>> onSetConsumers = new ArrayList<>();

    public CreateModelUpdateHolder()
    {
    }

    public void setModelData(final IModelData modelData)
    {
        this.modelData = modelData;
        this.onSetConsumers.forEach(consumer -> consumer.accept(this.modelData));
    }

    public void addConsumer(final Consumer<IModelData> modelDataConsumer) {
        if (this.modelData != null)
            modelDataConsumer.accept(this.modelData);

        this.onSetConsumers.add(modelDataConsumer);
    }
}
