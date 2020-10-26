package mod.chiselsandbits.client.gui;

import java.lang.reflect.Constructor;

import mod.chiselsandbits.bitbag.BagContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;

@SuppressWarnings( "unused" )
public enum ModGuiTypes
{

	BitBag( BagContainer.class );

    public final Constructor<?> container_construtor;
	public final Constructor<?> gui_construtor;

	private ModGuiTypes(
			final Class<? extends Container> c )
	{
        Class<? extends Container> container;
        try
		{
			container = c;
			container_construtor = container.getConstructor( PlayerEntity.class, World.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}

		// by default...
		Class<?> g = null;
		Constructor<?> g_construtor = null;

		// attempt to get gui class/constructor...
		try
		{
			g = (Class<?>) container.getMethod( "getGuiClass" ).invoke( null );
			g_construtor = g.getConstructor( PlayerEntity.class, World.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			// Only throw error if this is a client...
			if (EffectiveSide.get().isClient())
			{
				throw new RuntimeException( e );
			}

		}

        final Class<?> gui = g;
		gui_construtor = g_construtor;

	}
}
