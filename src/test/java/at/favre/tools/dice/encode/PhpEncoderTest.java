package at.favre.tools.dice.encode;

import at.favre.tools.dice.encode.languages.PhpEncoder;

public class PhpEncoderTest extends AEncoderTest {
    @Override
    void check(byte[] original, String encode) {

    }

    @Override
    Encoder createInstance() {
        return new PhpEncoder();
    }
}