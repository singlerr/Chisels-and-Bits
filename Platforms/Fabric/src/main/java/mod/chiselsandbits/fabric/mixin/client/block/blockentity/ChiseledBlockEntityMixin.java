package mod.chiselsandbits.fabric.mixin.client.block.blockentity;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.entity.block.IBlockEntityWithModelData;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChiseledBlockEntity.class, remap = false)
public abstract class ChiseledBlockEntityMixin implements RenderAttachmentBlockEntity, IBlockEntityWithModelData
{

    @Shadow
    @NotNull
    public abstract IBlockModelData getBlockModelData();

    @Override
    public @Nullable Object getRenderAttachmentData()
    {
        return getBlockModelData();
    }
}
