package mod.chiselsandbits.chiseledblock;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketRotateVoxelBlob;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockChiseled extends ItemBlock implements IVoxelBlobItem, IItemScrollWheel
{

	public ItemBlockChiseled(
			final Block block )
	{
		super( block );
	}

	// add info cached info
	ItemStack cachedInfo;
	List<String> details = new ArrayList<String>();

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpChiseledBlock, tooltip );

		if ( stack.hasTagCompound() )
		{
			if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{
				if ( cachedInfo != stack )
				{
					cachedInfo = stack;
					details.clear();

					final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
					tmp.readChisleData( stack.getSubCompound( "BlockEntityTag", false ) );
					final VoxelBlob blob = tmp.getBlob();

					blob.listContents( details );
				}

				tooltip.addAll( details );
			}
			else
			{
				tooltip.add( LocalStrings.ShiftDetails.getLocal() );
			}
		}
	}

	@Override
	public int getColorFromItemStack(
			final ItemStack stack,
			final int renderPass )
	{
		return super.getColorFromItemStack( stack, renderPass );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean canPlaceBlockOnSide(
			final World worldIn,
			final BlockPos pos,
			final EnumFacing side,
			final EntityPlayer player,
			final ItemStack stack )
	{
		return canPlaceBlockHere( worldIn, pos, side, player, stack );
	}

	public boolean vanillaStylePlacementTest(
			final World worldIn,
			BlockPos pos,
			EnumFacing side,
			final EntityPlayer player,
			final ItemStack stack )
	{
		final Block block = worldIn.getBlockState( pos ).getBlock();

		if ( block == Blocks.snow_layer )
		{
			side = EnumFacing.UP;
		}
		else if ( !block.isReplaceable( worldIn, pos ) )
		{
			pos = pos.offset( side );
		}

		return worldIn.canBlockBePlaced( this.block, pos, false, side, (Entity) null, stack );
	}

	public boolean canPlaceBlockHere(
			final World worldIn,
			final BlockPos pos,
			final EnumFacing side,
			final EntityPlayer player,
			final ItemStack stack )
	{
		if ( vanillaStylePlacementTest( worldIn, pos, side, player, stack ) )
		{
			return true;
		}

		if ( player.isSneaking() )
		{
			return true;
		}

		if ( tryPlaceBlockAt( block, stack, player, worldIn, pos, side, null, false ) )
		{
			return true;
		}

		return tryPlaceBlockAt( block, stack, player, worldIn, pos.offset( side ), side, null, false );
	}

	@Override
	public boolean onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			BlockPos pos,
			EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final IBlockState iblockstate = worldIn.getBlockState( pos );
		final Block block = iblockstate.getBlock();

		if ( block == Blocks.snow_layer && iblockstate.getValue( BlockSnow.LAYERS ).intValue() < 1 )
		{
			side = EnumFacing.UP;
		}
		else
		{
			boolean canMerge = false;
			if ( stack.hasTagCompound() )
			{
				final TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( worldIn, pos, true );

				if ( tebc != null )
				{
					final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
					tmp.readChisleData( stack.getSubCompound( "BlockEntityTag", false ) );
					VoxelBlob blob = tmp.getBlob();

					int rotations = ModUtil.getRotations( playerIn, stack.getTagCompound().getByte( "side" ) );
					while ( rotations-- > 0 )
					{
						blob = blob.spin( Axis.Y );
					}

					canMerge = tebc.canMerge( blob );
				}
			}

			if ( !canMerge && !playerIn.isSneaking() && !block.isReplaceable( worldIn, pos ) )
			{
				pos = pos.offset( side );
			}
		}

		if ( stack.stackSize == 0 )
		{
			return false;
		}
		else if ( !playerIn.canPlayerEdit( pos, side, stack ) )
		{
			return false;
		}
		else if ( pos.getY() == 255 && this.block.getMaterial().isSolid() )
		{
			return false;
		}
		else if ( canPlaceBlockHere( worldIn, pos, side, playerIn, stack ) )
		{
			final int i = this.getMetadata( stack.getMetadata() );
			final IBlockState iblockstate1 = this.block.onBlockPlaced( worldIn, pos, side, hitX, hitY, hitZ, i, playerIn );

			if ( placeBlockAt( stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, iblockstate1 ) )
			{
				worldIn.playSoundEffect( pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.block.stepSound.getPlaceSound(), ( this.block.stepSound.getVolume() + 1.0F ) / 2.0F, this.block.stepSound.getFrequency() * 0.8F );
				--stack.stackSize;
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean placeBlockAt(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ,
			final IBlockState newState )
	{
		if ( player.isSneaking() )
		{
			final BitLocation bl = new BitLocation( new MovingObjectPosition( MovingObjectType.BLOCK, new Vec3( hitX, hitY, hitZ ), side, pos ), false, ChiselToolType.BIT );
			return tryPlaceBlockAt( block, stack, player, world, bl.blockPos, side, new BlockPos( bl.bitX, bl.bitY, bl.bitZ ), true );
		}
		else
		{
			return tryPlaceBlockAt( block, stack, player, world, pos, side, null, true );
		}
	}

	static public boolean tryPlaceBlockAt(
			final Block block,
			final ItemStack stack,
			final EntityLivingBase player,
			final World world,
			BlockPos pos,
			final EnumFacing side,
			final BlockPos partial,
			final boolean modulateWorld )
	{
		try
		{
			final VoxelBlob[][][] blobs = new VoxelBlob[2][2][2];

			final TileEntityBlockChiseled tebc = new TileEntityBlockChiseled();
			tebc.readChisleData( stack.getSubCompound( "BlockEntityTag", false ) );
			VoxelBlob source = tebc.getBlob();

			int rotations = ModUtil.getRotations( player, stack.getTagCompound().getByte( "side" ) );
			while ( rotations-- > 0 )
			{
				source = source.spin( Axis.Y );
			}

			final IntegerBox modelBounds = source.getBounds();
			BlockPos offset = partial == null ? new BlockPos( 0, 0, 0 ) : ModUtil.getPartialOffset( side, partial, modelBounds );
			final BlockChiseled myBlock = (BlockChiseled) block;

			if ( offset.getX() < 0 )
			{
				pos = pos.add( -1, 0, 0 );
				offset = offset.add( VoxelBlob.dim, 0, 0 );
			}

			if ( offset.getY() < 0 )
			{
				pos = pos.add( 0, -1, 0 );
				offset = offset.add( 0, VoxelBlob.dim, 0 );
			}

			if ( offset.getZ() < 0 )
			{
				pos = pos.add( 0, 0, -1 );
				offset = offset.add( 0, 0, VoxelBlob.dim );
			}

			for ( int x = 0; x < 2; x++ )
			{
				for ( int y = 0; y < 2; y++ )
				{
					for ( int z = 0; z < 2; z++ )
					{
						blobs[x][y][z] = source.offset( offset.getX() - source.detail * x, offset.getY() - source.detail * y, offset.getZ() - source.detail * z );
						final int solids = blobs[x][y][z].solid();
						if ( solids > 0 )
						{
							final BlockPos bp = pos.add( x, y, z );

							if ( world.isAirBlock( bp ) || world.getBlockState( bp ).getBlock().isReplaceable( world, bp ) )
							{
								continue;
							}

							final TileEntityBlockChiseled target = ModUtil.getChiseledTileEntity( world, bp, true );
							if ( target != null )
							{
								if ( !target.canMerge( blobs[x][y][z] ) )
								{
									return false;
								}

								blobs[x][y][z] = blobs[x][y][z].merge( target.getBlob() );
								continue;
							}

							return false;
						}
					}
				}
			}

			if ( modulateWorld )
			{
				for ( int x = 0; x < 2; x++ )
				{
					for ( int y = 0; y < 2; y++ )
					{
						for ( int z = 0; z < 2; z++ )
						{
							if ( blobs[x][y][z].solid() > 0 )
							{
								final BlockPos bp = pos.add( x, y, z );
								final IBlockState state = world.getBlockState( bp );

								if ( world.getBlockState( bp ).getBlock().isReplaceable( world, bp ) )
								{
									// clear it...
									world.setBlockToAir( bp );
								}

								if ( world.isAirBlock( bp ) )
								{
									if ( BlockChiseled.replaceWithChisled( world, bp, state, tebc.getBasicState().getValue( BlockChiseled.block_prop ) ) )
									{
										final TileEntityBlockChiseled target = myBlock.getTileEntity( world, bp );
										target.setBlob( blobs[x][y][z] );
									}

									continue;
								}

								final TileEntityBlockChiseled target = ModUtil.getChiseledTileEntity( world, bp, true );
								if ( target != null )
								{
									target.setBlob( blobs[x][y][z] );

									continue;
								}

								return false;
							}
						}
					}
				}
			}

			return true;
		}
		catch ( final ExceptionNoTileEntity e )
		{
			return false;
		}
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		final NBTTagCompound comp = stack.getTagCompound();

		if ( comp != null )
		{
			final NBTTagCompound BlockEntityTag = comp.getCompoundTag( "BlockEntityTag" );
			if ( BlockEntityTag != null )
			{
				final int stateid = BlockEntityTag.getInteger( "b" );

				final IBlockState state = Block.getStateById( stateid );
				final Block blk = state.getBlock();

				final ItemStack target = new ItemStack( blk, 1, blk.getMetaFromState( state ) );

				if ( target.getItem() != null )
				{
					return new StringBuilder().append( super.getItemStackDisplayName( stack ) ).append( " - " ).append( target.getDisplayName() ).toString();
				}
			}
		}

		return super.getItemStackDisplayName( stack );
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob();
		p.wheel = dwheel;
		NetworkRouter.instance.sendToServer( p );
	}

	@Override
	public void rotate(
			final ItemStack stack,
			final int wheel )
	{
		final NBTTagCompound blueprintTag = stack.getTagCompound();
		EnumFacing side = EnumFacing.VALUES[blueprintTag.getByte( "side" )];

		if ( side == EnumFacing.DOWN || side == EnumFacing.UP )
		{
			side = EnumFacing.NORTH;
		}

		side = wheel > 0 ? side.rotateY() : side.rotateYCCW();
		blueprintTag.setInteger( "side", +side.ordinal() );
	}

}
