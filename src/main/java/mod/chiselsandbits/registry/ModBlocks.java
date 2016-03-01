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
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ModBlocks extends ModRegistry
{

	// TE Registration names.
	private static String TE_BIT_TANK = "mod.chiselsandbits.TileEntityBitTank";
	private static String TE_CHISELEDBLOCK = "mod.chiselsandbits.TileEntityChiseled";
	private static String TE_CHISELEDBLOCK_TESR = "mod.chiselsandbits.TileEntityChiseled.tesr";

	private final HashMap<Material, BlockChiseled> conversions = new HashMap<Material, BlockChiseled>();

	public final BlockBitTank blockBitTank;

	public static final MaterialType[] validMaterials = new MaterialType[] {
		new MaterialType( "wood", Material.wood ),
		new MaterialType( "rock", Material.rock ),
		new MaterialType( "iron", Material.iron ),
		new MaterialType( "cloth", Material.cloth ),
		new MaterialType( "ice", Material.ice ),
		new MaterialType( "packedIce", Material.packedIce ),
		new MaterialType( "clay", Material.clay ),
		new MaterialType( "glass", Material.glass ),
		new MaterialType( "sand", Material.sand ),
		new MaterialType( "ground", Material.ground ),
		new MaterialType( "grass", Material.grass ),
		new MaterialType( "snow", Material.snow ),
		new MaterialType( "fluid", Material.water ),
		new MaterialType( "leaves", Material.leaves ),
	};

	public ModBlocks(
			final ModConfig config,
			final Side side )
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
			GameRegistry.registerTileEntity( TileEntityBlockChiseled.class, TE_CHISELEDBLOCK_TESR );
		}

		if ( config.enableBitTank )
		{
			blockBitTank = new BlockBitTank();
			registerBlock( blockBitTank, null, "bittank" );
			GameRegistry.registerTileEntity( TileEntityBitTank.class, TE_BIT_TANK );
		}
		else
		{
			blockBitTank = null;
		}

		// register blocks...
		for ( final MaterialType mat : validMaterials )
		{
			final BlockChiseled blk = new BlockChiseled( mat.type, "chiseled_" + mat.name );
			getConversions().put( mat.type, blk );
			registerBlock( blk, ItemBlockChiseled.class, blk.name );
		}
	}

	public void addRecipes()
	{
		ShapedOreRecipe( blockBitTank, " G ", "GOG", " I ", 'G', "blockGlass", 'O', "logWood", 'I', "ingotIron" );
	}

	public IBlockState getChiseledDefaultState()
	{
		for ( final BlockChiseled bc : getConversions().values() )
		{
			return bc.getDefaultState();
		}
		return null;
	}

	public BlockChiseled getConversion(
			final Block material )
	{
		final Fluid f = BlockBitInfo.getFluidFromBlock( material );

		if ( f != null )
		{
			return getConversions().get( Material.water );
		}

		return getConversions().get( material.getMaterial() );
	}

	public BlockChiseled getConversionWithDefault(
			final Block material )
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

	public Map<Material, BlockChiseled> getConversions()
	{
		return conversions;
	}

}
