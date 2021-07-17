package mod.chiselsandbits.chiseling.modes.plane;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.StateEntrySize;
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
import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class PlaneChiseledMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final int                       depth;
    private final IFormattableTextComponent displayName;
    private final ResourceLocation          iconName;

    PlaneChiseledMode(final int depth, final IFormattableTextComponent displayName, final ResourceLocation iconName)
    {
        this.depth = depth;
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
          Direction::getOpposite
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

    @SuppressWarnings("deprecation")
    @Override
    public ClickProcessingState onRightClickBy(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.copy(face.getDirectionVec()),
          Function.identity()
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
      final Function<Direction, Direction> iterationAdaptor
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
            .mul(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final Vector3d hitBlockPosVector = Vector3d.copy(new BlockPos(hitVector));
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vector3d inBlockBitVector = inBlockHitVector.mul(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide());

        final Direction iterationDirection = iterationAdaptor.apply(blockRayTraceResult.getFace());
        switch (iterationDirection)
        {
            case DOWN:
                includeDownAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case UP:
                includeUpAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case NORTH:
                includeNorthAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case SOUTH:
                includeSouthAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case WEST:
                includeWestAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case EAST:
                includeEastAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
        }

        return Optional.empty();
    }

    private void includeDownAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.getY() - depth, 0).add(Vector3d.copy(Direction.UP.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.getY() - depth, 15.5).add(Vector3d.copy(Direction.UP.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.getY() - 0.5f, 0).add(Vector3d.copy(Direction.UP.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.getY() - 0.5f , 15.5).add(Vector3d.copy(Direction.UP.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeUpAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.getY() + depth, 0).add(Vector3d.copy(Direction.DOWN.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.getY() + depth, 15.5).add(Vector3d.copy(Direction.DOWN.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.getY() + 0.5f, 0).add(Vector3d.copy(Direction.DOWN.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.getY() + 0.5f , 15.5).add(Vector3d.copy(Direction.DOWN.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeNorthAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.getZ() - depth).add(Vector3d.copy(Direction.SOUTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.getZ() - depth).add(Vector3d.copy(Direction.SOUTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.getZ() - 0.5f).add(Vector3d.copy(Direction.SOUTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.getZ() - 0.5f).add(Vector3d.copy(Direction.SOUTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeSouthAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.getZ() + depth).add(Vector3d.copy(Direction.NORTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.getZ() + depth).add(Vector3d.copy(Direction.NORTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.getZ() + 0.5f).add(Vector3d.copy(Direction.NORTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.getZ() + 0.5f).add(Vector3d.copy(Direction.NORTH.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeWestAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() - depth, 0, 0).add(Vector3d.copy(Direction.EAST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() - depth, 15.5, 15.5).add(Vector3d.copy(Direction.EAST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() - 0.5f, 0, 0).add(Vector3d.copy(Direction.EAST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() - 0.5f , 15.5, 15.5).add(Vector3d.copy(Direction.EAST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeEastAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() + depth, 0, 0).add(Vector3d.copy(Direction.WEST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() + depth, 15.5, 15.5).add(Vector3d.copy(Direction.WEST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() + 0.5f, 0, 0).add(Vector3d.copy(Direction.WEST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.getX() + 0.5f , 15.5, 15.5).add(Vector3d.copy(Direction.WEST.getDirectionVec())).mul(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
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
        return Optional.of(ModChiselModeGroups.PLANE);
    }
}
