package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public enum MultiPartSlots implements IPartSlot
{
	BITS;

	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation( ChiselsAndBits.MODID, "Bits" );
	}

	@Override
	public EnumSlotAccess getFaceAccess(
			EnumFacing face )
	{
		return EnumSlotAccess.NONE;
	}

	@Override
	public int getFaceAccessPriority(
			EnumFacing face )
	{
		return 0;
	}

	@Override
	public EnumSlotAccess getEdgeAccess(
			EnumEdgeSlot edge,
			EnumFacing face )
	{
		return EnumSlotAccess.NONE;
	}

	@Override
	public int getEdgeAccessPriority(
			EnumEdgeSlot edge,
			EnumFacing face )
	{
		return 0;
	}

}
