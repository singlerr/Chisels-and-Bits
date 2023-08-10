package mod.chiselsandbits.forge.data.advancement;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselsAndBitsAdvancementGenerator extends AdvancementProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new ChiselsAndBitsAdvancementGenerator(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
    }

    public ChiselsAndBitsAdvancementGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> holderProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, holderProvider, List.of(new Provider()));
    }

    private static final class Provider implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
            Advancement root = Advancement.Builder.advancement()
                    .display(ModItems.ITEM_CHISEL_DIAMOND.get(),
                            Component.translatable("mod.chiselsandbits.advancements.root.title"),
                            Component.translatable("mod.chiselsandbits.advancements.root.description"),
                            new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.CHALLENGE,
                            true,
                            true,
                            true)
                    .addCriterion("chisel", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                            ModTags.Items.CHISEL,
                            null,
                            MinMaxBounds.Ints.ANY,
                            MinMaxBounds.Ints.ANY,
                            new EnchantmentPredicate[0],
                            new EnchantmentPredicate[0],
                            null,
                            NbtPredicate.ANY
                    )))
                    .save(consumer, Constants.MOD_ID + ":chiselsandbits/root");

            Advancement findChiselables = Advancement.Builder.advancement()
                    .parent(root)
                    .display(ModItems.MAGNIFYING_GLASS.get(),
                            Component.translatable("mod.chiselsandbits.advancements.find-chiselables.title"),
                            Component.translatable("mod.chiselsandbits.advancements.find-chiselables.description"),
                            new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK,
                            true,
                            true,
                            true)
                    .addCriterion("magnifier_glass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MAGNIFYING_GLASS.get()))
                    .save(consumer, Constants.MOD_ID + ":chiselsandbits/find_chiselables");

            Advancement collectBits = Advancement.Builder.advancement()
                    .parent(root)
                    .display(ModItems.ITEM_BIT_BAG_DEFAULT.get(),
                            Component.translatable("mod.chiselsandbits.advancements.collect-bits.title"),
                            Component.translatable("mod.chiselsandbits.advancements.collect-bits.description"),
                            new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK,
                            true,
                            true,
                            true)
                    .addCriterion("bit_bag", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                            ModTags.Items.BIT_BAG,
                            null,
                            MinMaxBounds.Ints.ANY,
                            MinMaxBounds.Ints.ANY,
                            new EnchantmentPredicate[0],
                            new EnchantmentPredicate[0],
                            null,
                            NbtPredicate.ANY
                    )))
                    .save(consumer, Constants.MOD_ID + ":chiselsandbits/collect_bits");

            Advancement makeTank = Advancement.Builder.advancement()
                    .parent(root)
                    .display(ModBlocks.BIT_STORAGE.get(),
                            Component.translatable("mod.chiselsandbits.advancements.make-tank.title"),
                            Component.translatable("mod.chiselsandbits.advancements.make-tank.description"),
                            new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK,
                            true,
                            true,
                            true)
                    .addCriterion("bit_tank", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.BIT_STORAGE.get()))
                    .save(consumer, Constants.MOD_ID + ":chiselsandbits/make_tank");
        }
    }
}