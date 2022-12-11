package mod.chiselsandbits.forge.data.loottable;

import com.google.common.collect.ImmutableList;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DropBlockLootTableGenerator extends LootTableProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new DropBlockLootTableGenerator(event.getGenerator().getPackOutput(), FeatureFlags.REGISTRY.allFlags()));
    }

    private DropBlockLootTableGenerator(PackOutput packOutput, FeatureFlagSet featureFlagSet)
    {
        super(packOutput, Set.of(), createTables(featureFlagSet));
    }

    @Override
    protected void validate(final @NotNull Map<ResourceLocation, LootTable> map, final @NotNull ValidationContext validationtracker)
    {
        //Noop
    }

    private static @NotNull List<LootTableProvider.SubProviderEntry> createTables(final FeatureFlagSet featureFlagSet)
    {
        return ImmutableList.of(
          new SubProviderEntry(
            () -> new SelfDroppingBlocks(featureFlagSet),
            LootContextParamSets.BLOCK
          )
        );
    }

    private static final class SelfDroppingBlocks extends BlockLootSubProvider {

        private SelfDroppingBlocks(FeatureFlagSet featureFlagSet) {
            super(Set.of(), featureFlagSet);
        }

        @Override
        protected void generate() {
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
}
