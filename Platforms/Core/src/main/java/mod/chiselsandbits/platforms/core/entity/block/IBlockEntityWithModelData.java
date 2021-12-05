package mod.chiselsandbits.platforms.core.entity.block;

import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a block entity which has its own model data.
 */
public interface IBlockEntityWithModelData
{
    @NotNull
    IBlockModelData getBlockModelData();
}
