package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.container.BagContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class OpenBagGuiPacket extends ModPacket
{
    public OpenBagGuiPacket(FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public OpenBagGuiPacket()
    {
    }

    @Override
	public void server(
			final ServerPlayer player )
	{
	    player.openMenu(new SimpleMenuProvider(
          (id, playerInventory, playerEntity) -> new BagContainer(id, playerInventory),
          new TranslatableComponent(LocalStrings.ContainerBitBag.toString())
        ));
	}

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {

    }

    @Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		// no data..
	}

}
