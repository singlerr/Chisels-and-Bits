package mod.chiselsandbits.forge.platform.mixin;

import mod.chiselsandbits.forge.platform.client.model.data.ForgeBlockModelDataPlatformDelegate;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.entity.block.IBlockEntityWithModelData;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@Mixin(value = IForgeBlockEntity.class, remap = false)
public interface IForgeBlockEntityMixin
{

    /**
     * Overrides the default model data logic.
     *
     * @return The default model data logic.
     * @author Chisels and Bits
     */
    @Nonnull
    @Overwrite()
    default public IModelData getModelData()
    {
        if (this instanceof IBlockEntityWithModelData blockEntityWithModelData) {
            final IBlockModelData blockModelData = blockEntityWithModelData.getBlockModelData();
            if (!(blockModelData instanceof ForgeBlockModelDataPlatformDelegate platformDelegate)) {
                throw new IllegalStateException("Block model data is not compatible with the current platform.");
            }

            return platformDelegate.getDelegate();
        }

        return EmptyModelData.INSTANCE;
    }
}
