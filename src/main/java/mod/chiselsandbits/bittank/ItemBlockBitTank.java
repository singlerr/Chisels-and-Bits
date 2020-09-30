package mod.chiselsandbits.bittank;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockBitTank extends BlockItem
{

	public ItemBlockBitTank(
			final Block block , Item.Properties builder)
	{
		super( block, builder );
	}

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(
      final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
    {
        super.addInformation( stack, worldIn, tooltip, flagIn );
        if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null)
            return;

        FluidStack fluid = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                        .map(h -> h.getFluidInTank(0))
                        .orElse(FluidStack.EMPTY);

        if (fluid.isEmpty()) {
            ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitTankEmpty, tooltip );
        }
        else
        {
            ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitTankFilled, tooltip, DeprecationHelper.translateToLocal(fluid.getTranslationKey()),
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
    protected boolean onBlockPlaced(
      final BlockPos pos, final World worldIn, @Nullable final PlayerEntity player, final ItemStack stack, final BlockState state)
    {
         super.onBlockPlaced(pos, worldIn, player, stack, state);
         if (worldIn.isRemote)
             return false;

         final TileEntity tileEntity = worldIn.getTileEntity(pos);
         if (!(tileEntity instanceof TileEntityBitTank))
             return false;

         final TileEntityBitTank tileEntityBitTank = (TileEntityBitTank) tileEntity;
         tileEntityBitTank
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
