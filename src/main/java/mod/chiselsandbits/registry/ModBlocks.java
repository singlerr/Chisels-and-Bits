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
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ModBlocks extends ModRegistry
{

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

	private final HashMap<Material, BlockChiseled> conversions = new HashMap<Material, BlockChiseled>();

	public ModBlocks(
			final ModConfig config,
			final Side side )
	{
		// register tile entities.
		GameRegistry.registerTileEntity( TileEntityBitTank.class, "mod.chiselsandbits.TileEntityBitTank" );
		GameRegistry.registerTileEntity( TileEntityBlockChiseled.class, "mod.chiselsandbits.TileEntityChiseled" );

		// register TESR differently on the server..
		GameRegistry.registerTileEntity( side == Side.CLIENT ? TileEntityBlockChiseledTESR.class : TileEntityBlockChiseled.class, "mod.chiselsandbits.TileEntityChiseled.tesr" );

		if ( config.enableBitTank )
		{
			blockBitTank = new BlockBitTank();
			registerBlock( blockBitTank, null, "bittank" );
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

	private void registerBlock(
			final Block block,
			final Class<? extends ItemBlock> itemBlock,
			final String name )
	{
		block.setCreativeTab( creativeTab );
		GameRegistry.registerBlock( block.setUnlocalizedName( unlocalizedPrefix + name ), itemBlock == null ? ItemBlock.class : itemBlock, name );
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
