package mod.chiselsandbits.forge.data.tag;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModItemTagGenerator extends ItemTagsProvider
{
    public ModItemTagGenerator(
      final DataGenerator dataGenerator, final BlockTagsProvider blockTagsProvider, @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(dataGenerator, blockTagsProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        this.tag(ModTags.Items.BIT_BAG).add(ModItems.ITEM_BIT_BAG_DEFAULT.get(), ModItems.ITEM_BIT_BAG_DYED.get());
        this.tag(ModTags.Items.CHISEL).add(
          ModItems.ITEM_CHISEL_STONE.get(),
          ModItems.ITEM_CHISEL_IRON.get(),
          ModItems.ITEM_CHISEL_GOLD.get(),
          ModItems.ITEM_CHISEL_DIAMOND.get(),
          ModItems.ITEM_CHISEL_NETHERITE.get()
        );
        this.tag(ModTags.Items.FORGE_PAPER).add(Items.PAPER);
        this.tag(ItemTags.PIGLIN_LOVED).add(ModItems.ITEM_CHISEL_GOLD.get());
    }
}
