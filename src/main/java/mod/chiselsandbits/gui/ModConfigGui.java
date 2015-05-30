package mod.chiselsandbits.gui;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ModConfig;
import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ModConfigGui extends GuiConfig {


	public ModConfigGui( GuiScreen parent )
	{
		super( parent, getConfigElements(), ChiselsAndBits.MODID, false, false, GuiConfig.getAbridgedConfigPath( ChiselsAndBits.instance.config.getFilePath() ) );
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();

		ModConfig config = ChiselsAndBits.instance.config;
		
		for( String cat : config.getCategoryNames() )
		{
			ConfigCategory cc = config.getCategory( cat );

			if( cc.isChild() )
			{
				continue;
			}

			ConfigElement ce = new ConfigElement( cc );
			list.add( ce );
		}

		return list;
	}

}
