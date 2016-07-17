package mod.chiselsandbits.render.helpers;

import net.minecraft.client.renderer.GLAllocation;

public class DeleteDisplayList implements Runnable
{

	final int dspList;

	public DeleteDisplayList(
			final int x )
	{
		dspList = x;
	}

	@Override
	public void run()
	{
		GLAllocation.deleteDisplayLists( dspList );
	}

}