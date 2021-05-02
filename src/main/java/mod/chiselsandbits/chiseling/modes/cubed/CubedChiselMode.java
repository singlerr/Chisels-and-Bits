package mod.chiselsandbits.chiseling.modes.cubed;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.registrars.ModChiselModeGroups;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.*;

public class CubedChiselMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final int                       bitsPerSide;
    private final boolean                   aligned;
    private final IFormattableTextComponent displayName;
    private final ResourceLocation          iconName;

    CubedChiselMode(final int bitsPerSide, final boolean aligned, final IFormattableTextComponent displayName, final ResourceLocation iconName)
    {
        this.bitsPerSide = bitsPerSide;
        this.aligned = aligned;
        this.displayName = displayName;
        this.iconName = iconName;
    }

    @Override
    public ClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.copy(face.getOpposite().getDirectionVec()),
          Function.identity()
        );

        if (context.isSimulation())
            return ClickProcessingState.DEFAULT;

        context.setComplete();
        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch())
              {
                  final Map<BlockState, Integer> resultingBitCount = Maps.newHashMap();

                  mutator.inWorldMutableStream()
                    .forEach(state -> {
                        final BlockState currentState = state.getState();
                        if (context.tryDamageItem()) {
                            resultingBitCount.putIfAbsent(currentState, 0);
                            resultingBitCount.computeIfPresent(currentState, (s, currentCount) -> currentCount + 1);

                            state.clear();
                        }
                    });

                  final IBitInventory inventory = IBitInventoryManager.getInstance()
                    .create(playerEntity);

                  resultingBitCount.forEach((blockState, count) -> BitInventoryUtils.insertIntoOrSpawn(
                    playerEntity,
                    inventory,
                    blockState,
                    count
                  ));

              }

              return new ClickProcessingState(true, Event.Result.ALLOW);
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedLeftClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        //Noop.
    }

    @SuppressWarnings("deprecation")
    @Override
    public ClickProcessingState onRightClickBy(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.copy(face.getDirectionVec()),
          facingVector -> aligned ? facingVector : facingVector.mul(1,-1, 1)
        );

        if (context.isSimulation())
            return ClickProcessingState.DEFAULT;

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
            final BlockState heldBlockState = ItemStackUtils.getHeldBitBlockStateFromPlayer(playerEntity);
            if (heldBlockState.isAir(new SingleBlockBlockReader(heldBlockState), BlockPos.ZERO))
                return ClickProcessingState.DEFAULT;

            final int missingBitCount = (int) mutator.stream()
              .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO))
              .count();

            final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

            context.setComplete();
            if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || playerEntity.isCreative()) {
                if (!playerEntity.isCreative())
                {
                    playerBitInventory.extract(heldBlockState, missingBitCount);
                }

                try (IBatchMutation ignored =
                       mutator.batch())
                {
                    mutator.inWorldMutableStream()
                      .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO))
                      .forEach(state -> state.overrideState(heldBlockState)); //We can use override state here to prevent the try-catch block.
                }
            }

            return new ClickProcessingState(true, Event.Result.ALLOW);
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedRightClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        //Noop.
    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    private Optional<ClickProcessingState> processRayTraceIntoContext(
      final PlayerEntity playerEntity,
      final IChiselingContext context,
      final Function<Direction, Vector3d> placementFacingAdapter,
      final Function<Vector3d, Vector3d> fullFacingVectorAdapter
    ) {
        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getHitVec().add(
          placementFacingAdapter.apply(blockRayTraceResult.getFace())
            .mul(SIZE_PER_HALF_BIT, SIZE_PER_HALF_BIT, SIZE_PER_HALF_BIT)
        );

        Vector3d alignmentOffset = Vector3d.ZERO;
        final Vector3d fullFacingVector = fullFacingVectorAdapter.apply(aligned ? new Vector3d(1, 1, 1) : Vector3d.copy(
          RayTracingUtils.getFullFacingVector(playerEntity)
        ));

        if (aligned)
        {
            final Vector3d inBlockOffset = hitVector.subtract(Vector3d.copy(new BlockPos(hitVector)));
            final BlockPos bitsInBlockOffset = new BlockPos(inBlockOffset.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

            final BlockPos targetedSectionIndices = new BlockPos(
              bitsInBlockOffset.getX() / bitsPerSide,
              bitsInBlockOffset.getY() / bitsPerSide,
              bitsInBlockOffset.getZ() / bitsPerSide
            );

            final BlockPos targetedStartPoint = new BlockPos(
              targetedSectionIndices.getX() * bitsPerSide,
              targetedSectionIndices.getY() * bitsPerSide,
              targetedSectionIndices.getZ() * bitsPerSide
            );

            final BlockPos targetedBitsInBlockOffset = bitsInBlockOffset.subtract(targetedStartPoint);

            alignmentOffset = Vector3d.copy(targetedBitsInBlockOffset)
                                .mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
        }

        final Vector3d finalAlignmentOffset = alignmentOffset.mul(fullFacingVector);
        BlockPosStreamProvider.getForRange(bitsPerSide)
          .forEach(bitPos -> context.include(
            hitVector
              .subtract(finalAlignmentOffset)
              .add(Vector3d.copy(bitPos).mul(fullFacingVector).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT))
          ));

        return Optional.empty();
    }

    @Override
    public ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.displayName;
    }

    @NotNull
    @Override
    public Optional<IToolModeGroup> getGroup()
    {
        return Optional.of(
          aligned ? ModChiselModeGroups.CUBED_ALIGNED : ModChiselModeGroups.CUBED
        );
    }
}
