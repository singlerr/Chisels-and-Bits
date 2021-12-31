package mod.chiselsandbits.fabric.mixin.client.block.blockentity;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChiseledBlockEntity.class, remap = false)
public abstract class ChiseledBlockEntityMixin implements RenderAttachmentBlockEntity
{

    @Shadow @NotNull public abstract IBlockModelData getBlockModelData();

    @Override
    public @Nullable Object getRenderAttachmentData()
    {
        return getBlockModelData();
    }
}
