package mod.chiselsandbits.core.api;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.Log;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.InterModComms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class IMCHandlerForceState implements IMCMessageHandler
{

	@SuppressWarnings( "rawtypes" )
	@Override
	public void excuteIMC(
			final InterModComms.IMCMessage message )
	{
		try
		{

		    final Supplier<Optional<Function<List, Boolean>>> methodSupplier = message.getMessageSupplier();
			final Optional<Function<List, Boolean>> method = methodSupplier.get();

			if ( method.isPresent() )
			{
				final Function<List, Boolean> targetMethod = method.get();
				final ArrayList<?> o = new ArrayList<Object>();
				final Boolean result = targetMethod.apply( o );

				if ( result == null )
				{
					Log.info( message.getSenderModId() + ", Your IMC returns null, must be true or false for " + message.getMethod() );
				}
				else
				{
					for ( final Object x : o )
					{
						if ( x instanceof BlockState )
						{
							BlockBitInfo.forceStateCompatibility( (BlockState) x, result );
						}
						else
						{
							Log.info( message.getSenderModId() + ", Your IMC provided a Object that was not an BlockState : " + x.getClass().getName() );
						}
					}
				}
			}
			else
			{
				Log.info( message.getSenderModId() + ", Your IMC must be a functional message, 'Boolean apply( List )'." );
			}
		}
		catch ( final Throwable e )
		{
			Log.logError( "IMC forcestatecompatibility From " + message.getMethod(), e );
		}
	}
}
