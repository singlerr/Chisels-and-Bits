package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.Nullable;

public class PacketOpenBagGui extends ModPacket
{
    public PacketOpenBagGui(PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketOpenBagGui()
    {
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
	    player.openContainer(new SimpleNamedContainerProvider(
          (id, playerInventory, playerEntity) -> new BagContainer(id, playerInventory),
          new StringTextComponent("Bitbag")
        ));
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		// no data...
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		// no data..
	}

}
