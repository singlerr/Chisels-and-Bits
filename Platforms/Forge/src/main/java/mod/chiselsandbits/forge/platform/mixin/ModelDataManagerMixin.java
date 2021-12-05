package mod.chiselsandbits.forge.platform.mixin;

import com.google.common.base.Preconditions;
import mod.chiselsandbits.forge.platform.client.model.data.ForgeBlockModelDataPlatformDelegate;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.entity.block.IBlockEntityWithModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ModelDataManager.class, remap = false)
public abstract class ModelDataManagerMixin
{
    @Redirect(
      method = "refreshModelData",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getModelData()Lnet/minecraftforge/client/model/data/IModelData;"
      )
    )
    private static IModelData getModelDataRetrieval(final BlockEntity blockEntity) {
        if (blockEntity instanceof IBlockEntityWithModelData blockEntityWithModelData) {
            final IBlockModelData blockModelData = blockEntityWithModelData.getBlockModelData();
            if (!(blockModelData instanceof ForgeBlockModelDataPlatformDelegate platformDelegate)) {
                throw new IllegalStateException("Block model data is not compatible with the current platform.");
            }

            return platformDelegate.getDelegate();
        }

        return blockEntity.getModelData();
    }
}
