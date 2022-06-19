package mod.chiselsandbits.forge.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin
{

    private static final Logger LOGGER = LogManager.getLogger();

    public static final ResourceLocation UID = new ResourceLocation(Constants.MOD_ID, ModIds.JEI_ID);

    @Override
    public @NotNull ResourceLocation getPluginUid()
    {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime jeiRuntime)
    {
        LOGGER.info("JEI Runtime is now available.");
        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

        final NonNullList<ItemStack> bitStacks = NonNullList.create();
        ModItems.ITEM_BLOCK_BIT.get().fillItemCategory(ModItems.ITEM_BLOCK_BIT.get().getItemCategory(), bitStacks);
        if (!bitStacks.isEmpty()) {
            if (JEICompatConfiguration.getInstance().getInjectBits().get()) {
                LOGGER.info("Injecting bits into JEI.");
                ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM, bitStacks);
            }
            else
            {
                LOGGER.info("Removing bits from JEI.");
                ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM, bitStacks);
            }
        }
    }
}

