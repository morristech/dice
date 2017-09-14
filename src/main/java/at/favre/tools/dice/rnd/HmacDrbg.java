package at.favre.tools.dice.rnd;

import at.favre.tools.dice.util.ByteUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Deterministic Random Bit Generator based on HMAC-SHA256.
 * <p>
 * Also known as: HMAC_DRBG.
 * See http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf for thorough specification.
 * <p>
 * Reseeding is supported.
 * See http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf Section 8.6.8.
 */
public final class HmacDrbg implements DeterministicRandomBitGenerator {
    // "V" from the the spec.
    private byte[] value;
    // An instance of HMAC-SHA512 configured with "Key" from the spec.
    private Mac hmac;
    // The total number of bytes that have been generated from this DRBG so far.
    private int bytesGenerated;

    // Assume maximum security strength for HMAC-512, which is 512.
    // See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf D.2 #1.
    private static final int SECURITY_STRENGTH_BIT = 512;

    /**
     * Used HMAC mode
     */
    private static final String HMAC_MODE = "HmacSHA512";

    /**
     * Personalization strings should not exceed this many bytes in length.
     * <p>
     * See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf D.2 #7.
     */
    private static final int MAX_PERSONALIZATION_STRING_LENGTH_BYTES = SECURITY_STRENGTH_BIT / 2 / 8;

    /**
     * The constructor's entropyInput should contain this many high quality random bytes.
     * HMAC_DRBG requires entropy input to be security_strength bits long.
     * See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf
     */
    private static final int ENTROPY_INPUT_SIZE_BYTES = SECURITY_STRENGTH_BIT / 8;

    /**
     * HMAC_DRBG requires nonce to be at least 1/2 security_strength bits long.
     * See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf
     */
    private static final int NOUNCE_INPUT_SIZE_BYTES = (SECURITY_STRENGTH_BIT / 8) / 2;

    /**
     * The maximum number of bytes that can be generated from this DRBG with a single seed.
     * <p>
     * This is conservative relative to the suggestions in
     * http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf section D.2.
     * Reseeding is necessary when this threshold is reached.
     */
    private static final int MAX_BYTES_PER_SEED = 4096;

    // See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf D.2 #2.
    private static final int DIGEST_NUM_BYTES = SECURITY_STRENGTH_BIT / 8;
    // floor(7500/8); see: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf D.2 #5.
    private static final int MAX_BYTES_PER_REQUEST = 937;
    private static final byte[] BYTE_ARRAY_0 = {0};
    private static final byte[] BYTE_ARRAY_1 = {1};

    private final ExpandableEntropySource entropySource;
    private final ExpandableEntropySource nounceSource;
    private final ExpandableEntropySource personalizationSource;

    public HmacDrbg(ExpandableEntropySource entropyInput, ExpandableEntropySource nounceSource, ExpandableEntropySource personalizationSource) {
        this.entropySource = entropyInput;
        this.nounceSource = nounceSource;
        this.personalizationSource = personalizationSource;

        // HMAC_DRBG Instantiate Process
        // See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf 10.1.1.2

        // 2. Key = 0x00 00...00
        setKey(new byte[SECURITY_STRENGTH_BIT / 8]);
        // 3. V = 0x01 01...01
        value = new byte[DIGEST_NUM_BYTES];
        Arrays.fill(value, (byte) 0x01);

        // 4. (Key, V) = HMAC_DRBG_Update(seed_material, Key, V)
        hmacDrbgUpdate(generateSeedMaterial());
        bytesGenerated = 0;
    }

    private byte[] generateSeedMaterial() {
        // 1. seed_material = entropy_input + nonce + personalization_string
        // Note: We are using the 8.6.7 interpretation, where the entropy_input and
        // nonce are acquired at the same time from the same source.
        return ByteUtils.concatAll(
                entropySource.generateEntropy(ENTROPY_INPUT_SIZE_BYTES),
                nounceSource.generateEntropy(NOUNCE_INPUT_SIZE_BYTES),
                personalizationSource.generateEntropy(MAX_PERSONALIZATION_STRING_LENGTH_BYTES));
    }

    /**
     * Returns an 0-length byte array if b is null, otherwise returns b.
     */
    private static byte[] emptyIfNull(byte[] b) {
        return b == null ? new byte[0] : b;
    }

