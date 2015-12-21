package mod.chiselsandbits.chiseledblock.data;

public class IntegerBox
{
	public IntegerBox(
			final int x1,
			final int y1,
			final int z1,
			final int x2,
			final int y2,
			final int z2 )
	{
		minX = x1;
		maxX = x2;

		minY = y1;
		maxY = y2;

		minZ = z1;
		maxZ = z2;
	}

	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;

}
