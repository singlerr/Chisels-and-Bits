package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.PositivePatternMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

public class PacketSetChiselMode extends ModPacket
{

	private IToolMode mode = ChiselMode.SINGLE;
    private ChiselToolType type = ChiselToolType.CHISEL;
    private boolean chatNotification = false;

    public PacketSetChiselMode(PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketSetChiselMode(final IToolMode mode, final ChiselToolType type, final boolean chatNotification)
    {
        this.mode = mode;
        this.type = type;
        this.chatNotification = chatNotification;
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
		final ItemStack ei = player.getHeldItemMainhand();
		if ( ei != null && ei.getItem() instanceof IChiselModeItem )
		{
			final IToolMode originalMode = type.getMode( ei );
			mode.setMode( ei );

			if ( originalMode != mode && chatNotification )
			{
				player.sendMessage( new TranslationTextComponent( mode.getName().toString() ), Util.DUMMY_UUID);
			}
        }
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeBoolean( chatNotification );
		buffer.writeEnumValue( type );
		buffer.writeEnumValue( (Enum<?>) mode );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		chatNotification = buffer.readBoolean();
		type = buffer.readEnumValue( ChiselToolType.class );

		if ( type == ChiselToolType.BIT || type == ChiselToolType.CHISEL )
		{
			mode = buffer.readEnumValue( ChiselMode.class );
		}
		else if ( type == ChiselToolType.POSITIVEPATTERN )
		{
			mode = buffer.readEnumValue( PositivePatternMode.class );
		}
		else if ( type == ChiselToolType.TAPEMEASURE )
		{
			mode = buffer.readEnumValue( TapeMeasureModes.class );
		}
	}

    public IToolMode getMode()
    {
        return mode;
    }

    public void setMode(final IToolMode mode)
    {
        this.mode = mode;
    }

    public ChiselToolType getType()
    {
        return type;
    }

    public void setType(final ChiselToolType type)
    {
        this.type = type;
    }

    public boolean isChatNotification()
    {
        return chatNotification;
    }

    public void setChatNotification(final boolean chatNotification)
    {
        this.chatNotification = chatNotification;
    }
}
