package mod.chiselsandbits.helpers;

import mod.chiselsandbits.chiseledblock.data.BitState;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;

public interface IContinuousInventory
{

	void useItem(
			BitState blockId );

	void fail(
			BitState blockId );

	boolean isValid();

	ItemStackSlot getItem(
			BitState blockId );

}
