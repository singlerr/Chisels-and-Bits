package mod.chiselsandbits.chiseledblock.data;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;

public class BitOcclusionIterator extends BitCollisionIterator
{

	private float physicalStartX = 0.0f;
	private boolean lastSetting = false;

	final List<AxisAlignedBB> o;

	public BitOcclusionIterator(
			final List<AxisAlignedBB> out )
	{
		o = out;
	}

	@Override
	protected void yPlus()
	{
		done();
		super.yPlus();
	}

	@Override
	protected void zPlus()
	{
		done();
		super.zPlus();
	}

	@Override
	protected void done()
	{
		if ( lastSetting == true )
		{
			addBox();
			lastSetting = false;
		}
	}

	private void addBox()
	{
		o.add( AxisAlignedBB.fromBounds( physicalStartX, physicalY, physicalZ, physicalX + One16thf, physicalYp1, physicalZp1 ) );
	}

	public void add()
	{
		if ( !lastSetting )
		{
			physicalStartX = physicalX;
			lastSetting = true;
		}
	}

	public void drop()
	{
		done();
	}

}
