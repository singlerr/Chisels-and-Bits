package mod.chiselsandbits.chiseling;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.item.leftclick.LeftClickProcessingState;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.*;

public class CubedChiselMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{

    private final int bitsPerSide;
    private final boolean aligned;
    private final IFormattableTextComponent displayName;

    CubedChiselMode(final int bitsPerSide, final boolean aligned, final IFormattableTextComponent displayName) {
        this.bitsPerSide = bitsPerSide;
        this.aligned = aligned;
        this.displayName = displayName;
    }

    @Override
    public LeftClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
            return LeftClickProcessingState.DEFAULT;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getHitVec();

        Vector3d alignmentOffset = Vector3d.ZERO;
        if (aligned) {
            final Vector3d inBlockOffset = alignmentOffset.subtract(Vector3d.copy(blockRayTraceResult.getPos()));
            final BlockPos bitsInBlockOffset = new BlockPos(inBlockOffset.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));
            final BlockPos bitsRemainder = new BlockPos(
              bitsInBlockOffset.getX() % bitsPerSide,
              bitsInBlockOffset.getY() % bitsPerSide,
              bitsInBlockOffset.getZ() % bitsPerSide
            );

            final BlockPos targetedBitsInBlockOffset = bitsInBlockOffset.subtract(bitsRemainder);

            alignmentOffset = Vector3d.copy(targetedBitsInBlockOffset)
                .mul(-1, -1, -1)
                .mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
        }

        final Vector3d finalAlignmentOffset = alignmentOffset;
        BlockPosStreamProvider.getForRange(bitsPerSide)
          .forEach(bitPos -> context.include(
            hitVector
              .subtract(finalAlignmentOffset)
              .add(Vector3d.copy(bitPos).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT))
          ));

        return context.getMutator().map(mutator -> {
            context.setComplete();
            mutator.inWorldMutableStream()
              .forEach(IMutableStateEntryInfo::clear);

            return new LeftClickProcessingState(true, Event.Result.ALLOW);
        })
        .orElse(LeftClickProcessingState.DEFAULT);
    }

    @Override
    public void onStoppedLeftClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        //Noop.
    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.displayName;
    }


}
