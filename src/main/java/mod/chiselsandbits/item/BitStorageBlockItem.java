package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.DeprecationHelper;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
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
              new TranslationTextComponent(fluid.getTranslationKey()),
              String.valueOf((int) Math.floor(fluid.getAmount() * 4.096)));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundNBT nbt)
    {
        return new FluidHandlerItemStack(stack, FluidAttributes.BUCKET_VOLUME);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
      final @NotNull BlockPos pos, final @NotNull World worldIn, @Nullable final PlayerEntity player, final @NotNull ItemStack stack, final @NotNull BlockState state)
    {
        super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
        if (worldIn.isClientSide)
            return false;

        final TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof BitStorageBlockEntity))
            return false;

        final BitStorageBlockEntity tileEntityBitStorage = (BitStorageBlockEntity) tileEntity;
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
}