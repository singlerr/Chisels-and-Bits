package mod.chiselsandbits.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ClassCanBeRecord")
@RunWith(Parameterized.class)
public class BitWidthMaskTest
{
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return IntStream.range(1, Integer.SIZE + 1)
                 .mapToObj(i -> new Object[] { i })
                 .collect(Collectors.toList());
    }

    public final int testWidth;

    public BitWidthMaskTest(final int testWidth) {this.testWidth = testWidth;}

    @Test
    public void getBitMask()
    {
        final int mask = BitUtils.getBitMask(testWidth);

        for (int i = 0; i < testWidth; i++)
        {
            final int checkMask = 1 << i;
            Assert.assertTrue("The mask for a bit width of " + testWidth + " should have a bit at index " + i, (mask & checkMask) != 0);
        }

        for (int i = testWidth; i < Integer.SIZE; i++)
        {
            final int checkMask = 1 << i;
            Assert.assertEquals("The mask for a bit width of " + testWidth + " should not have a bit at index " + i, 0, (mask & checkMask));
        }
    }

    @Test
    public void getMaskedBitWidth()
    {
        final int mask = BitUtils.getBitMask(testWidth);
        final int resultingWidth = BitUtils.getMaskWidth(mask);

        Assert.assertEquals("The masked bit width should be the same as the bit width", testWidth, resultingWidth);
    }
}