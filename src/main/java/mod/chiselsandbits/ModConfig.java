
package mod.chiselsandbits;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModConfig extends Configuration
{
	// automatic setting...
	public boolean allowBlockAlternatives = false;

	// file path...
	final private File myPath;

	@Configured( category = "Integration Settings" )
	private boolean ShowBitsInJEI;

	// mod settings...
	@Configured( category = "Client Settings" )
	private boolean showUsage;

	@Configured( category = "Client Settings" )
	public boolean invertBitBagFullness;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_Plane;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_ConnectedPlane;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_Line;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_SmallCube;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_LargeCube;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_HugeCube;

	@Configured( category = "Client Settings" )
	public boolean enableChiselMode_DrawnRegion;

	@Configured( category = "Client Settings" )
	public boolean enableToolbarIcons;

	@Configured( category = "Client Settings" )
	public boolean perChiselMode;

	@Configured( category = "Client Settings" )
	public boolean chatModeNotification;

	@Configured( category = "Client Settings" )
	public boolean itemNameModeDisplay;

	@Configured( category = "Balance Settings" )
	public boolean damageTools;

	@Configured( category = "Balance Settings" )
	public long availableUsesMultiplier;

	@Configured( category = "Crafting" )
	boolean enablePositivePrintCrafting;

	@Configured( category = "Crafting" )
	public boolean enableStackableCrafting;

	@Configured( category = "Crafting" )
	public boolean enableNegativePrintInversionCrafting;

	@Configured( category = "Items" )
	boolean enableBitBag;

	@Configured( category = "Items" )
	boolean enableNegativePrint;

	@Configured( category = "Items" )
	boolean enablePositivePrint;

	@Configured( category = "Items" )
	boolean enableChisledBits;

	@Configured( category = "Items" )
	boolean enableStoneChisel;

	@Configured( category = "Items" )
	boolean enableIronChisel;

	@Configured( category = "Items" )
	boolean enableGoldChisel;

	@Configured( category = "Items" )
	boolean enableDiamondChisel;

	@Configured( category = "Items" )
	boolean enableWoodenWrench;

	@Configured( category = "Balance Settings" )
	public boolean enableChiselToolHarvestCheck;

	@Configured( category = "Balance Settings" )
	public String enableChiselToolHarvestCheckTools;

	@Configured( category = "Balance Settings" )
	public boolean enableToolHarvestLevels;

	@Configured( category = "Balance Settings" )
	public boolean enableBitLightSource;

	@Configured( category = "Balance Settings" )
	public float bitLightPercentage;

	public boolean isEnabled(
			final String className )
	{
		final Property p = get( "Enabled Blocks", className, true );
		final boolean out = p.getBoolean( true );

		if ( hasChanged() )
		{
			save();
		}

		return out;
	}

	private void setDefaults()
	{
		enableChiselMode_ConnectedPlane = !ChiselMode.CONNECTED_PLANE.isDisabled;
		enableChiselMode_HugeCube = !ChiselMode.CUBE_HUGE.isDisabled;
		enableChiselMode_LargeCube = !ChiselMode.CUBE_LARGE.isDisabled;
		enableChiselMode_SmallCube = !ChiselMode.CUBE_SMALL.isDisabled;
		enableChiselMode_Line = !ChiselMode.LINE.isDisabled;
		enableChiselMode_Plane = !ChiselMode.PLANE.isDisabled;
		enableChiselMode_DrawnRegion = !ChiselMode.DRAWN_REGION.isDisabled;
		perChiselMode = true;
		chatModeNotification = false;
		itemNameModeDisplay = true;
		enableToolbarIcons = true;

		showUsage = true;
		invertBitBagFullness = false;

		damageTools = true;
		availableUsesMultiplier = 32;

		enablePositivePrintCrafting = true;
		enableStackableCrafting = true;
		enableNegativePrintInversionCrafting = true;

		enableChiselToolHarvestCheck = true;
		enableToolHarvestLevels = true;
		enableChiselToolHarvestCheckTools = "pickaxe,axe,shovel";

		enableBitLightSource = true;
		bitLightPercentage = 6.25f;
		enableBitBag = true;
		enableNegativePrint = true;
		enablePositivePrint = true;
		enableChisledBits = true;
		enableStoneChisel = true;
		enableIronChisel = true;
		enableGoldChisel = true;
		enableDiamondChisel = true;
		enableWoodenWrench = true;
		ShowBitsInJEI = false;
	}

	public ModConfig(
			final File path )
	{
		super( path );
		myPath = path;
		ChiselsAndBits.registerWithBus( this, ForgeBus.FML );
		setDefaults();
		populateSettings();
		save();
	}

	void populateSettings()
	{
		final Class<ModConfig> me = ModConfig.class;
		for ( final Field f : me.getDeclaredFields() )
		{
			final Configured c = f.getAnnotation( Configured.class );
			if ( c != null )
			{
				try
				{
					if ( f.getType() == long.class || f.getType() == Long.class )
					{
						final long defaultValue = f.getLong( this );
						final long value = get( c.category(), f.getName(), (int) defaultValue ).getInt();
						f.set( this, value );
					}
					else if ( f.getType() == String.class )
					{
						final String defaultValue = (String) f.get( this );
						final String value = get( c.category(), f.getName(), defaultValue ).getString();
						f.set( this, value );
					}
					else if ( f.getType() == int.class || f.getType() == Integer.class )
					{
						final int defaultValue = f.getInt( this );
						final int value = get( c.category(), f.getName(), defaultValue ).getInt();
						f.set( this, value );
					}
					else if ( f.getType() == float.class || f.getType() == Float.class )
					{
						final float defaultValue = f.getFloat( this );
						final float value = (float) get( c.category(), f.getName(), defaultValue ).getDouble();
						f.set( this, value );
					}
					else if ( f.getType() == boolean.class || f.getType() == Boolean.class )
					{
						final boolean defaultValue = f.getBoolean( this );
						final boolean value = get( c.category(), f.getName(), defaultValue ).getBoolean();
						f.set( this, value );
					}
				}
				catch ( final IllegalArgumentException e )
				{
					// yar!
					e.printStackTrace();
				}
				catch ( final IllegalAccessException e )
				{
					// yar!
					e.printStackTrace();
				}
			}
		}

		sync();
	}

	private void sync()
	{
		ChiselMode.CONNECTED_PLANE.isDisabled = !enableChiselMode_ConnectedPlane;
		ChiselMode.CUBE_HUGE.isDisabled = !enableChiselMode_HugeCube;
		ChiselMode.CUBE_LARGE.isDisabled = !enableChiselMode_LargeCube;
		ChiselMode.CUBE_SMALL.isDisabled = !enableChiselMode_SmallCube;
		ChiselMode.LINE.isDisabled = !enableChiselMode_Line;
		ChiselMode.PLANE.isDisabled = !enableChiselMode_Plane;
		ChiselMode.DRAWN_REGION.isDisabled = !enableChiselMode_DrawnRegion;
	}

	@SubscribeEvent
	public void onConfigChanged(
			final ConfigChangedEvent.OnConfigChangedEvent eventArgs )
	{
		if ( eventArgs.modID.equals( ChiselsAndBits.MODID ) )
		{
			populateSettings();
			save();
		}
	}

	@Override
	public void save()
	{
		if ( hasChanged() )
		{
			super.save();
		}
	}

	@Override
	public Property get(
			final String category,
			final String key,
			final String defaultValue,
			final String comment,
			final Property.Type type )
	{
		final Property prop = super.get( category, key, defaultValue, comment, type );

		if ( prop != null && !category.equals( "Client Settings" ) )
		{
			prop.setRequiresMcRestart( true );
		}

		return prop;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void helpText(
			final LocalStrings string,
			final List tooltip )
	{
		if ( showUsage )
		{
			final String[] lines = string.getLocal().split( ";" );
			for ( final String a : lines )
			{
				tooltip.add( a );
			}
		}
	}

	public String getFilePath()
	{
		return myPath.getAbsolutePath();
	}

}