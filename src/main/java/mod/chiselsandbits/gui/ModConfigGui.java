
package mod.chiselsandbits.gui;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ModConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;


public class ModConfigGui extends GuiConfig
{

	public ModConfigGui(
			final GuiScreen parent )
	{
		super( parent, getConfigElements(), ChiselsAndBits.MODID, false, false, GuiConfig.getAbridgedConfigPath( ChiselsAndBits.instance.config.getFilePath() ) );
	}

	private static List<IConfigElement> getConfigElements()
	{
		final List<IConfigElement> list = new ArrayList<IConfigElement>();

		final ModConfig config = ChiselsAndBits.instance.config;

		for ( final String cat : config.getCategoryNames() )
		{
			final ConfigCategory cc = config.getCategory( cat );

			if ( cc.isChild() )
			{
				continue;
			}

			final ConfigElement ce = new ConfigElement( cc );
			list.add( ce );
		}

		return list;
	}

}
