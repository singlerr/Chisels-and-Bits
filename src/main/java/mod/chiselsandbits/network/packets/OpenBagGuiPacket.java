package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.container.BagContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class OpenBagGuiPacket extends ModPacket
{
    public OpenBagGuiPacket(PacketBuffer buffer)
    {
        super(buffer);
    }

    public OpenBagGuiPacket()
    {
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
	    player.openContainer(new SimpleNamedContainerProvider(
          (id, playerInventory, playerEntity) -> new BagContainer(id, playerInventory),
          new TranslationTextComponent(LocalStrings.ContainerBitBag.toString())
        ));
	}

    @Override
    public void writePayload(final PacketBuffer buffer)
    {

    }

    @Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		// no data..
	}

}
