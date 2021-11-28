package mod.chiselsandbits.data.loottable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DropBlockLootTableGenerator extends LootTableProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new DropBlockLootTableGenerator(event.getGenerator()));
    }

    private DropBlockLootTableGenerator(final DataGenerator gen)
    {
        super(gen);
    }

    @Override
    protected void validate(final @NotNull Map<ResourceLocation, LootTable> map, final @NotNull ValidationContext validationtracker)
    {
        //Noop
    }

    @Override
    protected @NotNull List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        return ImmutableList.of(
          Pair.of(
            Provider::new,
            LootContextParamSets.BLOCK
          )
        );
    }

    private static final class Provider extends BlockLoot {

        @Override
        protected void addTables()
        {
            this.dropSelf(ModBlocks.CHISELED_PRINTER.get());
            this.dropSelf(ModBlocks.MODIFICATION_TABLE.get());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks()
        {
            return ImmutableList.of(
              ModBlocks.CHISELED_PRINTER.get(),
              ModBlocks.MODIFICATION_TABLE.get()
            );
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Drop block loot table generator";
    }
}
