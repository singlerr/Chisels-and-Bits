package mod.chiselsandbits.bittank;

import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockBitTank extends ItemBlock
{

	public ItemBlockBitTank(
			final Block block )
	{
		super( block );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<String> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBitTank, tooltip );
	}
}
