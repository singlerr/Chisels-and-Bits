package mod.chiselsandbits.integration.chiselsandbits.create;

import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChiselsAndBitsPlugin(requiredMods = "create")
public class CreatePlugin implements IChiselsAndBitsPlugin
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getId()
    {
        return "create";
    }

    @Override
    public void onConstruction()
    {
        LOGGER.info("Create Plugin for ChiselsAndBits activated.");
    }

    @Override
    public void onCommonSetup()
    {
        LOGGER.info("Registering movement override handler.");

        BlockMovementChecks.registerMovementAllowedCheck((state, world, pos) -> state.getBlock() instanceof IMultiStateBlock ? BlockMovementChecks.CheckResult.SUCCESS : BlockMovementChecks.CheckResult.PASS);
    }
}
