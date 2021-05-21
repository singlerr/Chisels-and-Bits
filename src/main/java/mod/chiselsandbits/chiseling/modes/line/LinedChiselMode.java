package mod.chiselsandbits.chiseling.modes.line;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
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

@SuppressWarnings("deprecation")
public class LinedChiselMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final int                       bitsPerSide;
    private final IFormattableTextComponent displayName;
    private final ResourceLocation          iconName;

    LinedChiselMode(final int bitsPerSide, final IFormattableTextComponent displayName, final ResourceLocation iconName)
    {
        this.bitsPerSide = bitsPerSide;
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
          Direction::getOpposite,
          false
        );


        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch())
              {
                  context.setComplete();

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

                  resultingBitCount.forEach((blockState, count) -> BitInventoryUtils.insertIntoOrSpawn(
                    playerEntity,
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

    }

    @Override
    public ClickProcessingState onRightClickBy(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.copy(face.getDirectionVec()),
          Function.identity(),
          true
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              final BlockState heldBlockState = ItemStackUtils.getHeldBitBlockStateFromPlayer(playerEntity);
            if (heldBlockState.isAir(new SingleBlockBlockReader(heldBlockState), BlockPos.ZERO))
            {
                return ClickProcessingState.DEFAULT;
            }

              final int missingBitCount = (int) mutator.stream()
                                                  .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO))
                                                  .count();

              final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

              context.setComplete();
              if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || playerEntity.isCreative())
              {
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
      final Function<Direction, Direction> iterationAdaptor,
      final boolean airOnly
    )
    {
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

        final Vector3d hitBlockPosVector = Vector3d.copy(new BlockPos(hitVector));
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vector3d inBlockBitVector = inBlockHitVector.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE);

        final Direction iterationDirection = iterationAdaptor.apply(blockRayTraceResult.getFace());
        switch (iterationDirection)
        {
            case DOWN:
                includeDownAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
            case UP:
                includeUpAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
            case NORTH:
                includeNorthAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
            case SOUTH:
                includeSouthAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
            case WEST:
                includeWestAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
            case EAST:
                includeEastAxis(context, airOnly, hitBlockPosVector, inBlockBitVector);
                break;
        }

        return Optional.empty();
    }

    private void includeDownAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int yOff = 0; yOff < inBlockBitVector.getY(); yOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(0, yOff, 0);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }

        for (int xOff = -bitsPerSide / 2; xOff < bitsPerSide / 2; xOff++)
        {
            for (int zOff = -bitsPerSide / 2; zOff < bitsPerSide / 2; zOff++)
            {
                for (int yOff = 0; yOff < inBlockBitVector.getY(); yOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(xOff, yOff, zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private void includeUpAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int yOff = 0; yOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getY()); yOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(0, -yOff, 0);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }

        for (int xOff = -bitsPerSide / 2; xOff < bitsPerSide / 2; xOff++)
        {
            for (int zOff = -bitsPerSide / 2; zOff < bitsPerSide / 2; zOff++)
            {
                for (int yOff = 0; yOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getY()); yOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(xOff, -yOff, zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private void includeNorthAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int zOff = 0; zOff < inBlockBitVector.getZ(); zOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(0, 0, zOff);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }

        for (int xOff = -bitsPerSide / 2; xOff < bitsPerSide / 2; xOff++)
        {
            for (int yOff = -bitsPerSide / 2; yOff < bitsPerSide / 2; yOff++)
            {
                for (int zOff = 0; zOff < inBlockBitVector.getZ(); zOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(xOff, yOff, zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private void includeSouthAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int zOff = 0; zOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getZ()); zOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(0, 0, -zOff);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }

        for (int xOff = -bitsPerSide / 2; xOff < bitsPerSide / 2; xOff++)
        {
            for (int yOff = -bitsPerSide / 2; yOff < bitsPerSide / 2; yOff++)
            {
                for (int zOff = 0; zOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getZ()); zOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(xOff, yOff, -zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private void includeWestAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int xOff = 0; xOff < inBlockBitVector.getX(); xOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(xOff, 0, 0);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }


        for (int zOff = -bitsPerSide / 2; zOff < bitsPerSide / 2; zOff++)
        {
            for (int yOff = -bitsPerSide / 2; yOff < bitsPerSide / 2; yOff++)
            {
                for (int xOff = 0; xOff < inBlockBitVector.getX(); xOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(xOff, yOff, zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private void includeEastAxis(final IChiselingContext context, final boolean airOnly, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        if (bitsPerSide == 1)
        {
            for (int xOff = 0; xOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getX()); xOff++)
            {
                final Vector3d targetBit = inBlockBitVector.subtract(-xOff, 0, 0);
                final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                if (context.getInAreaTarget(inWorldTarget)
                      .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                      .orElse(airOnly)
                )
                {
                    context.include(inWorldTarget);
                }
                else
                {
                    return;
                }
            }

            return;
        }

        for (int zOff = -bitsPerSide / 2; zOff < bitsPerSide / 2; zOff++)
        {
            for (int yOff = -bitsPerSide / 2; yOff < bitsPerSide / 2; yOff++)
            {
                for (int xOff = 0; xOff < (BITS_PER_BLOCK_SIDE - inBlockBitVector.getX()); xOff++)
                {
                    final Vector3d targetBit = inBlockBitVector.subtract(-xOff, yOff, zOff);
                    final Vector3d inWorldOffset = clampVectorToBlock(targetBit.mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT));
                    final Vector3d inWorldTarget = hitBlockPosVector.add(inWorldOffset);

                    if (context.getInAreaTarget(inWorldTarget)
                          .map(state -> !state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) ^ airOnly)
                          .orElse(airOnly)
                    )
                    {
                        context.include(inWorldTarget);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    private Vector3d clampVectorToBlock(final Vector3d v)
    {
        return new Vector3d(
          v.getX() < 0 ? 0 : (v.getX() >= 1 ? 1 - ONE_THOUSANDS : v.getX()),
          v.getY() < 0 ? 0 : (v.getY() >= 1 ? 1 - ONE_THOUSANDS : v.getY()),
          v.getZ() < 0 ? 0 : (v.getZ() >= 1 ? 1 - ONE_THOUSANDS : v.getZ())
        );
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
        return Optional.of(ModChiselModeGroups.LINE);
    }
}
