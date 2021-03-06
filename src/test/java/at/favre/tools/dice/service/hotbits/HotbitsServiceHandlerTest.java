package at.favre.tools.dice.service.hotbits;

import at.favre.lib.bytes.Bytes;
import at.favre.tools.dice.service.randomorg.RandomOrgServiceHandler;
import org.junit.Test;

import java.net.UnknownHostException;

import static at.favre.tools.dice.service.hotbits.HotbitsServiceHandler.ENTROPY_SEED_LENGTH_BYTE;
import static org.junit.Assert.*;

public class HotbitsServiceHandlerTest {
    @Test
    public void getRandom() throws Exception {
        RandomOrgServiceHandler.Result<Void> response = new HotbitsServiceHandler(true).getRandom();

        assertNotNull(response);

        if (response.throwable != null) {
            response.throwable.printStackTrace();
        }

        if (response.throwable != null && response.throwable instanceof UnknownHostException) {
            System.out.printf(response.errorMsg);
        } else {
            assertNotNull(response.seed);
            assertNull(response.response);
            System.out.println(Bytes.from(response.seed).encodeHex());
            assertTrue(response.seed.length == ENTROPY_SEED_LENGTH_BYTE);
        }
    }
}