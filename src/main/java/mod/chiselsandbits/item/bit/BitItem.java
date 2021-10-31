package mod.chiselsandbits.item.bit;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.ILocalChiselingContextCache;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRendererRegistry;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.documentation.IDocumentableItem;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.chiseling.ChiselingManager;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static mod.chiselsandbits.api.util.StateEntryPredicates.ALL;
import static mod.chiselsandbits.api.util.StateEntryPredicates.NOT_AIR;

public class BitItem extends Item implements IChiselingItem, IBitItem, IDocumentableItem
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String LEGACY_BLOCK_STATE_ID_KEY = "id";

    private final List<ItemStack> availableBitStacks = Lists.newLinkedList();

    private final ThreadLocal<Boolean> threadLocalBitMergeOperationInProgress = ThreadLocal.withInitial(() -> false);

    public BitItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public ClickProcessingState handleLeftClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.CHISELING, IChiselMode::onLeftClickBy
        );
    }

    @Override
    public boolean canUse(final PlayerEntity playerEntity)
    {
        return ChiselingManager.getInstance().canChisel(playerEntity);
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        final CompoundNBT stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.CHISEL_MODE))
        {
            final String chiselModeName = stackNbt.getString(NbtConstants.CHISEL_MODE);
            try {
                final IChiselMode registryMode = IChiselMode.getRegistry().getValue(new ResourceLocation(chiselModeName));
                if (registryMode == null)
                {
                    return IChiselMode.getDefaultMode();
                }

                return registryMode;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error(String.format("An ItemStack got loaded with a name that is not a valid chisel mode: %s", chiselModeName));
                this.setMode(stack, IChiselMode.getDefaultMode());
            }
        }

        return IChiselMode.getDefaultMode();
    }

    @NotNull
    @Override
    public ITextComponent getName(@NotNull final ItemStack stack)
    {
        final BlockState containedStack = getBitState(stack);
        final Block block = containedStack.getBlock();

        ITextComponent stateName = block.asItem().getName(new ItemStack(block));
        if (block instanceof FlowingFluidBlock) {
            final FlowingFluidBlock flowingFluidBlock = (FlowingFluidBlock) block;
            stateName = new TranslationTextComponent(flowingFluidBlock.getFluid().getAttributes().getTranslationKey());
        }

        return new TranslationTextComponent(this.getDescriptionId(stack), stateName);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IChiselMode mode = getMode(stack);
        if (mode.getGroup().isPresent()) {
            tooltip.add(TranslationUtils.build("chiselmode.mode_grouped", mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else {
            tooltip.add(TranslationUtils.build("chiselmode.mode", mode.getDisplayName()));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void setMode(final ItemStack stack, final IChiselMode mode)
    {
        if (mode == null)
            return;

        stack.getOrCreateTag().putString(NbtConstants.CHISEL_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Collection<IChiselMode> getPossibleModes()
    {
        return IChiselMode.getRegistry().getValues().stream().sorted(Comparator.comparing(((ForgeRegistry<IChiselMode>) IChiselMode.getRegistry())::getID)).collect(Collectors.toList());
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.PLACING, IChiselMode::onRightClickBy
        );
    }

    private ClickProcessingState handleClickProcessing(
      final PlayerEntity playerEntity,
      final Hand hand,
      final ClickProcessingState currentState,
      final ChiselingOperation modeOfOperation,
      final ChiselModeInteractionCallback callback)
    {
        final ItemStack itemStack = playerEntity.getItemInHand(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
            return currentState;

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final IChiselingContext context = IChiselingManager.getInstance().getOrCreateContext(
          playerEntity,
          chiselMode,
          modeOfOperation,
          false,
          itemStack);

        final ClickProcessingState resultState = callback.run(chiselMode, playerEntity, context);

        if (context.isComplete()) {
            playerEntity.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
        }

        return resultState;
    }

    @Override
    public BlockState getBitState(final ItemStack stack)
    {
        //TODO: 1.17 Remove the legacy loading of the blockstate.
        if (!stack.getOrCreateTag().contains(NbtConstants.BLOCK_STATE)) {
            if (!stack.getOrCreateTag().contains(LEGACY_BLOCK_STATE_ID_KEY)) {
                return Blocks.AIR.defaultBlockState();
            }

            final BlockState blockState = IBlockStateIdManager.getInstance().getBlockStateFrom(stack.getOrCreateTag().getInt(LEGACY_BLOCK_STATE_ID_KEY));
            stack.getOrCreateTag().remove(LEGACY_BLOCK_STATE_ID_KEY);
            stack.getOrCreateTag().put(NbtConstants.BLOCK_STATE, NBTUtil.writeBlockState(blockState));
        }
        return NBTUtil.readBlockState(stack.getOrCreateTagElement(NbtConstants.BLOCK_STATE));
    }

    @Override
    public void onMergeOperationWithBagBeginning()
    {
        this.threadLocalBitMergeOperationInProgress.set(true);
    }

    @Override
    public void onMergeOperationWithBagEnding()
    {
        this.threadLocalBitMergeOperationInProgress.set(false);
    }

    @Override
    public int getItemStackLimit(final ItemStack stack)
    {
        if (this.threadLocalBitMergeOperationInProgress.get())
            return Configuration.getInstance().getServer().bagStackSize.get();

        return super.getItemStackLimit(stack);
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

            if (currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING)
            {
                chiselMode.onLeftClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }
            else
            {
                chiselMode.onRightClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }

            return !currentContextSnapshot.getMutator().isPresent();
        }

        final Optional<IChiselingContext> localCachedContext = ILocalChiselingContextCache
                                                                 .getInstance()
                                                                 .get(ChiselingOperation.CHISELING);

        if (localCachedContext.isPresent())
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

        if (context.getMutator().isPresent())
            return false;

        chiselMode.onRightClickBy(
          playerEntity,
          context
        );

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
            return;

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final Optional<IChiselingContext> potentiallyExistingContext =
          IChiselingManager.getInstance().get(playerEntity, chiselMode);


        final Optional<IChiselingContext> potentialChiselingContext = ILocalChiselingContextCache.getInstance()
                                                                       .get(ChiselingOperation.CHISELING);

        final Optional<IChiselingContext> potentialPlacingContext = ILocalChiselingContextCache.getInstance()
          .get(ChiselingOperation.PLACING);

        if (potentiallyExistingContext.isPresent()) {
            final IChiselingContext currentContextSnapshot = potentiallyExistingContext.get().createSnapshot();

            if (currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING) {
                chiselMode.onLeftClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }
            else
            {
                chiselMode.onRightClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }

            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                                                                 .renderExistingContextsBoundingBox(matrixStack, currentContextSnapshot);
            return;
        }
        else if (potentialChiselingContext.isPresent()
                   && potentialChiselingContext.get().getMode() == chiselMode
                   && chiselMode.isStillValid(playerEntity, potentialChiselingContext.get(), ChiselingOperation.CHISELING)) {

            final IChiselingContext chiselingContext = potentialChiselingContext.get();

            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, chiselingContext);

            if (potentialPlacingContext.isPresent()
                  && potentialPlacingContext.get().getMode() == chiselMode
                  && chiselMode.isStillValid(playerEntity, potentialPlacingContext.get(), ChiselingOperation.PLACING)) {

                final IChiselingContext placingContext = potentialPlacingContext.get();

                IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                  .renderExistingContextsBoundingBox(matrixStack, placingContext);
            }
            return;
        }
        else if (potentialPlacingContext.isPresent()
                   && potentialPlacingContext.get().getMode() == chiselMode
                   && chiselMode.isStillValid(playerEntity, potentialPlacingContext.get(), ChiselingOperation.PLACING)) {

            final IChiselingContext context = potentialPlacingContext.get();

            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, context);
            return;
        }

        final IChiselingContext chiselingContext = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.CHISELING,
          true,
          itemStack
        );
        final IChiselingContext placingContext = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.PLACING,
          true,
          itemStack
        );

        chiselMode.onLeftClickBy(
          playerEntity,
          chiselingContext
        );
        chiselMode.onRightClickBy(
          playerEntity,
          placingContext
        );

        RenderSystem.disableDepthTest();
        if (chiselingContext.getMutator().isPresent()) {
            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, chiselingContext);
            ILocalChiselingContextCache.getInstance().set(ChiselingOperation.CHISELING, chiselingContext);
        }
        if (placingContext.getMutator().isPresent()) {
            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, placingContext);
            ILocalChiselingContextCache.getInstance().set(ChiselingOperation.PLACING, placingContext);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean isDamageableDuringChiseling()
    {
        return false;
    }

    @FunctionalInterface
    private interface ChiselModeInteractionCallback {
        ClickProcessingState run(final IChiselMode chiselMode, final PlayerEntity playerEntity, final IChiselingContext context);
    }

    @Override
    public void fillItemCategory(@Nullable final ItemGroup group, @NotNull final NonNullList<ItemStack> items)
    {
        if (group == null || this.getItemCategory() != group) {
            return;
        }

        if (availableBitStacks.isEmpty()) {
            ForgeRegistries.BLOCKS.getValues()
              .forEach(block -> {
                  if (IEligibilityManager.getInstance().canBeChiseled(block)) {
                    final BlockState blockState = block.defaultBlockState();
                    final ItemStack resultStack = IBitItemManager.getInstance().create(blockState);

                    if (!resultStack.isEmpty() && resultStack.getItem() instanceof IBitItem)
                        this.availableBitStacks.add(resultStack);
                  }
              });

            availableBitStacks.sort(Comparator.comparing(stack -> {
                if (!(stack.getItem() instanceof IBitItem))
                    throw new IllegalStateException("Stack did not contain a bit item.");

                return IBlockStateIdManager.getInstance().getIdFrom(((IBitItem) stack.getItem()).getBitState(stack));
            }));
        }

        items.addAll(availableBitStacks);
    }

    @Override
    public Map<String, ItemStack> getDocumentableInstances(final Item item)
    {
        return ForgeRegistries.BLOCKS.getValues()
          .stream()
          .map(block -> {
              if (IEligibilityManager.getInstance().canBeChiseled(block)) {
                  final BlockState blockState = block.defaultBlockState();
                  return IBitItemManager.getInstance().create(blockState);
              }

              return ItemStack.EMPTY;
          })
          .filter(stack -> !stack.isEmpty())
          .collect(Collectors.toMap(
            stack -> "bit_" + this.getBitState(stack).getBlock().getRegistryName().toString().replace(":", "_"),
            Function.identity()
          ));
    }
}
