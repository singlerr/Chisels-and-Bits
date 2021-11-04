package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;

public final class BagGuiPacket extends ModPacket
{
	private int slotNumber = -1;
	private int mouseButton = -1;
	private boolean duplicateButton = false;
	private boolean holdingShift = false;

    public BagGuiPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public BagGuiPacket(final int slotNumber, final int mouseButton, final boolean duplicateButton, final boolean holdingShift)
    {
        this.slotNumber = slotNumber;
        this.mouseButton = mouseButton;
        this.duplicateButton = duplicateButton;
        this.holdingShift = holdingShift;
    }

    @Override
	public void server(
			final ServerPlayer player )
	{
		doAction( player );
	}

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeInt( slotNumber );
        buffer.writeInt( mouseButton );
        buffer.writeBoolean( duplicateButton );
        buffer.writeBoolean( holdingShift );
    }

    public void doAction(
			final Player player )
	{
		final AbstractContainerMenu c = player.containerMenu;
		if ( c instanceof BagContainer)
		{
			final BagContainer bc = (BagContainer) c;
			bc.handleCustomSlotAction( slotNumber, mouseButton, duplicateButton, holdingShift );
		}
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
        slotNumber = buffer.readInt();
        mouseButton = buffer.readInt();
        duplicateButton = buffer.readBoolean();
        holdingShift = buffer.readBoolean();
	}

}
