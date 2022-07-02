package mod.chiselsandbits.fabric.mixin.platform.client.render.block;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ChiseledBlockModelUpdateMixin
{

    @Inject(
            method = "replaceWithPacketData",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onReplaceWithPacketData(
            final int i,
            final int j,
            final FriendlyByteBuf buf,
            final CompoundTag tag,
            final Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> tagOutputConsumer,
            final CallbackInfoReturnable<LevelChunk> ci,
            final int k,
            final LevelChunk levelChunk
    )
    {
        ChiseledBlockEntity.updateAllModelDataInChunk(levelChunk);
    }
}
