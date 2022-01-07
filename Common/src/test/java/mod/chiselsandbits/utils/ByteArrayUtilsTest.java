package mod.chiselsandbits.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteArrayUtilsTest
{
    @Test
    public void simpleSetInEmptyArray()
    {
        byte[] array = new byte[8];
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0x00, array[1] );
        assertEquals( (byte) 0b00101010, array[2] );
        assertEquals( (byte) 0x00, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0x00, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void simpleSetSingleBitInEmptyArray()
    {
        byte[] array = new byte[8];
        ByteArrayUtils.setValue( array, 0b00000001, 0b1, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0x00, array[1] );
        assertEquals( (byte) 0b00000001, array[2] );
        assertEquals( (byte) 0x00, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0x00, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void noneCollidingSetInArray()
    {
        byte[] array = new byte[] {
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b01100110,
          (byte) 0b00000000
        };
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0x00, array[1] );
        assertEquals( (byte) 0b00101010, array[2] );
        assertEquals( (byte) 0x00, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0b01100110, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void insertBeforeSetInArray()
    {
        byte[] array = new byte[] {
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b11000000,
          (byte) 0b0110011,
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b0000000
        };
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0x00, array[1] );
        assertEquals( (byte) 0b11101010, array[2] );
        assertEquals( (byte) 0b00110011, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0b00000000, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void insertAfterSetInArray()
    {
        byte[] array = new byte[] {
          (byte) 0b0000000,
          (byte) 0b11001100,
          (byte) 0b11000000,
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b0000000,
          (byte) 0b0000000
        };
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0b11001100, array[1] );
        assertEquals( (byte) 0b11101010, array[2] );
        assertEquals( (byte) 0x00, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0b00000000, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void insertCollidingInArray()
    {
        byte[] array = new byte[] {
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b11001111,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000
        };
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );
        assertEquals( (byte) 0x00, array[0] );
        assertEquals( (byte) 0x00, array[1] );
        assertEquals( (byte) 0b11101010, array[2] );
        assertEquals( (byte) 0x00, array[3] );
        assertEquals( (byte) 0x00, array[4] );
        assertEquals( (byte) 0x00, array[5] );
        assertEquals( (byte) 0b00000000, array[6] );
        assertEquals( (byte) 0x00, array[7] );
    }

    @Test
    public void simpleEnsureRoundTrip()
    {
        byte[] array = new byte[] {
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000,
          (byte) 0b00000000
        };
        ByteArrayUtils.setValue( array, 0b1010101010, 0b111111, 16 );

        final int value = ByteArrayUtils.getValue( array, 0b111111, 16 );
        assertEquals( 0b101010, value );
    }
}