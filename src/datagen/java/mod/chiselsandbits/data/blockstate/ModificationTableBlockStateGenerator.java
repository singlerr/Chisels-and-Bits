package mod.chiselsandbits.data.blockstate;

import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationTableBlockStateGenerator implements DataProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ModificationTableBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ModificationTableBlockStateGenerator(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(final @NotNull HashCache cache) throws IOException
    {
        if (ModBlocks.MODIFICATION_TABLE.get().getRegistryName() == null)
            return;

        final Map<String, BlockstateVariantJson> variants = new HashMap<>();

        for (final Direction direction : HorizontalDirectionalBlock.FACING.getPossibleValues())
        {
            final String modelLocation = Constants.MOD_ID + ":block/modification_table";

            int y = getYFromDirection(direction);

            final BlockstateModelJson model = new BlockstateModelJson(modelLocation, 0, y);

            final BlockstateVariantJson variant = new BlockstateVariantJson(model);

            variants.put("facing=" + direction.getName(), variant);
        }

        final BlockstateJson blockstate = new BlockstateJson(variants);

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(Objects.requireNonNull(ModBlocks.MODIFICATION_TABLE.get().getRegistryName()).getPath() + ".json");

        DataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(blockstate), blockstatePath);
    }

    private int getYFromDirection(final Direction direction)
    {
        return switch (direction)
                 {
                     default -> 0;
                     case WEST -> 90;
                     case NORTH -> 180;
                     case EAST -> 270;
                 };
    }

    @Override
    public @NotNull String getName()
    {
        return "Modification station blockstate generator.";
    }
}
