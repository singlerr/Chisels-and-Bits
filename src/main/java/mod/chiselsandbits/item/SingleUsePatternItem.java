package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.pattern.placement.PlacementResult;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModPatternPlacementTypes;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SingleUsePatternItem extends Item implements IPatternItem
{

    public SingleUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    @Override
    public ITextComponent getHighlightTip(final ItemStack item, final ITextComponent displayName)
    {
        if (!item.getOrCreateTag().contains("highlight"))
            return super.getHighlightTip(item, displayName);

        final String highlightTextJson = item.getOrCreateTag().getString("highlight");
        return ITextComponent.Serializer.fromJson(highlightTextJson).withStyle(TextFormatting.RED);
    }

    /**
     * Creates an itemstack aware context wrapper that gives access to the multistate information contained within the given itemstack.
     *
     * @param stack The stack to get an {@link IMultiStateItemStack} for.
     * @return The {@link IMultiStateItemStack} that represents the data in the given itemstack.
     */
    @NotNull
    @Override
    public IMultiStateItemStack createItemStack(final ItemStack stack)
    {
        //Take care of an empty pattern.
        //Generally the case when this is a stack from the creative menu.
        if (stack.getOrCreateTag().isEmpty()) {
            return EmptySnapshot.Stack.INSTANCE;
        }

        return new SingleBlockMultiStateItemStack(stack);
    }

    @Override
    @NotNull
    public ActionResultType useOn(@NotNull ItemUseContext context) {
        final IMultiStateItemStack contents = createItemStack(context.getItemInHand());
        if (contents.getStatistics().isEmpty()) {
            if (context.getPlayer() == null)
                return ActionResultType.FAIL;

            if (!context.getPlayer().isCreative())
                return ActionResultType.FAIL;

            if (!context.getPlayer().isCrouching())
                return ActionResultType.FAIL;

            final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getLevel(), context.getClickedPos());
            final ItemStack snapshotPatternStack = areaMutator.createSnapshot().toItemStack().toPatternStack();
            context.getItemInHand().setTag(snapshotPatternStack.getOrCreateTag().copy());
            return ActionResultType.SUCCESS;
        }


        return this.tryPlace(new BlockItemUseContext(context));
    }

    @NotNull
    public ActionResultType tryPlace(@NotNull final BlockItemUseContext context)
    {
        if (context.getPlayer() == null)
            return ActionResultType.FAIL;

        final IAreaAccessor source = this.createItemStack(context.getItemInHand());
        final IMultiStateSnapshot sourceSnapshot = source.createSnapshot();

        final PlacementResult resultType = getMode(context.getItemInHand())
          .performPlacement(
            sourceSnapshot,
            context,
            false);

        final ItemStack resultingStack = context.getItemInHand().copy();

        if (!resultType.isSuccess()) {
            resultingStack.getOrCreateTag().putString("highlight",
                ITextComponent.Serializer.toJson(resultType.getFailureMessage())
            );
            resultingStack.getOrCreateTag().putLong("highlightStartTime",
              context.getPlayer().level.getGameTime()
            );
        }
        else
        {
            resultingStack.getOrCreateTag().remove("highlight");
            resultingStack.getOrCreateTag().remove("highlightStartTime");
        }

        context.getPlayer().setItemInHand(context.getHand(), resultingStack);

        return resultType.isSuccess() ?
                 determineSuccessResult(context, resultingStack) :
                 ActionResultType.FAIL;
    }

    protected ActionResultType determineSuccessResult(final BlockItemUseContext context, final ItemStack stack) {
        if (context.getPlayer() != null && context.getPlayer().isCreative())
        {
            return ActionResultType.SUCCESS;
        }

        stack.shrink(1);
        return ActionResultType.CONSUME;
    }

    @Override
    public @NotNull ItemStack seal(@NotNull final ItemStack source) throws SealingNotSupportedException
    {
        if (source.getItem() == this)
        {
            if (!(source.getItem() instanceof IMultiUsePatternItem))
            {
                final ItemStack seal = new ItemStack(ModItems.MULTI_USE_PATTERN_ITEM.get());
                seal.setTag(source.getOrCreateTag().copy());
                return seal;
            }

            throw new SealingNotSupportedException();
        }

        return source;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        final IPatternPlacementType mode = getMode(stack);
        if (mode.getGroup().isPresent())
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeGrouped.getText(mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeSimple.getText(mode.getDisplayName()));
        }

        if ((Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(new StringTextComponent("        "));
            tooltip.add(new StringTextComponent("        "));

            Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSimplePattern, tooltip);
        }
    }

    @Override
    public @NotNull IPatternPlacementType getMode(final ItemStack stack)
    {
        return ModPatternPlacementTypes.REGISTRY_SUPPLIER.get().getValue(
          stack.getOrCreateTag().contains("mode") ?
            new ResourceLocation(stack.getOrCreateTag().getString("mode")) :
            ModPatternPlacementTypes.PLACEMENT.getId()
        );
    }

    @Override
    public void setMode(final ItemStack stack, final IPatternPlacementType mode)
    {
        if (mode == null)
            return;

        stack.getOrCreateTag().putString("mode", mode.getRegistryName().toString());
    }

    @Override
    public @NotNull Collection<IPatternPlacementType> getPossibleModes()
    {
        return ModPatternPlacementTypes.REGISTRY_SUPPLIER.get().getValues();
    }
}
