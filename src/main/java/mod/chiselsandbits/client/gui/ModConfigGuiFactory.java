package mod.chiselsandbits.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.FMLConfigGuiFactory;

public class ModConfigGuiFactory extends FMLConfigGuiFactory
{

	@Override
	public GuiScreen createConfigGui(
			final GuiScreen parentScreen )
	{
		return new ModConfigGui( parentScreen );
	}

}
