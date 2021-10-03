package mod.chiselsandbits.block;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.ArrayUtils;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.api.util.SingleBlockWorldReader;
import mod.chiselsandbits.api.util.StateEntryPredicates;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataManager;
import mod.chiselsandbits.network.packets.NeighborBlockUpdatedPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class ChiseledBlock extends Block implements IMultiStateBlock, SimpleWaterloggedBlock
{
    public ChiseledBlock(Properties properties)
    {
        super(properties);
    }


    @Override
    public float getFriction(final BlockState state, final LevelReader world, final BlockPos pos, @Nullable final Entity entity)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getSlipperiness())
                 .orElse(0f);
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter world, final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> (int) (multiStateBlockEntity.getStatistics().getLightEmissionFactor() * world.getMaxLightLevel()))
                 .orElse(0);
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final BlockGetter world, final BlockPos pos, final Player player)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(e -> {
                     final BlockState primaryState = e.getStatistics().getPrimaryState();

                     return primaryState.canHarvestBlock(
                       new SingleBlockBlockReader(
                         primaryState,
                         pos,
                         world
                       ),
                       pos,
                       player
                     );
                 })
                 .orElse(true);
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final HitResult target, final BlockGetter world, final BlockPos pos, final Player player)
    {
        if (!(target instanceof BlockHitResult))
        {
            return ItemStack.EMPTY;
        }

        final BlockHitResult blockRayTraceResult = (BlockHitResult) target;

        if (player.isCrouching())
        {
            return getBlockEntityFromOrThrow(world, pos)
                     .map(e -> {
                         final IMultiStateSnapshot snapshot = e.createSnapshot();
                         return snapshot.toItemStack().toBlockStack();
                     })
                     .orElse(ItemStack.EMPTY);
        }

        return getBlockEntityFromOrThrow(world, pos)
                 .flatMap(e -> {
                     final Vec3 hitVec = blockRayTraceResult.getLocation();
                     final BlockPos blockPos = blockRayTraceResult.getBlockPos();
                     final Vec3 accuratePos = new Vec3(
                       blockPos.getX(),
                       blockPos.getY(),
                       blockPos.getZ()
                     );
                     final Vec3 faceOffset = new Vec3(
                       blockRayTraceResult.getDirection().getOpposite().getStepX() * StateEntrySize.current().getSizePerHalfBit(),
                       blockRayTraceResult.getDirection().getOpposite().getStepY() * StateEntrySize.current().getSizePerHalfBit(),
                       blockRayTraceResult.getDirection().getOpposite().getStepZ() * StateEntrySize.current().getSizePerHalfBit()
                     );
                     final Vec3 hitDelta = hitVec.subtract(accuratePos).add(faceOffset);

                     return e.getInAreaTarget(hitDelta);
                 })
                 .map(targetedStateEntry -> IMultiStateItemFactory.getInstance().createBlockFrom(targetedStateEntry))
                 .orElse(ItemStack.EMPTY);
    }

    @Override
    public BlockState rotate(final BlockState state, final LevelAccessor world, final BlockPos pos, final Rotation rotation)
    {
        for (final Direction.Axis axis : Direction.Axis.values())
        {
            if (rotation.rotation().inverts(axis))
            {
                getBlockEntityFromOrThrow(world, pos)
                  .ifPresent(e -> e.rotate(axis));

                return state;
            }
        }

        return state;
    }

    @Override
    public boolean shouldCheckWeakPower(final BlockState state, final LevelReader world, final BlockPos pos, final Direction side)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().shouldCheckWeakPower())
                 .orElse(false);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(final BlockState state, final BlockAndTintGetter world, final BlockPos pos, final FluidState fluidState)
    {
        return true;
    }

    @NotNull
    private Optional<IMultiStateBlockEntity> getBlockEntityFromOrThrow(final BlockGetter worldIn, final BlockPos pos)
    {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
        {
            return Optional.empty();
        }

        return Optional.of((IMultiStateBlockEntity) tileEntity);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(reader, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().canPropagateSkylight())
                 .orElse(false);
    }

    @Override
    public void playerDestroy(
      @NotNull final Level worldIn,
      @NotNull final Player player,
      @NotNull final BlockPos pos,
      @NotNull final BlockState state,
      @Nullable final BlockEntity te,
      @NotNull final ItemStack stack)
    {
        if (te instanceof IMultiStateBlockEntity)
        {
            final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) te;

            final IMultiStateSnapshot snapshot = multiStateBlockEntity.createSnapshot();
            popResource(worldIn, pos, snapshot.toItemStack().toBlockStack());
        }
    }

    @Override
    public void setPlacedBy(
      @NotNull final Level worldIn,
      @NotNull final BlockPos pos,
      @NotNull final BlockState state,
      @Nullable final LivingEntity placer,
      @NotNull final ItemStack stack)
    {
        getBlockEntityFromOrThrow(worldIn, pos)
          .ifPresent(multiStateBlockEntity -> {
              final Direction placementDirection = placer == null ? Direction.NORTH : placer.getDirection().getOpposite();
              final int horizontalIndex = placementDirection.get2DDataValue();

              int rotationCount = horizontalIndex - 4;
              if (rotationCount < 0)
              {
                  rotationCount += 4;
              }

              multiStateBlockEntity.rotate(Direction.Axis.Y, rotationCount);
              super.setPlacedBy(worldIn, pos, state, placer, stack);
          });
    }

    @Override
    public void fillItemCategory(@NotNull final CreativeModeTab group, @NotNull final NonNullList<ItemStack> items)
    {
        //No items.
    }

    @Override
    public boolean hasDynamicShape()
    {
        return true;
    }

    @NotNull
    @Override
    public BlockState getPrimaryState(@NotNull final BlockGetter world, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(e -> e.getStatistics().getPrimaryState())
                 .orElse(Blocks.AIR.defaultBlockState());
    }

    @Override
    public void onRemove(final @NotNull BlockState state, final @NotNull Level worldIn, final @NotNull BlockPos pos, final BlockState newState, final boolean isMoving)
    {
        if (newState.getBlock() instanceof ChiseledBlock)
        {
            return;
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @NotNull
    @Override
    public PushReaction getPistonPushReaction(@NotNull final BlockState state)
    {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canBeReplaced(@NotNull final BlockState state, final BlockPlaceContext useContext)
    {
        return getBlockEntityFromOrThrow(useContext.getLevel(), useContext.getClickedPos())
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().isEmptyBlock())
                 .orElse(true);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final @NotNull BlockGetter reader, final @NotNull BlockPos pos)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(reader, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity,
                                     areaAccessor -> StateEntryPredicates.COLLIDEABLE_ONLY))
                                   .orElse(Shapes.empty());

        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    public float getShadeBrightness(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos)
    {
        return 1f - 0.8f * getBlockEntityFromOrThrow(worldIn, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getFullnessFactor())
                 .orElse(0f);
    }

    @Override
    public int getLightBlock(final @NotNull BlockState state, final @NotNull BlockGetter worldIn, final @NotNull BlockPos pos)
    {
        return (int) (float) (getBlockEntityFromOrThrow(worldIn, pos)
                  .map(multiStateBlockEntity -> worldIn.getMaxLightLevel() * multiStateBlockEntity.getStatistics().getFullnessFactor())
                  .orElse(0f));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos, @NotNull final CollisionContext context)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(worldIn, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity))
                                   .orElse(Shapes.empty());

        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos, @NotNull final CollisionContext context)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(worldIn, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity,
                                     areaAccessor -> StateEntryPredicates.COLLIDEABLE_ONLY))
                                   .orElse(Shapes.empty());

        if (shape.isEmpty()) {
            final boolean justFluids = getBlockEntityFromOrThrow(worldIn, pos)
                                         .map(IAreaAccessor::stream)
                                         .map(stream -> stream
                                           .allMatch(stateEntry -> stateEntry.getState().isAir() || !stateEntry.getState().getFluidState().isEmpty())
                                         )
                                         .orElse(false);

            return justFluids ? shape : Shapes.block();
        }

        return shape;
    }

    @NotNull
    @Override
    public VoxelShape getVisualShape(@NotNull final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos, @NotNull final CollisionContext context)
    {
        return getShape(state, reader, pos, context);
    }

    @Override
    public float getDestroyProgress(
      @NotNull final BlockState state,
      @NotNull final Player player,
      @NotNull final BlockGetter worldIn,
      @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getRelativeBlockHardness(player))
                 .orElse(1f);
    }

    @Override
    public boolean canPlaceLiquid(final @NotNull BlockGetter worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final Fluid fluidIn)
    {
        return IEligibilityManager.getInstance().canBeChiseled(fluidIn.defaultFluidState().createLegacyBlock());
    }

    @Override
    public boolean placeLiquid(final @NotNull LevelAccessor worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull FluidState fluidStateIn)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
                 .map(entity -> {
                     try (IBatchMutation ignored = entity.batch())
                     {
                         entity.mutableStream().forEach(
                           stateEntry -> {
                               if (stateEntry.getState().isAir())
                               {
                                   try
                                   {
                                       stateEntry.setState(fluidStateIn.createLegacyBlock());
                                   }
                                   catch (SpaceOccupiedException e)
                                   {
                                       //Ignore
                                   }
                               }
                           }
                         );
                     }

                     return true;
                 })
                 .orElse(false);
    }

    @Override
    public @NotNull ItemStack pickupBlock(final @NotNull LevelAccessor p_154560_, final @NotNull BlockPos p_154561_, final @NotNull BlockState p_154562_)
    {
        return ItemStack.EMPTY;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(final @NotNull BlockPos pos, final @NotNull BlockState state)
    {
        return new ChiseledBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(
      final @NotNull BlockState state,
      final Level level,
      final @NotNull BlockPos position,
      final @NotNull Block block,
      final @NotNull BlockPos otherPosition,
      final boolean update)
    {
        final BlockEntity tileEntity = level.getBlockEntity(position);
        if (level.isClientSide() && tileEntity instanceof ChiseledBlockEntity) {
            ChiseledBlockModelDataManager.getInstance().updateModelData((ChiseledBlockEntity) tileEntity);
        } else if (!level.isClientSide() && tileEntity instanceof ChiseledBlockEntity) {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToTrackingChunk(
                new NeighborBlockUpdatedPacket(position, otherPosition),
                level.getChunkAt(position)
            );
        }
    }

    @Override
    public float[] getBeaconColorMultiplier(final BlockState state, final LevelReader world, final BlockPos pos, final BlockPos beaconPos)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .filter(e -> e.getStatistics().getStateCounts().keySet()
                                .stream()
                                .filter(entryState -> !entryState.isAir())
                                .allMatch(entryState -> entryState.getBeaconColorMultiplier(
                                  new SingleBlockWorldReader(
                                    e.getStatistics().getPrimaryState(),
                                    pos,
                                    world
                                  ),
                                  pos,
                                  beaconPos
                                ) != null)
                 )
                 .flatMap(e -> e.getStatistics().getStateCounts().entrySet()
                   .stream()
                   .filter(entryState -> !entryState.getKey().isAir())
                   .map(entryState -> ArrayUtils.multiply(entryState.getKey().getBeaconColorMultiplier(
                     new SingleBlockWorldReader(
                       e.getStatistics().getPrimaryState(),
                       pos,
                       world
                     ),
                     pos,
                     beaconPos
                   ), entryState.getValue())).reduce((floats, floats2) -> {
                       if (floats.length != floats2.length)
                           return null;

                       if (floats == null)
                           return null;

                       if (floats2 == null)
                           return null;

                       final float[] result = new float[floats.length];
                       for (int i = 0; i < floats.length; i++)
                       {
                           result[i] = floats[i] + floats2[i];
                       }
                       return result;
                   })
                   .filter(Objects::nonNull)
                   .flatMap(summedResult -> getBlockEntityFromOrThrow(world, pos)
                     .map(entity -> ArrayUtils.multiply(summedResult, 1f / (entity.getStatistics().getFullnessFactor() * StateEntrySize.current().getBitsPerBlock())))
                   )
                ).orElse(null);
    }
}
