package mod.chiselsandbits.block;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.item.IMultiStateItemFactory;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.registry.ModTileEntityTypes;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
        return new ChiseledBlockEntity(
          ModTileEntityTypes.CHISELED.get()
        );
    }


    @Override
    public float getSlipperiness(final BlockState state, final IWorldReader world, final BlockPos pos, @Nullable final Entity entity)
    {
        final IMultiStateBlockEntity blockEntityWithMultipleStates = getBlockEntityFromOrThrow(world, pos);
        return blockEntityWithMultipleStates.getStatistics().getSlipperiness();
    }

    @Override
    public boolean shouldCheckWeakPower(final BlockState state, final IWorldReader world, final BlockPos pos, final Direction side)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, pos);
        return multiStateBlockEntity.getStatistics().shouldCheckWeakPower();
    }

    @Override
    public float getAmbientOcclusionLightValue(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        return multiStateBlockEntity.getStatistics().getFullnessFactor();
    }

    //TODO: Check if getOpacity needs to be overridden.

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        return IVoxelShapeManager.getInstance().get(multiStateBlockEntity);
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        return IVoxelShapeManager.getInstance().get(multiStateBlockEntity);
    }

    @NotNull
    @Override
    public VoxelShape getRayTraceShape(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(reader, pos);
        return IVoxelShapeManager.getInstance().get(multiStateBlockEntity);
    }

    @Override
    public int getLightValue(final BlockState state, final IBlockReader world, final BlockPos pos)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, pos);
        return (int) (multiStateBlockEntity.getStatistics().getLightEmissionFactor() * world.getMaxLightLevel());
    }

    @Override
    public float getPlayerRelativeBlockHardness(@NotNull final BlockState state, @NotNull final PlayerEntity player, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        return multiStateBlockEntity.getStatistics().getRelativeBlockHardness(player);
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
    public boolean propagatesSkylightDown(final BlockState state, final IBlockReader reader, final BlockPos pos)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(reader, pos);
        return !multiStateBlockEntity.getStatistics().isFullBlock();
    }

    @Override
    public boolean removedByPlayer(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final boolean willHarvest, final FluidState fluid)
    {
        if ( !willHarvest && ChiselsAndBits.getConfig().getClient().addBrokenBlocksToCreativeClipboard.get() )
        {
            final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, pos);
            final IMultiStateSnapshot multiStateSnapshot = multiStateBlockEntity.createSnapshot();

            IChangeTracker.getInstance().onBlockBroken(
              world,
              pos,
              multiStateSnapshot
            );
        }

        return super.removedByPlayer( state, world, pos, player, willHarvest, fluid );
    }

    @Override
    public boolean isReplaceable(final BlockState state, final BlockItemUseContext useContext)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(useContext.getWorld(), useContext.getPos());
        return multiStateBlockEntity.getStatistics().isEmptyBlock();
    }

    @Override
    public void harvestBlock(final World worldIn, final PlayerEntity player, final BlockPos pos, final BlockState state, @Nullable final TileEntity te, final ItemStack stack)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        final IMultiStateSnapshot snapshot = multiStateBlockEntity.createSnapshot();

        spawnAsEntity(worldIn, pos, IMultiStateItemFactory.getInstance().createFrom(snapshot));
    }

    @Override
    public void onBlockPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(worldIn, pos);
        final Direction placementDirection = placer == null ? Direction.NORTH : placer.getHorizontalFacing().getOpposite();
        final int horizontalIndex = placementDirection.getHorizontalIndex();
        
        int rotationCount = horizontalIndex - 4;
        if (rotationCount < 0) {
            rotationCount += 4;
        }

        multiStateBlockEntity.rotate(Direction.Axis.Y, rotationCount);
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        if (!(target instanceof BlockRayTraceResult))
            return ItemStack.EMPTY;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, pos);

        final Vector3d hitVec = blockRayTraceResult.getHitVec();
        final BlockPos blockPos = new BlockPos(hitVec);
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

        final Optional<IStateEntryInfo> potentialEntry = multiStateBlockEntity.getInAreaTarget(hitDelta);
        if (!potentialEntry.isPresent())
            return ItemStack.EMPTY;

        final IStateEntryInfo targetedStateEntry = potentialEntry.get();
        return IMultiStateItemFactory.getInstance().createFrom(targetedStateEntry);
    }

    @Override
    @OnlyIn( Dist.CLIENT )
    public boolean addDestroyEffects(final BlockState state, final World world, final BlockPos pos, final ParticleManager manager)
    {
        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, pos);
        return ClientSide.instance.addBlockDestroyEffects(
          world,
          pos,
          multiStateBlockEntity.getStatistics().getPrimaryState(),
          manager
        );
    }

    @Override
    @OnlyIn( Dist.CLIENT )
    public boolean addHitEffects(final BlockState state, final World world, final RayTraceResult target, final ParticleManager manager)
    {
        if (!(target instanceof BlockRayTraceResult))
            return false;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) target;

        final IMultiStateBlockEntity multiStateBlockEntity = getBlockEntityFromOrThrow(world, blockRayTraceResult.getPos());
        return ClientSide.instance.addHitEffects(
          world,
          blockRayTraceResult,
          multiStateBlockEntity.getStatistics().getPrimaryState(),
          manager
      );
    }

    @Override
    public BlockState rotate(final BlockState state, final IWorld world, final BlockPos pos, final Rotation rotation)
    {
        for (final Direction.Axis axis : Direction.Axis.values())
        {
            if (rotation.getOrientation().isOnAxis(axis)) {
                final IMultiStateBlockEntity blockEntityWithMultipleStates = getBlockEntityFromOrThrow(world, pos);
                blockEntityWithMultipleStates.rotate(axis);
            }
        }

        return state;
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        final IMultiStateBlockEntity blockEntityWithMultipleStates = getBlockEntityFromOrThrow(world, pos);
        final BlockState primaryState = blockEntityWithMultipleStates.getStatistics().getPrimaryState();

        return primaryState.canHarvestBlock(
          new SingleBlockBlockReader(
            primaryState,
            pos,
            world
          ),
          pos,
          player
        );
    }

    @Override
    public void fillItemGroup(final ItemGroup group, final NonNullList<ItemStack> items)
    {
        //No items.
    }

    @NotNull
    @Override
    public BlockState getPrimaryState(@NotNull final IBlockReader world, @NotNull final BlockPos pos)
    {
        final IMultiStateBlockEntity blockEntityWithMultipleStates = getBlockEntityFromOrThrow(world, pos);
        return blockEntityWithMultipleStates.getStatistics().getPrimaryState();
    }

    @NotNull
    private IMultiStateBlockEntity getBlockEntityFromOrThrow(final IBlockReader worldIn, final BlockPos pos)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalArgumentException(String.format("The given position: %s does not seem to point to a multi state block!", pos));

        return (IMultiStateBlockEntity) tileEntity;
    }
}
