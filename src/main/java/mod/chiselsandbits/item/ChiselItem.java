package mod.chiselsandbits.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.chiseling.ChiselingManager;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
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
import java.util.stream.Collectors;

public class ChiselItem extends ToolItem implements IChiselingItem
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
    public void addInformation(
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


        super.addInformation(stack, worldIn, tooltip, flagIn);
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
        final ItemStack itemStack = playerEntity.getHeldItem(hand);
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
            playerEntity.getCooldownTracker().setCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
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

        final IChiselingContext context = potentiallyExistingContext
                                            .map(IChiselingContext::createSnapshot)
                                            .orElseGet(
                                              () -> IChiselingManager.getInstance().create(
                                                playerEntity,
                                                chiselMode,
                                                ChiselingOperation.CHISELING,
                                                true,
                                                itemStack
                                              )
                                            );

        chiselMode.onLeftClickBy(
          playerEntity,
          context
        );

        if (!context.getMutator().isPresent())
        {
            //No bit was included in the chiseling action
            //We interacted with something we could not chisel
            return;
        }

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        double xView = vector3d.getX();
        double yView = vector3d.getY();
        double zView = vector3d.getZ();

        final BlockPos inWorldStartPos = new BlockPos(context.getMutator().get().getInWorldStartPoint());

        final VoxelShape boundingShape = VoxelShapeManager.getInstance().get(context.getMutator().get(), s -> true);
        WorldRenderer.drawShape(
          matrixStack,
          Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.LINES),
          boundingShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          0.95F, 0.0F, 0.0F, 0.65F
        );
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
        return tier.getMaxUses() * ChiseledBlockEntity.BITS_PER_BLOCK;
    }
}
