package mod.chiselsandbits.data.blockstate;

import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.Direction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationTableBlockStateGenerator implements IDataProvider
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
    public void act(final @NotNull DirectoryCache cache) throws IOException
    {
        if (ModBlocks.MODIFICATION_TABLE.get().getRegistryName() == null)
            return;

        final Map<String, BlockstateVariantJson> variants = new HashMap<>();

        for (final Direction direction : HorizontalBlock.HORIZONTAL_FACING.getAllowedValues())
        {
            final String modelLocation = Constants.MOD_ID + ":block/modification_table";

            int y = getYFromDirection(direction);

            final BlockstateModelJson model = new BlockstateModelJson(modelLocation, 0, y);

            final BlockstateVariantJson variant = new BlockstateVariantJson(model);

            variants.put("facing=" + direction.getName2(), variant);
        }

        final BlockstateJson blockstate = new BlockstateJson(variants);

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(Objects.requireNonNull(ModBlocks.MODIFICATION_TABLE.get().getRegistryName()).getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(blockstate), blockstatePath);
    }

    private int getYFromDirection(final Direction direction)
    {
        switch (direction)
        {
            default:
                return 0;
            case EAST:
                return 90;
            case SOUTH:
                return 180;
            case WEST:
                return 270;
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Modification station blockstate generator.";
    }
}
