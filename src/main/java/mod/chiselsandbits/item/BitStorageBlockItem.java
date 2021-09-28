package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.client.ister.BitStorageISTER;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BitStorageBlockItem extends BlockItem
{

    public BitStorageBlockItem(
      final Block block , Item.Properties builder)
    {
        super( block, builder );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText( stack, worldIn, tooltip, flagIn );
        if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null)
            return;

        FluidStack fluid = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                             .map(h -> h.getFluidInTank(0))
                             .orElse(FluidStack.EMPTY);

        if (fluid.isEmpty()) {
            Configuration.getInstance().getCommon().helpText( LocalStrings.HelpBitTankEmpty, tooltip );
        }
        else
        {
            Configuration.getInstance().getCommon().helpText( LocalStrings.HelpBitTankFilled, tooltip,
              new TranslatableComponent(fluid.getTranslationKey()),
              String.valueOf((int) Math.floor(fluid.getAmount() * 4.096)));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundTag nbt)
    {
        return new FluidHandlerItemStack(stack, FluidAttributes.BUCKET_VOLUME);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
      final @NotNull BlockPos pos, final @NotNull Level worldIn, @Nullable final Player player, final @NotNull ItemStack stack, final @NotNull BlockState state)
    {
        super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
        if (worldIn.isClientSide)
            return false;

        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof final BitStorageBlockEntity tileEntityBitStorage))
            return false;

        tileEntityBitStorage
          .getCapability(
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
          )
          .ifPresent(t -> t
                            .fill(
                              stack.getCapability(
                                CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                              )
                                .map(s ->s
                                           .drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE)
                                )
                                .orElse(FluidStack.EMPTY),
                              IFluidHandler.FluidAction.EXECUTE
                            )
          );

        return true;
    }

    @Override
    public void initializeClient(final Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer()
            {
                return new BitStorageISTER();
            }
        });
    }
}