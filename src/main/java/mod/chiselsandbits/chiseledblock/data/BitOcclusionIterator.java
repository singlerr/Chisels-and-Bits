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
		addCurrentBox( One16thf );
		super.yPlus();
	}

	@Override
	protected void zPlus()
	{
		addCurrentBox( One16thf );
		super.zPlus();
	}

	@Override
	protected void done()
	{
		addCurrentBox( One16thf );
	}

	protected void addCurrentBox(
			final double addition )
	{
		if ( lastSetting == true )
		{
			addBox( addition );
			lastSetting = false;
		}
	}

	private void addBox(
			final double addition )
	{
		final double epsilon = 0.00001;
		o.add( AxisAlignedBB.fromBounds( physicalStartX + epsilon, physicalY + epsilon, physicalZ + epsilon, physicalX + addition - epsilon, physicalYp1 - epsilon, physicalZp1 - epsilon ) );
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
		addCurrentBox( 0 );
	}

}
