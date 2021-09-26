package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public final class BagGuiPacket extends ModPacket
{
	private int slotNumber = -1;
	private int mouseButton = -1;
	private boolean duplicateButton = false;
	private boolean holdingShift = false;

    public BagGuiPacket(final PacketBuffer buffer)
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
			final ServerPlayerEntity player )
	{
		doAction( player );
	}

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeInt( slotNumber );
        buffer.writeInt( mouseButton );
        buffer.writeBoolean( duplicateButton );
        buffer.writeBoolean( holdingShift );
    }

    public void doAction(
			final PlayerEntity player )
	{
		final Container c = player.containerMenu;
		if ( c instanceof BagContainer)
		{
			final BagContainer bc = (BagContainer) c;
			bc.handleCustomSlotAction( slotNumber, mouseButton, duplicateButton, holdingShift );
		}
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
        slotNumber = buffer.readInt();
        mouseButton = buffer.readInt();
        duplicateButton = buffer.readBoolean();
        holdingShift = buffer.readBoolean();
	}

}
