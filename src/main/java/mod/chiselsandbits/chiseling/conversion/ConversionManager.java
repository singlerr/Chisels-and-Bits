package mod.chiselsandbits.chiseling.conversion;

import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.Optional;

public class ConversionManager implements IConversionManager
{
    private static final ConversionManager INSTANCE = new ConversionManager();

    public static ConversionManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Optional<Block> getChiseledVariantOf(final Block block)
    {
        final Material material = block.defaultBlockState().getMaterial();
        final Material workingMaterial = MaterialManager.getInstance().remapMaterialIfNeeded(material);

        if (!ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.containsKey(workingMaterial))
            return Optional.empty();

        return Optional.of(ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.get(workingMaterial)).map(RegistryObject::get);
    }

    private ConversionManager()
    {
    }
}
