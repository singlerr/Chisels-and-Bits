package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.crafting.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ModRecipeSerializers
{

    private static final DeferredRegister<IRecipeSerializer<?>> REGISTRAR = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ChiselsAndBits.MODID);
    
    private ModRecipeSerializers()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeSerializers but this is a Utility class.");
    }

    public static final RegistryObject<SpecialRecipeSerializer<BagDyeing>> BAG_DYEING = REGISTRAR.register("bag_dyeing", () -> new SpecialRecipeSerializer<>(BagDyeing::new));
    public static final RegistryObject<SpecialRecipeSerializer<ChiselCrafting>> CHISEL_CRAFTING = REGISTRAR.register("chisel_crafting", () -> new SpecialRecipeSerializer<>(ChiselCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<ChiselBlockCrafting>> CHISEL_BLOCK_CRAFTING = REGISTRAR.register("chisel_block_crafting", () -> new SpecialRecipeSerializer<>(ChiselBlockCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<StackableCrafting>> STACKABLE_CRAFTING = REGISTRAR.register("stackable_crafting", () -> new SpecialRecipeSerializer<>(StackableCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<NegativeInversionCrafting>> NEGATIVE_INVERSION_CRAFTING = REGISTRAR.register("negative_inversion_crafting", () -> new SpecialRecipeSerializer<>(NegativeInversionCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<MirrorTransferCrafting>> MIRROR_TRANSFER_CRAFTING = REGISTRAR.register("mirror_transfer_crafting", () -> new SpecialRecipeSerializer<>(MirrorTransferCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<BitSawCrafting>> BIT_SAW_CRAFTING = REGISTRAR.register("bit_saw_crafting", () -> new SpecialRecipeSerializer<>(BitSawCrafting::new));

    public static void onModConstruction() {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
