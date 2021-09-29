package mod.chiselsandbits.pattern.placement;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.pattern.placement.PlacementResult;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.registrars.ModPatternPlacementTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static mod.chiselsandbits.api.util.ColorUtils.MISSING_BITS_OR_SPACE_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.ColorUtils.NOT_FITTING_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.constants.Constants.MOD_ID;

public class RemovalPatternPlacementType extends ForgeRegistryEntry<IPatternPlacementType> implements IPatternPlacementType
{

    @Override
    public VoxelShape buildVoxelShapeForWireframe(
      final IMultiStateSnapshot sourceSnapshot, final PlayerEntity player, final Vector3d targetedPoint, final Direction hitFace)
    {
        final BlockPos targetedPosition = hitFace.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? new BlockPos(targetedPoint) : new BlockPos(targetedPoint).offset(hitFace.getOpposite().getNormal());
        final VoxelShape targetingShape = BlockPosStreamProvider.getForRange(
          player.isCrouching() ? targetedPoint : Vector3d.atLowerCornerOf(targetedPosition) ,
          player.isCrouching() ? targetedPoint.add(0.9999, 0.9999,0.9999): Vector3d.atLowerCornerOf(targetedPosition)
        ).map(position -> player.level.getBlockState(position).getShape(
          player.level,
          position
        ).move(
          position.getX() - targetedPosition.getX(),
          position.getY() - targetedPosition.getY(),
          position.getZ() - targetedPosition.getZ()
          )
        ).reduce(VoxelShapes.empty(), (voxelShape, voxelShape2) -> VoxelShapes.joinUnoptimized(voxelShape, voxelShape2, IBooleanFunction.OR)).optimize();

        final Vector3d offSet = player.isCrouching() ?
                                  new Vector3d(
                                    targetedPoint.x() - targetedPosition.getX(),
                                    targetedPoint.y() - targetedPosition.getY(),
                                    targetedPoint.z() - targetedPosition.getZ()
                                  ) : Vector3d.ZERO;
        final VoxelShape ownShape = ModPatternPlacementTypes.PLACEMENT.get().buildVoxelShapeForWireframe(
          sourceSnapshot, player, targetedPoint, hitFace
        ).move(
          offSet.x(),
          offSet.y(),
          offSet.z()
        );

        return VoxelShapes.join(
          targetingShape, ownShape, IBooleanFunction.ONLY_FIRST
        );
    }

    @Override
    public PlacementResult performPlacement(
      final IMultiStateSnapshot source, final BlockItemUseContext context, final boolean simulate)
    {
        final Vector3d targetedPosition = context.getPlayer().isCrouching() ?
                                            context.getClickLocation()
                                            : Vector3d.atLowerCornerOf(context.getClickedPos().offset(context.getClickedFace().getOpposite().getNormal()));
        final IWorldAreaMutator areaMutator =
          IMutatorFactory.getInstance().covering(
            context.getLevel(),
            targetedPosition,
            targetedPosition.add(0.9999,0.9999,0.9999)
          );

        final boolean isChiseledBlock = BlockPosStreamProvider.getForRange(areaMutator.getInWorldStartPoint(), areaMutator.getInWorldEndPoint())
          .map(pos -> context.getLevel().getBlockState(pos))
          .allMatch(state -> state.getBlock() instanceof IMultiStateBlock);

        if (isChiseledBlock)
        {
            return PlacementResult.failure(NOT_FITTING_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotASolidBlock.getText());
        }

        final boolean isSupported = BlockPosStreamProvider.getForRange(areaMutator.getInWorldStartPoint(), areaMutator.getInWorldEndPoint())
          .map(pos -> context.getLevel().getBlockState(pos))
          .allMatch(IEligibilityManager.getInstance()::canBeChiseled);

        if (!isSupported)
        {
            return PlacementResult.failure(NOT_FITTING_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotASupportedBlock.getText());
        }

        final Map<BlockState, Integer> totalRemovedBits = source.stream()
          .filter(s -> !s.getState().isAir())
          .filter(s -> {
              final Optional<IStateEntryInfo> o = areaMutator.getInAreaTarget(s.getStartPoint());

              return o
                .filter(os -> !os.getState().isAir())
                .map(os -> !os.getState().equals(s.getState()))
                .orElse(false);
          })
          .map(s -> areaMutator.getInAreaTarget(s.getStartPoint()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toMap(
            IStateEntryInfo::getState,
            s -> 1,
            Integer::sum
          ));

        final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(context.getPlayer());
        final boolean hasRequiredSpace = context.getPlayer().isCreative() ||
                                           totalRemovedBits.entrySet().stream().allMatch(e -> playerBitInventory.canInsert(e.getKey(), e.getValue()));

        if (!hasRequiredSpace)
        {
            return PlacementResult.failure(MISSING_BITS_OR_SPACE_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNoBitSpace.getText());
        }

        if (simulate)
        {
            return PlacementResult.success();
        }

        try (IBatchMutation ignored = areaMutator.batch())
        {
            source.stream()
              .filter(s -> !s.getState().isAir())
              .forEach(
                stateEntryInfo -> areaMutator.clearInAreaTarget(
                  stateEntryInfo.getStartPoint()
                )
              );
        }

        if (!context.getPlayer().isCreative())
        {
            totalRemovedBits.forEach(playerBitInventory::insert);
        }

        return PlacementResult.success();
    }

    @Override
    public Vector3d getTargetedPosition(final ItemStack heldStack, final PlayerEntity playerEntity, final BlockRayTraceResult blockRayTraceResult)
    {
        return Vector3d.atLowerCornerOf(blockRayTraceResult.getBlockPos());
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return new ResourceLocation(
          MOD_ID,
          "textures/icons/pattern_remove.png"
        );
    }

    @Override
    public @NotNull Optional<IToolModeGroup> getGroup()
    {
        return Optional.empty();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return LocalStrings.PatternPlacementModeRemoval.getText();
    }
}
