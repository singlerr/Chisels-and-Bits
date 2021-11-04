package mod.chiselsandbits.chiseling.conversion;

import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.platforms.core.registrars.IBlockRegistrar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

import java.util.Optional;
import java.util.function.Supplier;

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

        if (!IBlockRegistrar.getInstance().getMaterialToChiseledBlockConversions().containsKey(workingMaterial))
            return Optional.empty();

        return Optional.of(IBlockRegistrar.getInstance().getMaterialToChiseledBlockConversions().get(workingMaterial)).map(Supplier::get);
    }

    private ConversionManager()
    {
    }
}
