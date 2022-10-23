package com.communi.suggestu.formula.fabric;

import com.communi.suggestu.formula.core.Formula;
import com.communi.suggestu.scena.core.init.PlatformInitializationHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fabric implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("formula-fabric");

    private Formula formula;

    public Fabric()
    {
        LOGGER.info("Initialized formula-forge");
        PlatformInitializationHandler.getInstance().onInit(platform -> setFormula(new Formula()));
    }

    @Override
    public void onInitialize()
    {
        //Noop for now.
    }

    public void setFormula(final Formula formula)
    {
        this.formula = formula;
    }
}
