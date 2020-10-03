package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PacketAccurateSneakPlace extends ModPacket
{

	public interface IItemBlockAccurate
	{

		ActionResultType tryPlace(
		  ItemUseContext context,
          boolean offGrid
        );

	};

    public PacketAccurateSneakPlace(PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketAccurateSneakPlace(
      final ItemStack stack,
      final BlockPos pos,
      final Hand hand,
      final Direction side,
      final double hitX,
      final double hitY,
      final double hitZ,
      final boolean offgrid)
    {
        this.stack = stack;
        this.pos = pos;
        this.hand = hand;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.offgrid = offgrid;
    }

    private ItemStack stack;
    private BlockPos  pos;
    private Hand      hand;
    private Direction side;
    private double     hitX, hitY, hitZ;
	private boolean offgrid;

	@Override
	public void server(
			final ServerPlayerEntity playerEntity )
	{
		if ( stack != null && stack.getItem() instanceof IItemBlockAccurate )
		{
			ItemStack inHand = playerEntity.getHeldItem( hand );
			if ( ItemStack.areItemStackTagsEqual( stack, inHand ) )
			{
				if ( playerEntity.isCreative() )
				{
					inHand = stack;
				}

				final IItemBlockAccurate ibc = (IItemBlockAccurate) stack.getItem();
				final ItemUseContext context = new ItemUseContext(
				  playerEntity,
                  hand,
                  new BlockRayTraceResult(new Vector3d(hitX, hitY, hitZ), side, pos, false)
                );
				ibc.tryPlace(new BlockItemUseContext(context), offgrid);

				if ( !playerEntity.isCreative() && ModUtil.getStackSize( inHand ) <= 0 )
				{
					playerEntity.setHeldItem( hand, ModUtil.getEmptyStack() );
				}
			}
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeItemStack( stack );
		buffer.writeBlockPos( pos );
		buffer.writeEnumValue( side );
		buffer.writeEnumValue( hand );
		buffer.writeDouble( hitX );
		buffer.writeDouble( hitY );
		buffer.writeDouble( hitZ );
		buffer.writeBoolean( offgrid );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
        stack = buffer.readItemStack();
        pos = buffer.readBlockPos();
        side = buffer.readEnumValue( Direction.class );
        hand = buffer.readEnumValue( Hand.class );
        hitX = buffer.readDouble();
        hitY = buffer.readDouble();
        hitZ = buffer.readDouble();
        offgrid = buffer.readBoolean();
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public void setStack(final ItemStack stack)
    {
        this.stack = stack;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public void setPos(final BlockPos pos)
    {
        this.pos = pos;
    }

    public Hand getHand()
    {
        return hand;
    }

    public void setHand(final Hand hand)
    {
        this.hand = hand;
    }

    public Direction getSide()
    {
        return side;
    }

    public void setSide(final Direction side)
    {
        this.side = side;
    }

    public double getHitX()
    {
        return hitX;
    }

    public void setHitX(final double hitX)
    {
        this.hitX = hitX;
    }

    public double getHitY()
    {
        return hitY;
    }

    public void setHitY(final double hitY)
    {
        this.hitY = hitY;
    }

    public double getHitZ()
    {
        return hitZ;
    }

    public void setHitZ(final double hitZ)
    {
        this.hitZ = hitZ;
    }

    public boolean isOffgrid()
    {
        return offgrid;
    }

    public void setOffgrid(final boolean offgrid)
    {
        this.offgrid = offgrid;
    }
}
