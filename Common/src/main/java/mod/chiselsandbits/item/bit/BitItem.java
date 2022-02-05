package mod.chiselsandbits.item.bit;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.ILocalChiselingContextCache;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRendererRegistry;
import mod.chiselsandbits.api.config.IServerConfiguration;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.documentation.IDocumentableItem;
import mod.chiselsandbits.chiseling.LocalChiselingContextCache;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import mod.chiselsandbits.chiseling.ChiselingManager;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.CHISELING, IChiselMode::onLeftClickBy
        );
    }
    
    @Override
    public void onLeftClickProcessingEnd(final Player player, final ItemStack stack)
    {
        final IChiselMode chiselMode = getMode(stack);
        Optional<IChiselingContext> context = IChiselingManager.getInstance().get(
          player,
          chiselMode,
          ChiselingOperation.CHISELING);

        if (context.isEmpty()) {
            context = LocalChiselingContextCache.getInstance().get(ChiselingOperation.CHISELING);
        }

        context.ifPresent(c -> {
            chiselMode.onStoppedLeftClicking(player, c);
            if (c.isComplete()) {
                player.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
                LocalChiselingContextCache.getInstance().clear(ChiselingOperation.CHISELING);
            }
        });
    }


    @Override
    public boolean canUse(final Player playerEntity)
    {
        return ChiselingManager.getInstance().canChisel(playerEntity);
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        final CompoundTag stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.CHISEL_MODE))
        {
            final String chiselModeName = stackNbt.getString(NbtConstants.CHISEL_MODE);
            try {
                final Optional<IChiselMode> registryMode = IChiselMode.getRegistry().get(new ResourceLocation(chiselModeName));
                if (registryMode.isEmpty())
                {
                    return IChiselMode.getDefaultMode();
                }

                return registryMode.get();
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
    public Component getName(@NotNull final ItemStack stack)
    {
        final BlockState containedStack = getBitState(stack);
        final Block block = containedStack.getBlock();

        Component stateName = block.asItem().getName(new ItemStack(block));
        if (block instanceof LiquidBlock) {
            final LiquidBlock flowingFluidBlock = (LiquidBlock) block;
            stateName = IFluidManager.getInstance().getDisplayName(flowingFluidBlock.getFluidState(flowingFluidBlock.defaultBlockState()).getType());
        }

        return new TranslatableComponent(this.getDescriptionId(stack), stateName);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
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
        return IChiselMode.getRegistry().getValues().stream().sorted(Comparator.comparing(IChiselMode::getRegistryName)).collect(Collectors.toList());
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.PLACING, IChiselMode::onRightClickBy
        );
    }

    @Override
    public void onRightClickProcessingEnd(final Player player, final ItemStack stack)
    {
        final IChiselMode chiselMode = getMode(stack);
        Optional<IChiselingContext> context = IChiselingManager.getInstance().get(
          player,
          chiselMode,
          ChiselingOperation.PLACING);

        if (context.isEmpty()) {
            context = LocalChiselingContextCache.getInstance().get(ChiselingOperation.PLACING);
        }

        context.ifPresent(c -> {
            chiselMode.onStoppedRightClicking(player, c);
            if (c.isComplete()) {
                player.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
                LocalChiselingContextCache.getInstance().clear(ChiselingOperation.PLACING);
            }
        });
    }

    private ClickProcessingState handleClickProcessing(
      final Player playerEntity,
      final InteractionHand hand,
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
            LocalChiselingContextCache.getInstance().clear(modeOfOperation);
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
            stack.getOrCreateTag().put(NbtConstants.BLOCK_STATE, NbtUtils.writeBlockState(blockState));
        }
        return NbtUtils.readBlockState(stack.getOrCreateTagElement(NbtConstants.BLOCK_STATE));
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


    //TODO: Figure this out!
/*    @Override
    public int getItemStackLimit(final ItemStack stack)
    {
        if (this.threadLocalBitMergeOperationInProgress.get())
            return Configuration.getInstance().getServer().bagStackSize.get();

        return super.getItemStackLimit(stack);
    }*/

    @Override
    public boolean shouldDrawDefaultHighlight(@NotNull final Player playerEntity)
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
    public void renderHighlight(
      final Player playerEntity,
      final LevelRenderer worldRenderer,
      final PoseStack matrixStack,
      final float partialTicks)
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

        boolean renderedSomething = false;

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
        } else if (potentialChiselingContext.isPresent()) {
            final IChiselingContext chiselingContext = potentialChiselingContext.get();
            if (potentialChiselingContext.get().getMode() == chiselMode
                && chiselingContext.getMode().isStillValid(playerEntity, chiselingContext, ChiselingOperation.CHISELING))
            {
                IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                  .renderExistingContextsBoundingBox(matrixStack, chiselingContext);
            }
            else
            {
                ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.CHISELING);
            }

            if (potentialPlacingContext.isPresent()) {
                final IChiselingContext placingContext = potentialPlacingContext.get();
                if (placingContext.getMode() == chiselMode &&
                      potentialPlacingContext.get().getMode().isStillValid(playerEntity, potentialPlacingContext.get(), ChiselingOperation.PLACING))
                {
                    IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                      .renderExistingContextsBoundingBox(matrixStack, placingContext);
                }
                else
                {
                    ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.PLACING);
                }
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
        ClickProcessingState run(final IChiselMode chiselMode, final Player playerEntity, final IChiselingContext context);
    }

    @Override
    public void fillItemCategory(@Nullable final CreativeModeTab group, @NotNull final NonNullList<ItemStack> items)
    {
        if (group == null || this.getItemCategory() != group) {
            return;
        }

        if (availableBitStacks.isEmpty()) {
            IPlatformRegistryManager.getInstance().getBlockRegistry().getValues()
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
        return IPlatformRegistryManager.getInstance().getBlockRegistry().getValues()
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
            stack -> "bit_" + IPlatformRegistryManager.getInstance().getBlockRegistry().getKey(this.getBitState(stack).getBlock()).toString().replace(":", "_"),
            Function.identity()
          ));
    }
}
