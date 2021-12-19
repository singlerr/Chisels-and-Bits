package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CreateClient::clientInit);
    }

    @Override
    public void onCommonSetup()
    {
        LOGGER.info("Registering movement override handler.");
        BlockMovementChecks.registerMovementAllowedCheck((state, world, pos) -> state.getBlock() instanceof IMultiStateBlock ? BlockMovementChecks.CheckResult.SUCCESS : BlockMovementChecks.CheckResult.PASS);

        LOGGER.info("Registering movement behaviours.");
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream()
          .map(IRegistryObject::get)
          .forEach(block -> AllMovementBehaviours.addMovementBehaviour(block, new ChiseledBlockMovementBehaviour()));
    }
}
