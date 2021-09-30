package mod.chiselsandbits.block;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.api.util.SingleBlockWorldReader;
import mod.chiselsandbits.api.util.StateEntryPredicates;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.utils.EffectUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class ChiseledBlock extends Block implements IMultiStateBlock, IWaterLoggable
{
    public ChiseledBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getSlipperiness(final BlockState state, final IWorldReader world, final BlockPos pos, @Nullable final Entity entity)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getSlipperiness())
                 .orElse(0f);
    }

    @Override
    public int getLightValue(final BlockState state, final IBlockReader world, final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> (int) (multiStateBlockEntity.getStatistics().getLightEmissionFactor() * world.getMaxLightLevel()))
                 .orElse(0);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ChiseledBlockEntity();
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
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
    public boolean removedByPlayer(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final boolean willHarvest, final FluidState fluid)
    {
        if (!willHarvest && Configuration.getInstance().getClient().addBrokenBlocksToCreativeClipboard.get())
        {
            getBlockEntityFromOrThrow(world, pos)
              .ifPresent(multiStateBlockEntity -> {
                  final IMultiStateSnapshot multiStateSnapshot = multiStateBlockEntity.createSnapshot();

                  IChangeTrackerManager.getInstance().getChangeTracker(player).onBlockBroken(
                    pos,
                    multiStateSnapshot
                  );
              });
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        if (!(target instanceof BlockRayTraceResult))
        {
            return ItemStack.EMPTY;
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

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
                     final Vector3d hitVec = blockRayTraceResult.getLocation();
                     final BlockPos blockPos = blockRayTraceResult.getBlockPos();
                     final Vector3d accuratePos = new Vector3d(
                       blockPos.getX(),
                       blockPos.getY(),
                       blockPos.getZ()
                     );
                     final Vector3d faceOffset = new Vector3d(
                       blockRayTraceResult.getDirection().getOpposite().getStepX() * StateEntrySize.current().getSizePerHalfBit(),
                       blockRayTraceResult.getDirection().getOpposite().getStepY() * StateEntrySize.current().getSizePerHalfBit(),
                       blockRayTraceResult.getDirection().getOpposite().getStepZ() * StateEntrySize.current().getSizePerHalfBit()
                     );
                     final Vector3d hitDelta = hitVec.subtract(accuratePos).add(faceOffset);

                     return e.getInAreaTarget(hitDelta);
                 })
                 .map(targetedStateEntry -> IMultiStateItemFactory.getInstance().createBlockFrom(targetedStateEntry))
                 .orElse(ItemStack.EMPTY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(final BlockState state, final World world, final RayTraceResult target, final ParticleManager manager)
    {
        if (!(target instanceof BlockRayTraceResult))
        {
            return false;
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

        return getBlockEntityFromOrThrow(world, blockRayTraceResult.getBlockPos())
                 .map(e -> EffectUtils.addHitEffects(
                   world,
                   blockRayTraceResult,
                   e.getStatistics().getPrimaryState(),
                   manager
                 ))
                 .orElse(false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(final BlockState state, final World world, final BlockPos pos, final ParticleManager manager)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(e -> EffectUtils.addBlockDestroyEffects(
                   new SingleBlockWorldReader(
                     e.getStatistics().getPrimaryState(),
                     pos,
                     world
                   ),
                   pos,
                   e.getStatistics().getPrimaryState(),
                   manager,
                   world
                 ))
                 .orElse(false);
    }

    @Override
    public BlockState rotate(final BlockState state, final IWorld world, final BlockPos pos, final Rotation rotation)
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
    public boolean shouldCheckWeakPower(final BlockState state, final IWorldReader world, final BlockPos pos, final Direction side)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().shouldCheckWeakPower())
                 .orElse(false);
    }

    @Override
    public boolean isToolEffective(final BlockState state, final ToolType tool)
    {
        return true;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(final BlockState state, final IBlockDisplayReader world, final BlockPos pos, final FluidState fluidState)
    {
        return true;
    }

    @NotNull
    private Optional<IMultiStateBlockEntity> getBlockEntityFromOrThrow(final IBlockReader worldIn, final BlockPos pos)
    {
        final TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
        {
            return Optional.empty();
        }

        return Optional.of((IMultiStateBlockEntity) tileEntity);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(reader, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().canPropagateSkylight())
                 .orElse(false);
    }

    @Override
    public void playerDestroy(
      @NotNull final World worldIn,
      @NotNull final PlayerEntity player,
      @NotNull final BlockPos pos,
      @NotNull final BlockState state,
      @Nullable final TileEntity te,
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
      @NotNull final World worldIn,
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
    public void fillItemCategory(@NotNull final ItemGroup group, @NotNull final NonNullList<ItemStack> items)
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
    public BlockState getPrimaryState(@NotNull final IBlockReader world, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
                 .map(e -> e.getStatistics().getPrimaryState())
                 .orElse(Blocks.AIR.defaultBlockState());
    }

    @Override
    public void onRemove(final @NotNull BlockState state, final @NotNull World worldIn, final @NotNull BlockPos pos, final BlockState newState, final boolean isMoving)
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
    public boolean canBeReplaced(@NotNull final BlockState state, final BlockItemUseContext useContext)
    {
        return getBlockEntityFromOrThrow(useContext.getLevel(), useContext.getClickedPos())
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().isEmptyBlock())
                 .orElse(true);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final @NotNull IBlockReader reader, final @NotNull BlockPos pos)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(reader, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity,
                                     areaAccessor -> StateEntryPredicates.COLLIDEABLE_ONLY))
                                   .orElse(VoxelShapes.empty());

        return shape.isEmpty() ? VoxelShapes.block() : shape;
    }

    @Override
    public float getShadeBrightness(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        return 1f - 0.8f * getBlockEntityFromOrThrow(worldIn, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getFullnessFactor())
                 .orElse(0f);
    }

    //TODO: Check if getOpacity needs to be overridden.

    @Override
    public int getLightBlock(final @NotNull BlockState state, final @NotNull IBlockReader worldIn, final @NotNull BlockPos pos)
    {
        return (int) (float) (getBlockEntityFromOrThrow(worldIn, pos)
                  .map(multiStateBlockEntity -> worldIn.getMaxLightLevel() * multiStateBlockEntity.getStatistics().getFullnessFactor())
                  .orElse(0f));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(worldIn, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity))
                                   .orElse(VoxelShapes.empty());

        return shape.isEmpty() ? VoxelShapes.block() : shape;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        final VoxelShape shape = getBlockEntityFromOrThrow(worldIn, pos)
                                   .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity,
                                     areaAccessor -> StateEntryPredicates.COLLIDEABLE_ONLY))
                                   .orElse(VoxelShapes.empty());

        if (shape.isEmpty()) {
            final boolean justFluids = getBlockEntityFromOrThrow(worldIn, pos)
                                         .map(IAreaAccessor::stream)
                                         .map(stream -> stream
                                           .allMatch(stateEntry -> stateEntry.getState().isAir() || !stateEntry.getState().getFluidState().isEmpty())
                                         )
                                         .orElse(false);

            return justFluids ? shape : VoxelShapes.block();
        }

        return shape;
    }

    @NotNull
    @Override
    public VoxelShape getVisualShape(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        return getShape(state, reader, pos, context);
    }

    @Override
    public float getDestroyProgress(
      @NotNull final BlockState state,
      @NotNull final PlayerEntity player,
      @NotNull final IBlockReader worldIn,
      @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
                 .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getRelativeBlockHardness(player))
                 .orElse(1f);
    }

    @Override
    public boolean canPlaceLiquid(final @NotNull IBlockReader worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final Fluid fluidIn)
    {
        return IEligibilityManager.getInstance().canBeChiseled(fluidIn.defaultFluidState().createLegacyBlock());
    }

    @Override
    public boolean placeLiquid(final @NotNull IWorld worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull FluidState fluidStateIn)
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
                                       stateEntry.setState(fluidStateIn.createLegacyBlock().getBlockState());
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
    public @NotNull Fluid takeLiquid(final @NotNull IWorld worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state)
    {
        return Fluids.EMPTY;
    }
}
