
package mod.chiselsandbits.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.FMLConfigGuiFactory;


public class ModConfigGuiFactory extends FMLConfigGuiFactory
{

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass()
	{
		return ModConfigGui.class;
	}

}
