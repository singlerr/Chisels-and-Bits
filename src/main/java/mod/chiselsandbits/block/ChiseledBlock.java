package mod.chiselsandbits.block;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.api.util.SingleBlockWorldReader;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.utils.EffectUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class ChiseledBlock extends Block implements IMultiStateBlock
{
    public ChiseledBlock(Properties properties)
    {
        super(properties);
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
    public float getSlipperiness(final BlockState state, final IWorldReader world, final BlockPos pos, @Nullable final Entity entity)
    {
        return getBlockEntityFromOrThrow(world, pos)
          .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getSlipperiness())
          .orElse(0f);
    }

    @Override
    public boolean shouldCheckWeakPower(final BlockState state, final IWorldReader world, final BlockPos pos, final Direction side)
    {
        return getBlockEntityFromOrThrow(world, pos)
          .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().shouldCheckWeakPower())
          .orElse(false);
    }

    @Override
    public float getAmbientOcclusionLightValue(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
          .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getFullnessFactor())
          .orElse(1f);
    }

    //TODO: Check if getOpacity needs to be overridden.

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
                 .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity))
                 .orElse(VoxelShapes.empty());
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        return getShape(state, worldIn, pos, context);
    }

    @NotNull
    @Override
    public VoxelShape getRayTraceShape(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        return getShape(state, reader, pos, context);
    }

    @Override
    public int getLightValue(final BlockState state, final IBlockReader world, final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
          .map(multiStateBlockEntity -> (int) (multiStateBlockEntity.getStatistics().getLightEmissionFactor() * world.getMaxLightLevel()))
          .orElse(0);
    }

    @Override
    public float getPlayerRelativeBlockHardness(@NotNull final BlockState state, @NotNull final PlayerEntity player, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(worldIn, pos)
          .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getRelativeBlockHardness(player))
          .orElse(1f);
    }

    @Override
    public boolean isToolEffective(final BlockState state, final ToolType tool)
    {
        return true;
    }

    @NotNull
    @Override
    public PushReaction getPushReaction(@NotNull final BlockState state)
    {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean isVariableOpacity()
    {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(reader, pos)
          .map(multiStateBlockEntity -> !multiStateBlockEntity.getStatistics().canPropagateSkylight())
          .orElse(false);
    }

    @Override
    public boolean removedByPlayer(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final boolean willHarvest, final FluidState fluid)
    {
        if ( !willHarvest && Configuration.getInstance().getClient().addBrokenBlocksToCreativeClipboard.get() )
        {
            getBlockEntityFromOrThrow(world, pos)
              .ifPresent(multiStateBlockEntity -> {
                  final IMultiStateSnapshot multiStateSnapshot = multiStateBlockEntity.createSnapshot();

                  IChangeTracker.getInstance().onBlockBroken(
                    world,
                    pos,
                    multiStateSnapshot
                  );
              });
        }

        return super.removedByPlayer( state, world, pos, player, willHarvest, fluid );
    }

    @Override
    public boolean isReplaceable(@NotNull final BlockState state, final BlockItemUseContext useContext)
    {
        return getBlockEntityFromOrThrow(useContext.getWorld(), useContext.getPos())
          .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().isEmptyBlock())
          .orElse(true);
    }

    @Override
    public void harvestBlock(@NotNull final World worldIn, @NotNull final PlayerEntity player, @NotNull final BlockPos pos, @NotNull final BlockState state, @Nullable final TileEntity te, @NotNull final ItemStack stack)
    {
        if (te instanceof IMultiStateBlockEntity) {
            final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) te;

            final IMultiStateSnapshot snapshot = multiStateBlockEntity.createSnapshot();
            spawnAsEntity(worldIn, pos, snapshot.toItemStack().toItemStack());
        }
    }

    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, @NotNull final BlockState state, @Nullable final LivingEntity placer, @NotNull final ItemStack stack)
    {
        getBlockEntityFromOrThrow(worldIn, pos)
          .ifPresent(multiStateBlockEntity -> {
              final Direction placementDirection = placer == null ? Direction.NORTH : placer.getHorizontalFacing().getOpposite();
              final int horizontalIndex = placementDirection.getHorizontalIndex();

              int rotationCount = horizontalIndex - 4;
              if (rotationCount < 0) {
                  rotationCount += 4;
              }

              multiStateBlockEntity.rotate(Direction.Axis.Y, rotationCount);
              super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
          });
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        if (!(target instanceof BlockRayTraceResult))
            return ItemStack.EMPTY;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

        return getBlockEntityFromOrThrow(world, pos)
          .flatMap(e -> {
              final Vector3d hitVec = blockRayTraceResult.getHitVec();
              final BlockPos blockPos = blockRayTraceResult.getPos();
              final Vector3d accuratePos = new Vector3d(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ()
              );
              final Vector3d faceOffset = new Vector3d(
                blockRayTraceResult.getFace().getOpposite().getXOffset() * ChiseledBlockEntity.SIZE_PER_HALF_BIT,
                blockRayTraceResult.getFace().getOpposite().getYOffset() * ChiseledBlockEntity.SIZE_PER_HALF_BIT,
                blockRayTraceResult.getFace().getOpposite().getZOffset() * ChiseledBlockEntity.SIZE_PER_HALF_BIT
              );
              final Vector3d hitDelta = hitVec.subtract(accuratePos).add(faceOffset);

              return e.getInAreaTarget(hitDelta);
          })
          .map(targetedStateEntry -> IMultiStateItemFactory.getInstance().createFrom(targetedStateEntry))
          .orElse(ItemStack.EMPTY);
    }

    @Override
    @OnlyIn( Dist.CLIENT )
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
    @OnlyIn( Dist.CLIENT )
    public boolean addHitEffects(final BlockState state, final World world, final RayTraceResult target, final ParticleManager manager)
    {
        if (!(target instanceof BlockRayTraceResult))
            return false;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

        return getBlockEntityFromOrThrow(world, blockRayTraceResult.getPos())
          .map(e -> EffectUtils.addHitEffects(
            world,
            blockRayTraceResult,
            e.getStatistics().getPrimaryState(),
            manager
          ))
          .orElse(false);
    }

    @Override
    public BlockState rotate(final BlockState state, final IWorld world, final BlockPos pos, final Rotation rotation)
    {
        for (final Direction.Axis axis : Direction.Axis.values())
        {
            if (rotation.getOrientation().isOnAxis(axis)) {
                getBlockEntityFromOrThrow(world, pos)
                  .ifPresent(e -> e.rotate(axis));

                return state;
            }
        }

        return state;
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
    public void fillItemGroup(@NotNull final ItemGroup group, @NotNull final NonNullList<ItemStack> items)
    {
        //No items.
    }

    @NotNull
    @Override
    public BlockState getPrimaryState(@NotNull final IBlockReader world, @NotNull final BlockPos pos)
    {
        return getBlockEntityFromOrThrow(world, pos)
          .map(e -> e.getStatistics().getPrimaryState())
          .orElse(Blocks.AIR.getDefaultState());
    }

    @NotNull
    private Optional<IMultiStateBlockEntity> getBlockEntityFromOrThrow(final IBlockReader worldIn, final BlockPos pos)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            return Optional.empty();

        return Optional.of((IMultiStateBlockEntity) tileEntity);
    }
}
