package mod.chiselsandbits.items;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static net.minecraft.item.ItemTier.*;

public class ItemBitSaw extends Item
{

	public ItemBitSaw(IItemTier tier, Item.Properties properties)
	{
        super(setupDamageStack(tier, properties.maxStackSize(1)));
	}


    private static Item.Properties setupDamageStack(IItemTier material, Item.Properties properties) {
        long uses = 1;
        if (DIAMOND.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().diamondSawUses.get();
        }
        else if (GOLD.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().goldSawUses.get();
        }
        else if (IRON.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().ironSawUses.get();
        }
        else if (STONE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().stoneSawUses.get();
        }
        else if (NETHERITE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().netheriteSawUses.get();
        }

        return properties.maxDamage(ChiselsAndBits.getConfig().getServer().damageTools.get() ? (int) Math.max( 0, uses ) : 0);
    }

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitSaw, tooltip );
	}

	@Override
	public ItemStack getContainerItem(
			final ItemStack itemStack )
	{
		if ( ChiselsAndBits.getConfig().getServer().damageTools.get() )
		{
			itemStack.setDamage( itemStack.getDamage() + 1 );
			if (itemStack.getDamage() == itemStack.getMaxDamage())
            {
                return ItemStack.EMPTY;
            }
		}

		return itemStack.copy();
	}

	@Override
	public boolean hasContainerItem()
	{
		return true;
	}

}
