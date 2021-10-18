package mod.chiselsandbits.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.ILocalChiselingContextCache;
import mod.chiselsandbits.api.item.chisel.IChiselItem;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.StateEntryPredicates;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.chiseling.ChiselingManager;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChiselItem extends ToolItem implements IChiselItem
{

    private static final Logger LOGGER = LogManager.getLogger();

    public ChiselItem(
      final IItemTier tier,
      final Properties builderIn)
    {
        super(
          0.1F,
          -2.8F,
          tier,
          ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).collect(Collectors.toSet()),
          builderIn
        );
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IChiselMode mode = getMode(stack);
        if (mode.getGroup().isPresent())
        {
            tooltip.add(TranslationUtils.build("chiselmode.mode_grouped", mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else
        {
            tooltip.add(TranslationUtils.build("chiselmode.mode", mode.getDisplayName()));
        }


        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        final CompoundNBT stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.CHISEL_MODE))
        {
            final String chiselModeName = stackNbt.getString(NbtConstants.CHISEL_MODE);
            try
            {
                final IChiselMode registryMode = IChiselMode.getRegistry().getValue(new ResourceLocation(chiselModeName));
                if (registryMode == null)
                {
                    return IChiselMode.getDefaultMode();
                }

                return registryMode;
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                LOGGER.error(String.format("An ItemStack got loaded with a name that is not a valid chisel mode: %s", chiselModeName));
                this.setMode(stack, IChiselMode.getDefaultMode());
            }
        }

        return IChiselMode.getDefaultMode();
    }

    @Override
    public void setMode(final ItemStack stack, final IChiselMode mode)
    {
        stack.getOrCreateTag().putString(NbtConstants.CHISEL_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Collection<IChiselMode> getPossibleModes()
    {
        return IChiselMode.getRegistry()
                 .getValues()
                 .stream()
                 .filter(mode -> !mode.requiresPlaceableEditStack())
                 .sorted(Comparator.comparing(((ForgeRegistry<IChiselMode>) IChiselMode.getRegistry())::getID))
                 .collect(Collectors.toList());
    }

    @Override
    public ClickProcessingState handleLeftClickProcessing(
      final PlayerEntity playerEntity,
      final Hand hand,
      final BlockPos position,
      final Direction face,
      final ClickProcessingState currentState
    )
    {
        final ItemStack itemStack = playerEntity.getItemInHand(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
        {
            return currentState;
        }

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final IChiselingContext context = IChiselingManager.getInstance().getOrCreateContext(
          playerEntity,
          chiselMode,
          ChiselingOperation.CHISELING,
          false,
          itemStack);

        final ClickProcessingState resultState = chiselMode.onLeftClickBy(
          playerEntity,
          context
        );

        if (context.isComplete())
        {
            playerEntity.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
        }

        return resultState;
    }

    @Override
    public boolean canUse(final PlayerEntity playerEntity)
    {
        return ChiselingManager.getInstance().canChisel(playerEntity);
    }

    @Override
    public boolean shouldDrawDefaultHighlight(@NotNull final PlayerEntity playerEntity)
    {
        final ItemStack itemStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
        {
            return true;
        }

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final Optional<IChiselingContext> potentiallyExistingContext =
          IChiselingManager.getInstance().get(playerEntity, chiselMode, ChiselingOperation.CHISELING);
        if (potentiallyExistingContext.isPresent())
        {
            final IChiselingContext context = potentiallyExistingContext.get();

            if (context.getMutator().isPresent())
            {
                return false;
            }

            final IChiselingContext currentContextSnapshot = context.createSnapshot();
            chiselMode.onLeftClickBy(
              playerEntity,
              currentContextSnapshot
            );

            return !currentContextSnapshot.getMutator().isPresent();
        }

        final Optional<IChiselingContext> localCachedContext = ILocalChiselingContextCache
                                                                 .getInstance()
                                                                 .get(ChiselingOperation.CHISELING);

        if (localCachedContext.isPresent()
          &&
              localCachedContext.get().getMode().isStillValid(playerEntity, localCachedContext.get(), ChiselingOperation.CHISELING)
        )
        {
            final IChiselingContext context = localCachedContext.get();

            if (
              context.getMode() == chiselMode
            )

            if (context.getMutator().isPresent())
            {
                return false;
            }

            return !context.getMutator().isPresent();
        }

        final IChiselingContext context = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.CHISELING,
          true,
          itemStack);

        //We try a left click render first.
        chiselMode.onLeftClickBy(
          playerEntity,
          context
        );

        //Store it in the local cache.
        ILocalChiselingContextCache.getInstance().set(ChiselingOperation.CHISELING, context);

        return !context.getMutator().isPresent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHighlight(
      final PlayerEntity playerEntity,
      final WorldRenderer worldRenderer,
      final MatrixStack matrixStack,
      final float partialTicks,
      final Matrix4f projectionMatrix,
      final long finishTimeNano)
    {
        final ItemStack itemStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
        {
            return;
        }

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final Optional<IChiselingContext> potentiallyExistingContext =
          IChiselingManager.getInstance().get(playerEntity, chiselMode, ChiselingOperation.CHISELING);

        final Optional<IChiselingContext> potentiallyCachedContext = ILocalChiselingContextCache.getInstance()
                                                                       .get(ChiselingOperation.CHISELING);
        IChiselingContext context;
        if (potentiallyExistingContext.isPresent()) {
            context = potentiallyExistingContext.get();

            chiselMode.onLeftClickBy(
              playerEntity,
              context
            );
        }
        else if (potentiallyCachedContext.isPresent()
                   && potentiallyCachedContext.get().getMode() == chiselMode
                   && potentiallyCachedContext.get().getModeOfOperandus() == ChiselingOperation.CHISELING
                   && chiselMode.isStillValid(playerEntity, potentiallyCachedContext.get(), ChiselingOperation.CHISELING)) {
            context = potentiallyCachedContext.get();
        }
        else
        {
            context =  IChiselingManager.getInstance().create(
              playerEntity,
              chiselMode,
              ChiselingOperation.CHISELING,
              true,
              itemStack
            );

            chiselMode.onLeftClickBy(
              playerEntity,
              context
            );
        }

        if (!context.getMutator().isPresent())
        {
            ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.CHISELING);
            //No bit was included in the chiseling action
            //We interacted with something we could not chisel
            return;
        }

        ILocalChiselingContextCache.getInstance().set(ChiselingOperation.CHISELING, context);

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        final BlockPos inWorldStartPos = new BlockPos(context.getMutator().get().getInWorldStartPoint());

        final VoxelShape boundingShape = VoxelShapeManager.getInstance()
                                           .get(context.getMutator().get(),
                                             areaAccessor -> {
                                                 final Predicate<IStateEntryInfo> contextPredicate = context.getStateFilter()
                                                                                                       .map(factory -> factory.apply(areaAccessor))
                                                                                                       .orElse(StateEntryPredicates.NOT_AIR);

                                                 return new InternalContextFilter(contextPredicate);
                                             },
                                             false
                                             );

        RenderSystem.disableDepthTest();
        WorldRenderer.renderShape(
          matrixStack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
          boundingShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          0.95F, 0.0F, 0.0F, 0.65F
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean isDamageableDuringChiseling()
    {
        return true;
    }

    @Override
    public int getMaxDamage(final ItemStack stack)
    {
        final IItemTier tier = getTier();
        return tier.getUses() * StateEntrySize.current().getBitsPerBlock();
    }


    private static final class InternalContextFilter implements Predicate<IStateEntryInfo> {

        private final Predicate<IStateEntryInfo> placingContextPredicate;

        private InternalContextFilter(final Predicate<IStateEntryInfo> placingContextPredicate) {this.placingContextPredicate = placingContextPredicate;}

        @Override
        public boolean test(final IStateEntryInfo s)
        {
            return (s.getState().isAir() || IEligibilityManager.getInstance().canBeChiseled(s.getState())) && placingContextPredicate.test(s);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof InternalContextFilter))
            {
                return false;
            }

            final InternalContextFilter that = (InternalContextFilter) o;

            return Objects.equals(placingContextPredicate, that.placingContextPredicate);
        }

        @Override
        public int hashCode()
        {
            return placingContextPredicate != null ? placingContextPredicate.hashCode() : 0;
        }
    }
}
