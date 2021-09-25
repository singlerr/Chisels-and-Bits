package mod.chiselsandbits.data.advancement;

import mod.chiselsandbits.api.data.advancement.AbstractAdvancementGenerator;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselsAndBitsAdvancementGenerator extends AbstractAdvancementGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselsAndBitsAdvancementGenerator(event.getGenerator()));
    }

    private ChiselsAndBitsAdvancementGenerator(
      final DataGenerator generator)
    {
        super(generator, ChiselsAndBitsAdvancementGenerator::build);
    }

    private static void build(Consumer<Advancement> register)
    {
        Advancement root = Advancement.Builder.advancement()
                             .display(ModItems.ITEM_CHISEL_DIAMOND.get(),
                               new TranslationTextComponent("mod.chiselsandbits.advancements.root.title"),
                               new TranslationTextComponent("mod.chiselsandbits.advancements.root.description"),
                               new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                               FrameType.CHALLENGE,
                               true,
                               true,
                               true)
                             .addCriterion("chisel", InventoryChangeTrigger.Instance.hasItems(new ItemPredicate(
                               ModTags.Items.CHISEL,
                               null,
                               MinMaxBounds.IntBound.ANY,
                               MinMaxBounds.IntBound.ANY,
                               new EnchantmentPredicate[0],
                               new EnchantmentPredicate[0],
                               null,
                               NBTPredicate.ANY
                             )))
                             .save(register, Constants.MOD_ID + ":chiselsandbits/root");

        Advancement findChiselables = Advancement.Builder.advancement()
                                        .parent(root)
                                        .display(ModItems.MAGNIFYING_GLASS.get(),
                                          new TranslationTextComponent("mod.chiselsandbits.advancements.find-chiselables.title"),
                                          new TranslationTextComponent("mod.chiselsandbits.advancements.find-chiselables.description"),
                                          new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                                          FrameType.TASK,
                                          true,
                                          true,
                                          true)
                                        .addCriterion("magnifier_glass", InventoryChangeTrigger.Instance.hasItems(ModItems.MAGNIFYING_GLASS.get()))
                                        .save(register, Constants.MOD_ID + ":chiselsandbits/find_chiselables");

        Advancement collectBits = Advancement.Builder.advancement()
                                    .parent(root)
                                    .display(ModItems.BIT_BAG_DEFAULT.get(),
                                      new TranslationTextComponent("mod.chiselsandbits.advancements.collect-bits.title"),
                                      new TranslationTextComponent("mod.chiselsandbits.advancements.collect-bits.description"),
                                      new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                                      FrameType.TASK,
                                      true,
                                      true,
                                      true)
                                    .addCriterion("bit_bag", InventoryChangeTrigger.Instance.hasItems(new ItemPredicate(
                                      ModTags.Items.BIT_BAG,
                                      null,
                                      MinMaxBounds.IntBound.ANY,
                                      MinMaxBounds.IntBound.ANY,
                                      new EnchantmentPredicate[0],
                                      new EnchantmentPredicate[0],
                                      null,
                                      NBTPredicate.ANY
                                    )))
                                    .save(register, Constants.MOD_ID + ":chiselsandbits/collect_bits");

        Advancement makeTank = Advancement.Builder.advancement()
                                 .parent(root)
                                 .display(ModBlocks.BIT_STORAGE.get(),
                                   new TranslationTextComponent("mod.chiselsandbits.advancements.make-tank.title"),
                                   new TranslationTextComponent("mod.chiselsandbits.advancements.make-tank.description"),
                                   new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                                   FrameType.TASK,
                                   true,
                                   true,
                                   true)
                                 .addCriterion("bit_tank", InventoryChangeTrigger.Instance.hasItems(ModBlocks.BIT_STORAGE.get()))
                                 .save(register, Constants.MOD_ID + ":chiselsandbits/make_tank");
    }

    @Override
    public @NotNull String getName()
    {
        return "Chisels and bits default advancement generator";
    }
}