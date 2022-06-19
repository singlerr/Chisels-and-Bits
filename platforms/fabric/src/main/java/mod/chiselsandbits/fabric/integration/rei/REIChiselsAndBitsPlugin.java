package mod.chiselsandbits.fabric.integration.rei;

import me.shedaniel.rei.api.common.plugins.REIPlugin;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;

@ChiselsAndBitsPlugin(requiredMods = REIChiselsAndBitsPlugin.ID)
public class REIChiselsAndBitsPlugin implements IChiselsAndBitsPlugin
{
    public static final String ID = "rei";

    @Override
    public String getId()
    {
        return REIChiselsAndBitsPlugin.ID;
    }

    @Override
    public void onConstruction()
    {
        REICompatConfiguration.getInstance().initialize();
    }
}

