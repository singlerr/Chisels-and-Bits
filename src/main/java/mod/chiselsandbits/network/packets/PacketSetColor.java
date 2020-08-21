package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

public class PacketSetColor extends ModPacket
{

	private DyeColor       newColor         = DyeColor.WHITE;
    private ChiselToolType type             = ChiselToolType.TAPEMEASURE;
    private boolean        chatNotification = false;

    public PacketSetColor(final PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketSetColor(final DyeColor newColor, final ChiselToolType type, final boolean chatNotification)
    {
        this.newColor = newColor;
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
			final DyeColor originalMode = getColor( ei );
			setColor( ei, newColor );

			if ( originalMode != newColor && chatNotification )
			{
				player.sendMessage( new TranslationTextComponent( "chiselsandbits.color." + newColor.getTranslationKey() ), Util.DUMMY_UUID );
			}
		}
	}

	private void setColor(
			final ItemStack ei,
			final DyeColor newColor2 )
	{
		if ( ei != null )
		{
			ei.setTagInfo( "color", StringNBT.valueOf( newColor2.name() ) );
		}
	}

	private DyeColor getColor(
			final ItemStack ei )
	{
		try
		{
			if ( ei != null && ei.hasTag() )
			{
				return DyeColor.valueOf( ModUtil.getTagCompound( ei ).getString( "color" ) );
			}
		}
		catch ( final IllegalArgumentException e )
		{
			// nope!
		}

		return DyeColor.WHITE;
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeBoolean( chatNotification );
		buffer.writeEnumValue( type );
		buffer.writeEnumValue( newColor );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		chatNotification = buffer.readBoolean();
		type = buffer.readEnumValue( ChiselToolType.class );
		newColor = buffer.readEnumValue( DyeColor.class );
	}

}
