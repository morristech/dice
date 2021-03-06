package at.favre.tools.dice.util;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteUtilsTest {
    @Test
    public void appendCrc32Checksum() throws Exception {
        byte[] b1 = Bytes.random(32).array();

        byte[] b1PlusCrc32 = new ByteUtils.Crc32AppenderTransformer().transform(b1, false);
        assertTrue(b1.length + 4 == b1PlusCrc32.length);
        assertFalse(Arrays.equals(b1, b1PlusCrc32));
    }
}