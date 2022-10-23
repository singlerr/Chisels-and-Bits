package com.communi.suggestu.formula.forge;

import com.communi.suggestu.formula.core.Formula;
import com.communi.suggestu.formula.core.util.Constants;
import com.communi.suggestu.scena.core.init.PlatformInitializationHandler;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Constants.MOD_ID)
public class Forge
{
    private static final Logger LOGGER = LoggerFactory.getLogger("formula-forge");

    private Formula formula;

    private void setFormula(final Formula formula)
    {
        this.formula = formula;
    }

    public Forge()
	{
        LOGGER.info("Initialized formula-forge");
        //We need to use the platform initialization manager to handle the init in the constructor since this runs in parallel with scena itself.
        PlatformInitializationHandler.getInstance().onInit(platform -> {
            setFormula(new Formula());
        });
	}
}
