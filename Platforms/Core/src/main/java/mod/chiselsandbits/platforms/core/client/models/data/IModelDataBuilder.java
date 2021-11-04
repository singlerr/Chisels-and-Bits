package mod.chiselsandbits.platforms.core.client.models.data;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.client.IClientManager;

/**
 * Builder for a new model data instances.
 */
public interface IModelDataBuilder
{

    static IModelDataBuilder create() {
        return IClientManager.getInstance().createNewModelDataBuilder();
    }

    IModelData build();
}
