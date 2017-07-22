package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBitSaw extends Item
{

	public ItemBitSaw()
	{
		setMaxStackSize( 1 );

		final int uses = ChiselsAndBits.getConfig().diamondSawUses;
		setMaxDamage( ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, uses ) : 0 );
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
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBitSaw, tooltip );
	}

	@Override
	public ItemStack getContainerItem(
			final ItemStack itemStack )
	{
		if ( ChiselsAndBits.getConfig().damageTools )
		{
			itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		}

		return itemStack.copy();
	}

	@Override
	public boolean hasContainerItem()
	{
		return true;
	}

}
