package mod.chiselsandbits.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import mod.chiselsandbits.client.CreativeClipboardTab;
import mod.chiselsandbits.client.ModCreativeTab;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRegistry
{

	public static final String unlocalizedPrefix = "mod." + ChiselsAndBits.MODID + ".";

	static ModCreativeTab creativeTab = new ModCreativeTab();
	static CreativeClipboardTab creativeClipboard = null;

	public ModRegistry()
	{
		ChiselsAndBits.registerWithBus( this );
	}

	static
	{
		if ( ChiselsAndBits.getConfig().getServer().creativeClipboardSize.get() > 0 )
		{
			creativeClipboard = new CreativeClipboardTab();
		}
	}

	List<Item> registeredItems = new ArrayList<Item>();
	List<Block> registeredBlocks = new ArrayList<Block>();

	@SubscribeEvent
	public void registerItems(
			RegistryEvent.Register<Item> e )
	{
		IForgeRegistry<Item> r = e.getRegistry();
		for ( Item b : registeredItems )
		{
			r.register( b );
		}

		if ( !registeredItems.isEmpty() && ChiselsAndBits.getInstance().loadClientAssets() )
		{
			ClientSide.instance.registerItemModels();
		}
	}

	@SubscribeEvent
	public void registerBlocks(
			RegistryEvent.Register<Block> e )
	{
		IForgeRegistry<Block> r = e.getRegistry();
		for ( Block b : registeredBlocks )
		{
			r.register( b );
		}

		if ( !registeredBlocks.isEmpty() && ChiselsAndBits.getInstance().loadClientAssets() )
		{
			ClientSide.instance.registerBlockModels();
		}
	}

	protected <T extends Item> T registerItem(
			final Function<Item.Properties, T> itemProducer,
			final String name )
	{
	    final T item = (T) itemProducer.apply(new Item.Properties().group(creativeTab)).setRegistryName(ChiselsAndBits.MODID, name);
	    registeredItems.add(item);
        return item;
	}

	protected void registerBlock(
	        final Material blockMaterial,
			final MaterialColor blockMaterialColor,
			final Function<AbstractBlock.Properties, Block> blockProducer,
			final BiFunction<Block, Item.Properties, BlockItem> blockItemProducer,
			final String name )
	{
	    final Block block = blockProducer.apply(AbstractBlock.Properties.create(blockMaterial, blockMaterialColor)).setRegistryName(ChiselsAndBits.MODID, name);
	    final BlockItem item = (BlockItem) blockItemProducer.apply(block, new Item.Properties().group(creativeTab)).setRegistryName(ChiselsAndBits.MODID, name);

		registeredBlocks.add( block );
		registeredItems.add( item );
	}
}
