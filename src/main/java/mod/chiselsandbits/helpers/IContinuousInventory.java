package mod.chiselsandbits.helpers;

import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;

public interface IContinuousInventory
{

	void useItem(
			int blockId );

	void fail(
			int blockId );

	boolean isValid();

	ItemStackSlot getItem(
			int blockId );

	default boolean useItem(
			int state,
			int fullSize )
	{
		if ( hasUses( state, fullSize ) )
		{
			for ( int x = 0; x < fullSize; x++ )
				useItem( state );

			return true;
		}

		return false;
	}

	boolean hasUses(
			int state,
			int fullSize );

}
