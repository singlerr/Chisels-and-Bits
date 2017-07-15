package mod.chiselsandbits.core.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCHandlerMaterialEquivilancy implements IMCMessageHandler
{

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void excuteIMC(
			final IMCMessage message )
	{
		try
		{
			if ( message.isFunctionMessage() )
			{
				final Optional<Function<Map, Void>> map = message.getFunctionValue( Map.class, Void.class );

				final Map<Object, Object> obj = new HashMap();
				map.get().apply( obj );

				for ( final Entry<Object, Object> set : obj.entrySet() )
				{
					if ( set.getKey() instanceof Material && set.getValue() instanceof Material )
					{
						ChiselsAndBits.getApi().addEquivilantMaterial( (Material) set.getKey(), (Material) set.getValue() );
					}
					else
					{
						Log.info( "Expected materials for keys and values but got something else - IMC: " + message.getMessageType().getName() );
					}
				}
			}
			else
			{
				Log.info( "Invalid Type for IMC: " + message.getMessageType().getName() );
				return;
			}
		}
		catch ( final Throwable e )
		{
			Log.logError( "IMC materialequivilancy From " + message.getSender(), e );
		}
	}
}
