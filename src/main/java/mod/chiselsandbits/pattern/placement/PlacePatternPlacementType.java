package mod.chiselsandbits.pattern.placement;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.pattern.placement.PlacementResult;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static mod.chiselsandbits.api.util.ColorUtils.MISSING_BITS_OR_SPACE_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.ColorUtils.NOT_FITTING_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.StateEntryPredicates.NOT_AIR;
import static mod.chiselsandbits.api.util.constants.Constants.MOD_ID;

public class PlacePatternPlacementType extends ForgeRegistryEntry<IPatternPlacementType> implements IPatternPlacementType
{

    @Override
    public VoxelShape buildVoxelShapeForWireframe(
      final IMultiStateSnapshot sourceSnapshot, final PlayerEntity player, final Vector3d targetedPoint, final Direction hitFace)
    {
        return VoxelShapeManager.getInstance()
          .get(sourceSnapshot,
            accessor -> NOT_AIR);
    }

    @Override
    public PlacementResult performPlacement(
      final IMultiStateSnapshot source, final BlockItemUseContext context, final boolean simulate)
    {
        final Vector3d targetedPosition = context.getPlayer().isCrouching() ?
                                            context.getClickLocation()
                                            : Vector3d.atLowerCornerOf(context.getClickedPos());
        final IWorldAreaMutator areaMutator =
          IMutatorFactory.getInstance().covering(
            context.getLevel(),
            targetedPosition,
            targetedPosition.add(0.9999,0.9999,0.9999)
          );

        final boolean isAir = BlockPosStreamProvider.getForRange(areaMutator.getInWorldStartPoint(), areaMutator.getInWorldEndPoint())
          .map(context.getLevel()::getBlockState)
          .allMatch(AbstractBlock.AbstractBlockState::isAir);

        if (!isAir)
        {
            return PlacementResult.failure(NOT_FITTING_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotAnAirBlock.getText());
        }

        final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(context.getPlayer());
        final boolean hasRequiredBits = context.getPlayer().isCreative() || source.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .allMatch(e -> playerBitInventory.canExtract(e.getKey(), e.getValue()));

        if (!hasRequiredBits)
        {
            return PlacementResult.failure(MISSING_BITS_OR_SPACE_PATTERN_PLACEMENT_COLOR, LocalStrings.PatternPlacementNotEnoughBits.getText());
        }

        if (simulate)
        {
            return PlacementResult.success();
        }

        try (IBatchMutation ignored = areaMutator.batch())
        {
            source.stream().sequential().forEach(
              stateEntryInfo -> {
                  try
                  {
                      areaMutator.setInAreaTarget(
                        stateEntryInfo.getState(),
                        stateEntryInfo.getStartPoint()
                      );
                  }
                  catch (SpaceOccupiedException ignored1)
                  {
                  }
              }
            );
        }

        if (!context.getPlayer().isCreative())
        {
            source.getStatics().getStateCounts().entrySet().stream()
              .filter(e -> !e.getKey().isAir())
              .forEach(e -> playerBitInventory.extract(e.getKey(), e.getValue()));
        }

        return PlacementResult.success();
    }

    @Override
    public Vector3d getTargetedPosition(
      final ItemStack heldStack, final PlayerEntity playerEntity, final BlockRayTraceResult blockRayTraceResult)
    {
        if (playerEntity.isCrouching())
        {
            return blockRayTraceResult.getLocation();
        }

        return Vector3d.atLowerCornerOf(blockRayTraceResult.getBlockPos().offset(blockRayTraceResult.getDirection().getNormal()));
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return new ResourceLocation(
          MOD_ID,
          "textures/icons/pattern_place.png"
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
        return LocalStrings.PatternPlacementModePlacement.getText();
    }
}
