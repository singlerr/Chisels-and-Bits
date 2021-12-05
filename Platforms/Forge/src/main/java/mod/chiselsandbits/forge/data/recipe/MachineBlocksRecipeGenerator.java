package mod.chiselsandbits.forge.data.recipe;

import com.google.common.collect.ImmutableMap;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MachineBlocksRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.CHISELED_PRINTER.get(),
            " c ;l l;sss",
            ImmutableMap.of(
              'c', ModTags.Items.CHISEL,
              'l', ItemTags.LOGS
            ),
            ImmutableMap.of(
              's', Blocks.SMOOTH_STONE_SLAB
            )
          )
        );

        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.MODIFICATION_TABLE.get(),
            "scs;nbn;ppp",
            ImmutableMap.of(
              's', ItemTags.WOODEN_SLABS,
              'n', Tags.Items.NUGGETS_IRON,
              'b', ItemTags.LOGS,
              'p', ItemTags.PLANKS,
              'c', ModTags.Items.CHISEL
            ),
            ImmutableMap.of()
          )
        );

        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.BIT_STORAGE.get(),
            "igi;glg;ici",
            ImmutableMap.of(
              'g', Tags.Items.GLASS,
              'l', ItemTags.LOGS,
              'i', Tags.Items.INGOTS_IRON,
              'c', ModTags.Items.CHISEL
            ),
            ImmutableMap.of()
          )
        );
    }

    private final List<String>                            pattern;
    private final Map<Character, Tag<Item>>               tagMap;
    private final Map<Character, ItemLike> itemMap;

    private MachineBlocksRecipeGenerator(
      final DataGenerator generator,
      final ItemLike result,
      final String pattern,
      final Map<Character, Tag<Item>> tagMap,
      final Map<Character, ItemLike> itemMap)
    {
        super(generator, result);
        this.pattern = Arrays.asList(pattern.split(";"));
        this.tagMap = tagMap;
        this.itemMap = itemMap;
    }

    @Override
    protected void buildCraftingRecipes(final @NotNull Consumer<FinishedRecipe> writer)
    {
        final ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(getItemProvider());
        pattern.forEach(builder::pattern);
        tagMap.forEach((ingredientKey, tag) -> {
            builder.define(ingredientKey, tag);
            builder.unlockedBy("has_" + ingredientKey, has(tag));
        });
        itemMap.forEach((ingredientKey, item) -> {
            builder.define(ingredientKey, item);
            builder.unlockedBy("has_" + ingredientKey, has(item));
        });
        builder.save(writer);
    }
}
