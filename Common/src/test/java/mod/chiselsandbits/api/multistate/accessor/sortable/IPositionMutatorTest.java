package mod.chiselsandbits.api.multistate.accessor.sortable;

import net.minecraft.core.Vec3i;
import org.junit.Assert;
import org.junit.Test;

public class IPositionMutatorTest
{

    final Vec3i testVector = new Vec3i(1, 2, 3);

    @Test
    public void identity()
    {
        Assert.assertEquals(new Vec3i(1,2,3), IPositionMutator.identity().mutate(testVector));
    }

    @Test
    public void xyz()
    {
        Assert.assertEquals(new Vec3i(1,2,3), IPositionMutator.xyz().mutate(testVector));
    }

    @Test
    public void xzy()
    {
        Assert.assertEquals(new Vec3i(1,3,2), IPositionMutator.xzy().mutate(testVector));
    }

    @Test
    public void zyx()
    {
        Assert.assertEquals(new Vec3i(3,2,1), IPositionMutator.zyx().mutate(testVector));
    }

    @Test
    public void yxz()
    {
        Assert.assertEquals(new Vec3i(2,1,3), IPositionMutator.yxz().mutate(testVector));
    }

    @Test
    public void yzx()
    {
        Assert.assertEquals(new Vec3i(2,3,1), IPositionMutator.yzx().mutate(testVector));
    }
}