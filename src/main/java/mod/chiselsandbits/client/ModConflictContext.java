package mod.chiselsandbits.client;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import mod.chiselsandbits.api.KeyBindingContext;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public enum ModConflictContext implements IKeyConflictContext
{

	HOLDING_ROTATEABLE
	{
		@Override
		public boolean isActive()
		{
			if ( super.isActive() )
			{
				return true;
			}

			final ItemStack held = ClientSide.instance.getPlayer().getHeldItemMainhand();
			return held != null && held.getItem() instanceof IVoxelBlobItem;
		}

		@Override
		public boolean conflicts(
				final IKeyConflictContext other )
		{
			return this == other || other == KeyConflictContext.IN_GAME || other == HOLDING_MENUITEM;
		}
	},

	HOLDING_MENUITEM
	{
		@Override
		public boolean isActive()
		{
			if ( super.isActive() )
			{
				return true;
			}

			final ChiselToolType tool = ClientSide.instance.getHeldToolType( EnumHand.MAIN_HAND );
			return tool != null && tool.hasMenu();
		}

		@Override
		public boolean conflicts(
				final IKeyConflictContext other )
		{
			return this == other || other == KeyConflictContext.IN_GAME || other == HOLDING_POSTIVEPATTERN || other == HOLDING_CHISEL || other == HOLDING_TAPEMEASURE;
		}
	},
	HOLDING_TAPEMEASURE
	{
		@Override
		public boolean isActive()
		{
			return super.isActive() || ClientSide.instance.getHeldToolType( EnumHand.MAIN_HAND ) == ChiselToolType.TAPEMEASURE;
		}

		@Override
		public boolean conflicts(
				final IKeyConflictContext other )
		{
			return this == other || other == KeyConflictContext.IN_GAME;
		}
	},

	HOLDING_POSTIVEPATTERN
	{
		@Override
		public boolean isActive()
		{
			return super.isActive() || ClientSide.instance.getHeldToolType( EnumHand.MAIN_HAND ) == ChiselToolType.POSITIVEPATTERN;
		}

		@Override
		public boolean conflicts(
				final IKeyConflictContext other )
		{
			return this == other || other == KeyConflictContext.IN_GAME;
		}
	},

	HOLDING_CHISEL
	{
		@Override
		public boolean isActive()
		{
			if ( super.isActive() )
			{
				return true;
			}

			final ChiselToolType tool = ClientSide.instance.getHeldToolType( EnumHand.MAIN_HAND );
			return tool != null && tool.isBitOrChisel();
		}

		@Override
		public boolean conflicts(
				final IKeyConflictContext other )
		{
			return this == other || other == KeyConflictContext.IN_GAME;
		}
	};

	private final Set<Class<? extends Item>> activeItemClasses = new HashSet<Class<? extends Item>>();

	public void setItemActive(
			final Item item )
	{
		activeItemClasses.add( item.getClass() );
	}

	@Override
	public boolean isActive()
	{
		EntityPlayer player = ClientSide.instance.getPlayer();

		if ( player == null )
		{
			return false;
		}

		final ItemStack held = player.getHeldItemMainhand();

		if ( held == null )
		{
			return false;
		}

		for ( final Class<? extends Item> itemClass : activeItemClasses )
		{
			if ( itemClass.isInstance( held.getItem() ) )
			{
				return true;
			}
		}

		if ( held.getItem().getClass().isAnnotationPresent( KeyBindingContext.class ) )
		{
			final Annotation annotation = held.getItem().getClass().getAnnotation( KeyBindingContext.class );

			if ( annotation instanceof KeyBindingContext )
			{
				for ( final String name : ( (KeyBindingContext) annotation ).value() )
				{
					if ( name.equals( getName() ) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public String getName()
	{
		return toString().toLowerCase().replace( "holding_", "" );
	}

}
