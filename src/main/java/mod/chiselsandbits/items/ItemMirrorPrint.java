package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.helpers.SimpleInstanceCache;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMirrorPrint extends Item implements IPatternItem
{

	public ItemMirrorPrint(Item.Properties properties)
	{
        super(properties);
	}

	SimpleInstanceCache<ItemStack, List<ITextComponent>> toolTipCache = new SimpleInstanceCache<>(null, new ArrayList<>());

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpMirrorPrint, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindUseItem ) );

		if ( isWritten( stack ) )
		{
			if ( ClientSide.instance.holdingShift() )
			{
				if ( toolTipCache.needsUpdate( stack ) )
				{
					final VoxelBlob blob = ModUtil.getBlobFromStack( stack, null );
					toolTipCache.updateCachedValue( blob.listContents(new ArrayList<>() ) );
				}

				tooltip.addAll( toolTipCache.getCached() );
			}
			else
			{
				tooltip.add( new StringTextComponent(LocalStrings.ShiftDetails.getLocal()) );
			}
		}
	}

    @Override
    public String getTranslationKey(final ItemStack stack)
    {
        if ( isWritten( stack ) )
        {
            return super.getTranslationKey( stack ) + "_written";
        }

        return super.getTranslationKey( stack );
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final ItemStack stack = context.getPlayer().getHeldItem( context.getHand() );

        if ( !context.getPlayer().canPlayerEdit( context.getPos(), context.getFace(), stack ) )
        {
            return ActionResultType.SUCCESS;
        }

        if ( !isWritten( stack ) )
        {
            final CompoundNBT comp = getCompoundFromBlock( context.getWorld(), context.getPos(), context.getPlayer(), context.getFace() );
            if ( comp != null )
            {
                stack.shrink(1);

                final ItemStack newStack = new ItemStack(ModItems.ITEM_MIRROR_PRINT_WRITTEN.get(), 1);
                newStack.setTag(comp);

                final ItemEntity entity = context.getPlayer().dropItem(newStack, true);
                entity.setPickupDelay(0);
                entity.setThrowerId(context.getPlayer().getUniqueID());

                return ActionResultType.SUCCESS;
            }

            return ActionResultType.FAIL;
        }

        return ActionResultType.FAIL;
    }

	protected CompoundNBT getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final PlayerEntity player,
			final Direction face )
	{
		final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( world, pos, false );

		if ( te != null )
		{
			final CompoundNBT comp = new CompoundNBT();
			te.writeChiselData( comp );

			final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
			tmp.readChiselData( comp );

			final VoxelBlob bestBlob = tmp.getBlob();
			tmp.setBlob( bestBlob.mirror( face.getAxis() ) );
			tmp.writeChiselData( comp );

			comp.putByte( ModUtil.NBT_SIDE, (byte) ModUtil.getPlaceFace( player ).ordinal() );
			return comp;
		}

		return null;
	}

	@Override
	public ItemStack getPatternedItem(
			final ItemStack stack,
			final boolean wantRealItems )
	{
		if ( !isWritten( stack ) )
		{
			return null;
		}

		final CompoundNBT tag = ModUtil.getTagCompound( stack );

		// Detect and provide full blocks if pattern solid full and solid.
		final NBTBlobConverter conv = new NBTBlobConverter();
		conv.readChisleData( tag, VoxelBlob.VERSION_ANY );

		final BlockState blk = conv.getPrimaryBlockState();
		final ItemStack itemstack = new ItemStack(ModBlocks.convertGivenStateToChiseledBlock(blk), 1 );

		itemstack.setTagInfo( ModUtil.NBT_BLOCKENTITYTAG, tag );
		return itemstack;
	}

	@Override
	public boolean isWritten(
			final ItemStack stack )
	{
	    return stack.getItem() == ModItems.ITEM_MIRROR_PRINT_WRITTEN.get() && stack.hasTag();
	}

}