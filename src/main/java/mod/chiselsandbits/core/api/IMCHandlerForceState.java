package mod.chiselsandbits.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCHandlerForceState implements IMCMessageHandler
{

	@SuppressWarnings( "rawtypes" )
	@Override
	public void excuteIMC(
			final IMCMessage message )
	{
		try
		{
			final Optional<Function<List, Boolean>> method = message.getFunctionValue( List.class, Boolean.class );

			if ( method.isPresent() )
			{
				final Function<List, Boolean> targetMethod = method.get();
				final ArrayList<?> o = new ArrayList<Object>();
				final Boolean result = targetMethod.apply( o );

				if ( result == null )
				{
					Log.info( message.getSender() + ", Your IMC returns null, must be true or false for " + message.getMessageType().getName() );
				}
				else
				{
					for ( final Object x : o )
					{
						if ( x instanceof IBlockState )
						{
							BlockBitInfo.forceStateCompatibility( (IBlockState) x, result );
						}
						else
						{
							Log.info( message.getSender() + ", Your IMC provided a Object that was not an IBlockState : " + x.getClass().getName() );
						}
					}
				}
			}
			else
			{
				Log.info( message.getSender() + ", Your IMC must be a functional message, 'Boolean apply( List )'." );
			}
		}
		catch ( final Throwable e )
		{
			Log.logError( "IMC forcestatecompatibility From " + message.getSender(), e );
		}
	}
}
