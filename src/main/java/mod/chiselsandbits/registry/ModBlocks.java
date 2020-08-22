package mod.chiselsandbits.registry;

import java.util.HashMap;
import java.util.Map;

import mod.chiselsandbits.bittank.BlockBitTank;
import mod.chiselsandbits.bittank.TileEntityBitTank;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled.TileEntityBlockChiseledDummy;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks extends ModRegistry
{

	// TE Registration names.
	private static String TE_BIT_TANK = "mod.chiselsandbits.TileEntityBitTank";
	private static String TE_CHISELEDBLOCK = "mod.chiselsandbits.TileEntityChiseled";
	private static String TE_CHISELEDBLOCK_TESR = "mod.chiselsandbits.TileEntityChiseled.tesr";

	private final HashMap<Material, BlockChiseled> conversions = new HashMap<Material, BlockChiseled>();
	private final HashMap<Material, Item> itemConversions = new HashMap<Material, Item>();

	public final BlockItem itemBitTank;
	public final BlockBitTank blockBitTank;

	public static final MaterialType[] validMaterials = new MaterialType[] {
			new MaterialType( "wood", Material.WOOD ),
			new MaterialType( "rock", Material.ROCK ),
			new MaterialType( "iron", Material.IRON ),
			new MaterialType( "cloth", Material.CARPET ),
			new MaterialType( "ice", Material.ICE ),
			new MaterialType( "packedIce", Material.PACKED_ICE ),
			new MaterialType( "clay", Material.CLAY ),
			new MaterialType( "glass", Material.GLASS ),
			new MaterialType( "sand", Material.SAND ),
			new MaterialType( "ground", Material.EARTH ),
			new MaterialType( "grass", Material.EARTH ),
			new MaterialType( "snow", Material.SNOW_BLOCK ),
			new MaterialType( "fluid", Material.WATER ),
			new MaterialType( "leaves", Material.LEAVES ),
	};

	public ModBlocks()
	{
		// register tile entities.
		GameRegistry.registerTileEntity( TileEntityBlockChiseled.class, TE_CHISELEDBLOCK );

		/**
		 * register the TESR name either way, but if its a dedicated server
		 * register the normal class under the same name.
		 */
		if ( side == Side.CLIENT )
		{
			GameRegistry.registerTileEntity( TileEntityBlockChiseledTESR.class, TE_CHISELEDBLOCK_TESR );
		}
		else
		{
			GameRegistry.registerTileEntity( TileEntityBlockChiseledDummy.class, TE_CHISELEDBLOCK_TESR );
		}

        blockBitTank = new BlockBitTank(AbstractBlock.Properties.create(Material.IRON));
        itemBitTank = new BlockItem( blockBitTank, new Item.Properties() );
        registerBlock( blockBitTank, itemBitTank, "bittank" );
        GameRegistry.registerTileEntity( TileEntityBitTank.class, TE_BIT_TANK );

		// register blocks...
		for ( final MaterialType mat : validMaterials )
		{
			final BlockChiseled blk = new BlockChiseled("chiseled_" + mat.name, AbstractBlock.Properties.create(mat.type) );
			final ItemBlockChiseled item = new ItemBlockChiseled( blk, new Item.Properties() );

			getConversions().put( mat.type, blk );
			getItemConversions().put( mat.type, item );

			registerBlock( blk, item, blk.name );
		}
	}

	public BlockState getChiseledDefaultState()
	{
		for ( final BlockChiseled bc : getConversions().values() )
		{
			return bc.getDefaultState();
		}
		return null;
	}

	public BlockChiseled getConversion(
			final BlockState material )
	{
		final Fluid f = BlockBitInfo.getFluidFromBlock( material.getBlock() );

		if ( f != null )
		{
			return getConversions().get( Material.WATER );
		}

		BlockChiseled out = getConversions().get( material.getMaterial() );

		if ( out == null )
		{
			// unknown material? just use stone.
			out = getConversions().get( Material.ROCK );
		}

		return out;
	}

	public BlockChiseled getConversionWithDefault(
			final BlockState material )
	{
		final BlockChiseled bcX = getConversion( material );

		if ( bcX == null )
		{
			for ( final BlockChiseled bc : getConversions().values() )
			{
				return bc;
			}
		}

		return bcX;
	}

	public Map<Material, Item> getItemConversions()
	{
		return itemConversions;
	}

	public Map<Material, BlockChiseled> getConversions()
	{
		return conversions;
	}

	public boolean addConversion(
			final Material newMaterial,
			final Material target )
	{
		final BlockChiseled targ = conversions.get( target );

		if ( targ != null && !conversions.containsKey( newMaterial ) )
		{
			BlockBitInfo.reset();
			conversions.put( newMaterial, targ );
			return true;
		}

		return false;
	}

}
