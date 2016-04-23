package mod.chiselsandbits.blueprints;

import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketOpenGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBlueprintBlank extends Item
{

	public ItemBlueprintBlank()
	{
		setMaxStackSize( 1 );
	}

	@Override
	public ItemStack onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn )
	{
		if ( worldIn.isRemote )
		{
			NetworkRouter.instance.sendToServer( new PacketOpenGui( ModGuiTypes.Blueprint ) );
		}

		return itemStackIn;
	}

}
