package mod.chiselsandbits.crafting;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipes
{

    public static SpecialRecipeSerializer<BagDyeing> BAG_DYEING = new SpecialRecipeSerializer<>(BagDyeing::new);
    public static SpecialRecipeSerializer<ChiselCrafting> CHISEL_CRAFTING = new SpecialRecipeSerializer<>(ChiselCrafting::new);
    public static SpecialRecipeSerializer<ChiselBlockCrafting> CHISEL_BLOCK_CRAFTING = new SpecialRecipeSerializer<>(ChiselBlockCrafting::new);
    public static SpecialRecipeSerializer<StackableCrafting> STACKABLE_CRAFTING = new SpecialRecipeSerializer<>(StackableCrafting::new);
    public static SpecialRecipeSerializer<NegativeInversionCrafting> NEGATIVE_INVERSION_CRAFTING = new SpecialRecipeSerializer<>(NegativeInversionCrafting::new);
    public static SpecialRecipeSerializer<MirrorTransferCrafting> MIRROR_TRANSFER_CRAFTING = new SpecialRecipeSerializer<>(MirrorTransferCrafting::new);
    public static SpecialRecipeSerializer<BitSawCrafting> BIT_SAW_CRAFTING = new SpecialRecipeSerializer<>(BitSawCrafting::new);


	@SubscribeEvent
	void registerRecipes(
			RegistryEvent.Register<IRecipeSerializer<?>> e )
	{
		IForgeRegistry<IRecipeSerializer<?>> r = e.getRegistry();

		r.registerAll(
		  BAG_DYEING,
          CHISEL_CRAFTING,
          CHISEL_BLOCK_CRAFTING,
          STACKABLE_CRAFTING,
          NEGATIVE_INVERSION_CRAFTING,
          MIRROR_TRANSFER_CRAFTING,
          BIT_SAW_CRAFTING
        );
	}

}