    /**
     * Set's the "Key" state from the spec.
     */
    private void setKey(byte[] key) {
        hmac = makeHMACHasher(key);
    }


    private static Mac makeHMACHasher(byte[] key) {
        try {
            Mac hmacHasher = Mac.getInstance(HMAC_MODE);
            hmacHasher.init(new SecretKeySpec(key, HMAC_MODE));
            return hmacHasher;
        } catch (Exception e) {
            throw new IllegalStateException("could not make hmac hasher in drbg", e);
        }
    }

    /**
     * Computes hmac("key" from the spec, x).
     */
    private byte[] hash(byte[] x) {
        hmac.update(x);
        return hmac.doFinal();
    }

    /**
     * HMAC_DRBG Update Process
     * <p>
     * See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf 10.1.2.2
     */
    private void hmacDrbgUpdate(byte[] providedData) {
        // 1. K = HMAC(K, V || 0x00 || provided_data)
        setKey(hash(ByteUtils.concatAll(value, BYTE_ARRAY_0, emptyIfNull(providedData))));
        // 2. V = HMAC(K, V);
        value = hash(value);
        // 3. If (provided_data = Null), then return K and V.
        if (providedData == null) {
            return;
        }
        // 4. K = HMAC (K, V || 0x01 || provided_data).
        setKey(hash(ByteUtils.concatAll(value, BYTE_ARRAY_1, providedData)));
        // 5. V = HMAC (K, V).
        value = hash(value);
    }

    /**
     * HMAC_DRBG Generate Process
     * <p>
     * See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf 10.1.2.5
     * <p>
     * We do not support additional_input, assuming it to be always null.
     * <p>
     * We guarantee that reseeding is never required through the use of MAX_BYTES_PER_SEED
     * rather than RESEED_INTERVAL.
     */
    private void hmacDrbgGenerate(byte[] out, int start, int count) {
        // 3. temp = Null.
        int bytesWritten = 0;
        // 4. While (len (temp) < requested_number_of_bits) do:
        while (bytesWritten < count) {
            // 4.1 V = HMAC (Key, V).
            value = hash(value);
            // 4.2 temp = temp || V.
            // 5. returned_bits = Leftmost requested_number_of_bits of temp
            int bytesToWrite = Math.min(count - bytesWritten, DIGEST_NUM_BYTES);
            System.arraycopy(value, 0, out, start + bytesWritten, bytesToWrite);
            bytesWritten += bytesToWrite;
        }

        // 6. (Key, V) = HMAC_DRBG_Update (additional_input, Key, V).
        hmacDrbgUpdate(null);
    }

    /**
     * Generates entropy byte-string suitable for use as the constructor's entropyInput.
     * <p>
     * Uses SecureRandom to generate entropy.
     */
    public static byte[] generateEntropyInput() {
        byte result[] = new byte[ENTROPY_INPUT_SIZE_BYTES];
        new SecureRandom().nextBytes(result);
        return result;
    }

    /**
     * Returns the next length pseudo-random bytes.
     */
    @Override
    public byte[] nextBytes(int lengthBytes) {
        byte result[] = new byte[lengthBytes];
        nextBytes(result);
        return result;
    }

    /**
     * Fills the output vector with pseudo-random bytes.
     */
    @Override
    public void nextBytes(byte[] out) {
        nextBytes(out, 0, out.length);
    }

    /**
     * Fills out[start] through out[start+count-1] (inclusive) with pseudo-random bytes.
     */
    public void nextBytes(byte[] out, int start, int count) {
        if (count == 0) {
            return;
        }

        if (bytesGenerated + count > MAX_BYTES_PER_SEED) {
            hmacDrbgUpdate(generateSeedMaterial());
            bytesGenerated = 0;
        }

        try {
            int bytesWritten = 0;
            while (bytesWritten < count) {
                int bytesToWrite = Math.min(count - bytesWritten, MAX_BYTES_PER_REQUEST);
                hmacDrbgGenerate(out, start + bytesWritten, bytesToWrite);
                bytesWritten += bytesToWrite;
            }
        } finally {
            bytesGenerated += count;
        }
    }
}