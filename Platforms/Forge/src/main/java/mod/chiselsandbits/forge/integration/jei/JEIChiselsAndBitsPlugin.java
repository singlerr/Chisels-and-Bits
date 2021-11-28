package mod.chiselsandbits.forge.integration.jei;

import mezz.jei.api.constants.ModIds;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;

@ChiselsAndBitsPlugin(requiredMods = ModIds.JEI_ID)
public class JEIChiselsAndBitsPlugin implements IChiselsAndBitsPlugin
{
    @Override
    public String getId()
    {
        return ModIds.JEI_ID;
    }

    @Override
    public void onConstruction()
    {
        JEICompatConfiguration.getInstance().initialize();
    }
}
