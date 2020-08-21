package mod.chiselsandbits.items;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.sun.org.apache.xpath.internal.operations.String;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.VoxelStats;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.BitInventoryFeeder;
import mod.chiselsandbits.helpers.ContinousChisels;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import mod.chiselsandbits.network.packets.PacketRotateVoxelBlob;
import mod.chiselsandbits.render.helpers.SimpleInstanceCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemNegativePrint extends Item implements IVoxelBlobItem, IItemScrollWheel, IPatternItem
{

	public ItemNegativePrint(Item.Properties properties)
	{
        super(properties);
	}

	@OnlyIn( Dist.CLIENT )
	protected void defaultAddInfo(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
	}

	// add info cached info
	SimpleInstanceCache<ItemStack, List<ITextComponent>> toolTipCache = new SimpleInstanceCache<>(null, new ArrayList<>());

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		defaultAddInfo( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpNegativePrint, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindUseItem ),
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindUseItem ) );

		if ( isWritten( stack ) )
		{
			if ( ClientSide.instance.holdingShift() )
			{
				final List<ITextComponent> details = toolTipCache.getCached();

				if ( toolTipCache.needsUpdate( stack ) )
				{
					details.clear();

					final VoxelBlob blob = ModUtil.getBlobFromStack( stack, null );

					final int solid = blob.filled();
					final int air = blob.air();

					if ( solid > 0 )
					{
                        details.add( new StringTextComponent(Integer.valueOf(solid).toString()).appendString(" ").append(new StringTextComponent(LocalStrings.Filled.getLocal()) ));
					}

					if ( air > 0 )
					{
						details.add( new StringTextComponent(Integer.valueOf(air).toString()).appendString(" ").append(new StringTextComponent(LocalStrings.Empty.getLocal()) ));
					}
				}

				tooltip.addAll( details );
			}
			else
			{
				tooltip.add( new StringTextComponent(LocalStrings.ShiftDetails.getLocal()) );
			}
		}
	}

	@Override
	public boolean isWritten(
			final ItemStack stack )
	{
	    if (stack.getItem() == ChiselsAndBits.getItems().itemNegativePrint || stack.getItem() == ChiselsAndBits.getItems().itemPositiveprint)
	        return false;

		if ( stack != null && stack.hasTag() )
		{
			final boolean a = ModUtil.getSubCompound( stack, ModUtil.NBT_BLOCKENTITYTAG, false ) != null;
			final boolean b = ModUtil.getTagCompound( stack ).contains( NBTBlobConverter.NBT_LEGACY_VOXEL );
			final boolean c = ModUtil.getTagCompound( stack ).contains( NBTBlobConverter.NBT_VERSIONED_VOXEL );
			return a || b || c;
		}
		return false;
	}

    protected Item getWrittenItem() {
	    return ChiselsAndBits.getItems().itemNegativePrintWritten;
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final PlayerEntity player = context.getPlayer();
        final Hand hand = context.getHand();
        final World world = context.getWorld();
        final BlockPos pos = context.getPos();
        final Direction side = context.getFace();

        final ItemStack stack = player.getHeldItem( hand );
        final BlockState blkstate = world.getBlockState( pos );

        if ( ItemChiseledBit.checkRequiredSpace( player, blkstate ) )
        {
            return ActionResultType.FAIL;
        }

        if ( !player.canPlayerEdit( pos, side, stack ) || !world.isBlockModifiable( player, pos ) )
        {
            return ActionResultType.FAIL;
        }

        if ( !isWritten( stack ) )
        {
            final CompoundNBT comp = getCompoundFromBlock( world, pos, player );
            if ( comp != null )
            {
                stack.shrink(stack.getCount());

                final ItemStack newStack = new ItemStack(
                  this::getWrittenItem,
                  stack.getCount()
                );
                newStack.setTag(comp);

                ItemEntity itementity = player.dropItem(newStack, false);
                if (itementity != null) {
                    itementity.setNoPickupDelay();
                    itementity.setOwnerId(player.getUniqueID());
                }
                return ActionResultType.SUCCESS;
            }

            return ActionResultType.FAIL;
        }

        final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( world, pos, false );
        if ( te != null )
        {
            // we can do this!
        }
        else if ( !BlockChiseled.replaceWithChisled( world, pos, blkstate, true ) )
        {
            return ActionResultType.FAIL;
        }

        final TileEntityBlockChiseled tec = ModUtil.getChiseledTileEntity( world, pos, true );
        if ( tec != null )
        {
            final VoxelBlob vb = tec.getBlob();

            final VoxelBlob pattern = ModUtil.getBlobFromStack( stack, player );

            applyPrint( stack, world, pos, side, vb, pattern, player, hand );

            tec.completeEditOperation( vb );
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

	protected boolean convertToStone()
	{
		return true;
	}

	protected CompoundNBT getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final PlayerEntity player )
	{

		final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( world, pos, false );
		if ( te != null )
		{
			final CompoundNBT comp = new CompoundNBT();
			te.writeChisleData( comp );

			if ( convertToStone() )
			{
				final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
				tmp.readChisleData( comp );

				final VoxelBlob bestBlob = tmp.getBlob();
				bestBlob.binaryReplacement( 0, ModUtil.getStateId( Blocks.STONE.getDefaultState() ) );

				tmp.setBlob( bestBlob );
				tmp.writeChisleData( comp );
			}

			comp.putByte( ModUtil.NBT_SIDE, (byte) ModUtil.getPlaceFace( player ).ordinal() );
			return comp;
		}

		return null;
	}

	@Override
	public ItemStack getPatternedItem(
			final ItemStack stack,
			final boolean craftingBlocks )
	{
		if ( !isWritten( stack ) )
		{
			return null;
		}

		final CompoundNBT tag = ModUtil.getTagCompound( stack );

		// Detect and provide full blocks if pattern solid full and solid.
		final NBTBlobConverter conv = new NBTBlobConverter();
		conv.readChisleData( tag, VoxelBlob.VERSION_ANY );

		if ( craftingBlocks && ChiselsAndBits.getConfig().fullBlockCrafting )
		{
			final VoxelStats stats = conv.getBlob().getVoxelStats();
			if ( stats.isFullBlock )
			{
				final BlockState state = ModUtil.getStateById( stats.mostCommonState );
				final ItemStack is = ModUtil.getItemStackFromBlockState( state );

				if ( !ModUtil.isEmpty( is ) )
				{
					return is;
				}
			}
		}

		final BlockState state = conv.getPrimaryBlockState();
		final ItemStack itemstack = new ItemStack( ChiselsAndBits.getBlocks().getConversionWithDefault( state ), 1 );

		itemstack.setTagInfo( ModUtil.NBT_BLOCKENTITYTAG, tag );
		return itemstack;
	}

	protected void applyPrint(
			@Nonnull final ItemStack stack,
			@Nonnull final World world,
			@Nonnull final BlockPos pos,
			@Nonnull final Direction side,
			@Nonnull final VoxelBlob vb,
			@Nonnull final VoxelBlob pattern,
			@Nonnull final PlayerEntity who,
			@Nonnull final Hand hand )
	{
		// snag a tool...
		final ActingPlayer player = ActingPlayer.actingAs( who, hand );
		final IContinuousInventory selected = new ContinousChisels( player, pos, side );
		ItemStack spawnedItem = null;

		final List<ItemEntity> spawnlist = new ArrayList<ItemEntity>();

		for ( int z = 0; z < vb.detail && selected.isValid(); z++ )
		{
			for ( int y = 0; y < vb.detail && selected.isValid(); y++ )
			{
				for ( int x = 0; x < vb.detail && selected.isValid(); x++ )
				{
					final int blkID = vb.get( x, y, z );
					if ( blkID != 0 && pattern.get( x, y, z ) == 0 )
					{
						spawnedItem = ItemChisel.chiselBlock( selected, player, vb, world, pos, side, x, y, z, spawnedItem, spawnlist );
					}
				}
			}
		}

		BitInventoryFeeder feeder = new BitInventoryFeeder( who, world );
		for ( final ItemEntity ei : spawnlist )
		{
			feeder.addItem( ei );
		}
	}

	@Override
	public void scroll(
			final PlayerEntity player,
			final ItemStack stack,
			final int dwheel )
	{
		final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob(Axis.Y, dwheel > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
		ChiselsAndBits.getNetworkChannel().sendToServer(p);
	}

	@Override
	public void rotate(
			final ItemStack stack,
			final Direction.Axis axis,
			final Rotation rotation )
	{
		Direction side = ModUtil.getSide( stack );

		if ( axis == Axis.Y )
		{
			if ( side.getAxis() == Axis.Y )
			{
				side = Direction.NORTH;
			}

			switch ( rotation )
			{
				case CLOCKWISE_180:
					side = side.rotateY();
				case CLOCKWISE_90:
					side = side.rotateY();
					break;
				case COUNTERCLOCKWISE_90:
					side = side.rotateYCCW();
					break;
				default:
				case NONE:
					break;
			}
		}
		else
		{
			IBitAccess ba = ChiselsAndBits.getApi().createBitItem( stack );
			ba.rotate( axis, rotation );
			stack.setTag( ba.getBitsAsItem( side, ChiselsAndBits.getApi().getItemType( stack ), false ).getTag() );
		}

		ModUtil.setSide( stack, side );
	}

}