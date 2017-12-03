package mod.chiselsandbits.blueprints;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class BlueprintRenderWorld implements IBlockAccess
{

	int sizeX, sizeY, sizeZ, sizeXY;
	int badPos = 0;
	IBlockState[] states;
	TileEntityBlockChiseled[] tiles;

	public BlueprintRenderWorld(
			final BlueprintData data )
	{
		sizeX = data.getXSize();
		sizeY = data.getYSize();
		sizeZ = data.getZSize();
		sizeXY = sizeX * sizeY;
		int size = sizeX * sizeY * sizeZ;
		badPos = size;
		size++;

		states = new IBlockState[size];
		tiles = new TileEntityBlockChiseled[size];

		for ( final BlockPos p : BlockPos.getAllInBoxMutable( BlockPos.ORIGIN, new BlockPos( sizeX, sizeY, sizeZ ) ) )
		{
			states[getOffset( p )] = data.getStateAt( p );
			tiles[getOffset( p )] = data.getTileAt( p );
		}

		states[badPos] = Blocks.AIR.getDefaultState();
	}

	public TileEntityBlockChiseled getChisledEntity(
			final BlockPos pos )
	{
		return tiles[getOffset( pos )];
	}

	@Override
	public TileEntity getTileEntity(
			final BlockPos pos )
	{
		return tiles[getOffset( pos )];
	}

	@Override
	public int getCombinedLight(
			final BlockPos pos,
			final int lightValue )
	{
		return 0xff << 16 | 0xff;
	}

	@Override
	public IBlockState getBlockState(
			final BlockPos pos )
	{
		return states[getOffset( pos )];
	}

	@Override
	public boolean isAirBlock(
			final BlockPos pos )
	{
		return getBlockState( pos ).getBlock() == Blocks.AIR;
	}

	@Override
	public Biome getBiome(
			BlockPos pos )
	{
		return Biomes.PLAINS;
	}

	@Override
	public int getStrongPower(
			final BlockPos pos,
			final EnumFacing direction )
	{
		return 0;
	}

	@Override
	public WorldType getWorldType()
	{
		return WorldType.DEFAULT;
	}

	@Override
	public boolean isSideSolid(
			final BlockPos pos,
			final EnumFacing side,
			final boolean WAT )
	{
		return false;// getBlockState( pos ).isSideSolid( this, pos, side );
	}

	int getOffset(
			final BlockPos p )
	{
		if ( p.getX() < 0 || p.getY() < 0 || p.getZ() < 0 || p.getX() >= sizeX || p.getY() >= sizeY || p.getZ() >= sizeZ )
		{
			return badPos;
		}

		return p.getX() + p.getY() * sizeX + p.getZ() * sizeXY;
	}

}
