package mod.chiselsandbits.helpers;

import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;

public interface IContinuousInventory
{

	void damage(
			int blk );

	void fail(
			int BlockID );

	boolean isValid();

	ItemStackSlot getTool(
			int BlockID );

}
