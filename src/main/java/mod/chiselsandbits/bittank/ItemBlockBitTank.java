package mod.chiselsandbits.bittank;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockBitTank extends BlockItem
{

	public ItemBlockBitTank(
			final Block block , Item.Properties builder)
	{
		super( block, builder );
	}

    @Override
    public void addInformation(
      final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
    {
        super.addInformation( stack, worldIn, tooltip, flagIn );
        ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitTank, tooltip );
    }
}
