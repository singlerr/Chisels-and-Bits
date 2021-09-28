package mod.chiselsandbits.pattern.placement;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.pattern.placement.PlacementResult;
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

import static mod.chiselsandbits.api.util.ColorUtils.MISSING_BITS_OR_SPACE_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.ColorUtils.NOT_FITTING_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.constants.Constants.MOD_ID;

public class RemovalPatternPlacementType extends ForgeRegistryEntry<IPatternPlacementType> implements IPatternPlacementType
{

    @Override
    public VoxelShape buildVoxelShapeForWireframe(
      final IMultiStateSnapshot sourceSnapshot, final PlayerEntity player, final Vector3d targetedPoint, final Direction hitFace)
    {
        final BlockPos targetedPosition = new BlockPos(targetedPoint);
        final VoxelShape targetingShape = player.level.getBlockState(targetedPosition).getShape(
          player.level,
          targetedPosition
        );

        final VoxelShape ownShape = ModPatternPlacementTypes.PLACEMENT.get().buildVoxelShapeForWireframe(
          sourceSnapshot, player, targetedPoint, hitFace
        );

        return VoxelShapes.join(
          targetingShape, ownShape, IBooleanFunction.ONLY_FIRST
        );
    }

    @Override
    public PlacementResult performPlacement(
      final IMultiStateSnapshot source, final BlockItemUseContext context, final boolean simulate)
    {
        final BlockPos targetedPosition = context.getClickedPos().offset(context.getClickedFace().getOpposite().getNormal());
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getLevel(), targetedPosition);

        final BlockState targetState = context.getLevel().getBlockState(targetedPosition);
        final boolean isChiseledBlock = targetState
          .getBlock() instanceof IMultiStateBlock;

        if (isChiseledBlock)
        {
            return PlacementResult.failure(NOT_FITTING_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotASolidBlock.getText());
        }

        final boolean isSupported = IEligibilityManager.getInstance().canBeChiseled(
          targetState
        );

        if (!isSupported)
        {
            return PlacementResult.failure(NOT_FITTING_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotASupportedBlock.getText());
        }

        final int totalBitCount = source.getStatics().getStateCounts().entrySet()
          .stream().filter(e -> !e.getKey().isAir())
          .mapToInt(Map.Entry::getValue)
          .sum();

        final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(context.getPlayer());
        final boolean hasRequiredSpace = context.getPlayer().isCreative() ||
                                           playerBitInventory.canInsert(targetState, totalBitCount);

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
            playerBitInventory.insert(
              targetState,
              totalBitCount
            );
        }

        return PlacementResult.success();
    }

    @Override
    public BlockPos getTargetedBlockPos(final ItemStack heldStack, final PlayerEntity playerEntity, final BlockRayTraceResult blockRayTraceResult)
    {
        return blockRayTraceResult.getBlockPos();
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
