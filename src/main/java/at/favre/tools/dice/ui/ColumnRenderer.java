package at.favre.tools.dice.ui;

import at.favre.tools.dice.encode.Encoder;
import at.favre.tools.dice.encode.EncoderFormat;
import at.favre.tools.dice.rnd.DeterministicRandomBitGenerator;
import at.favre.tools.dice.util.ByteUtils;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

public final class ColumnRenderer {
    private static final int LINE_BREAK_EVERY_LINES = 24;
    private static final int RND_PER_REQUEST = 100;

    private final EncoderFormat encoderFormat;
    private final RandomGenerator randomGenerator;

    public ColumnRenderer(EncoderFormat encoderFormat, RandomGenerator randomGenerator) {
        this.encoderFormat = encoderFormat;
        this.randomGenerator = randomGenerator;
    }

    /**
     * Will take a list and a target count and tries to create even columns
     *
     * @param targetCount approximate count you want to render (may be filled by the auto algorithm)
     * @param outStream   to write the output to
     * @return the actual used count
     */
    public long renderAutoColumn(long targetCount, PrintStream outStream, boolean toFile) {
        String[] rndArray = new String[Math.min(RND_PER_REQUEST, encoderFormat.printWidth() * 2)];
        randomGenerator.request(rndArray);

        final long columns = getColumnCount(getMaxLength(rndArray));

        final long fill = columns - (targetCount % columns);
        return render(targetCount + fill, outStream, toFile);
    }

    public long renderSingleColumn(long count, PrintStream outStream) {
        int currentCount = 0;
        int countPerIteration = RND_PER_REQUEST;
        String[] rndArray = null;

        while (currentCount < count) {
            int nextLength = currentCount + countPerIteration > count ? (int) (count - currentCount) : countPerIteration;
            if (rndArray == null || nextLength != rndArray.length) {
                rndArray = new String[nextLength];
            }
            randomGenerator.request(rndArray);

            for (String rnd : rndArray) {
                outStream.print(rnd + System.lineSeparator());
            }

            currentCount += rndArray.length;
        }
        return count;
    }

    public long render(long count, PrintStream outStream, boolean toFile) {
        if (count != 0) {
            int currentCount = 0;
            int countPerIteration = RND_PER_REQUEST;

            String[] rndArray = null;
            long maxLength = -1;
            long columns = -1;
            long columnCounter = -1;
            long lineCount = 0;

            while (currentCount < count) {
                int nextLength = currentCount + countPerIteration > count ? (int) (count - currentCount) : countPerIteration;
                if (rndArray == null || nextLength != rndArray.length) {
                    rndArray = new String[nextLength];
                }
                randomGenerator.request(rndArray);

                if (maxLength <= 0) {
                    maxLength = getMaxLength(rndArray);
                    columns = getColumnCount(maxLength);
                    columnCounter = columns;
                }

                for (int i = 0; i < rndArray.length; i++) {
                    String randomString = rndArray[i];
                    try {
                        if (columns == 1) {
                            outStream.write(encoderFormat.asBytes(randomString));
                        } else {
                            outStream.write(encoderFormat.asBytes(String.format("%-" + maxLength + "s", randomString)));
                        }

                        columnCounter--;

                        if (columnCounter == 0 && i + 1 != count) {
                            columnCounter = columns;
                            outStream.write(encoderFormat.asBytes(toFile ? encoderFormat.newLineFile() : encoderFormat.newLineCmdLine()));
                            lineCount++;

                            if (lineCount % LINE_BREAK_EVERY_LINES == 0) {
                                outStream.write(encoderFormat.asBytes(toFile ? encoderFormat.paragraphFile() : encoderFormat.paragraphCmdLine()));
                            }

                        } else {
                            outStream.write(encoderFormat.asBytes(toFile ? encoderFormat.separatorFile() : encoderFormat.separatorFile()));
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("could not write random to output stream", e);
                    }
                }
                currentCount += rndArray.length;
            }
        }
        return count;
    }

    private int getMaxLength(String[] outputList) {
        return Arrays.stream(outputList).max(Comparator.comparingInt(String::length)).get().length();
    }

    private long getColumnCount(long maxLength) {
        long columns = Math.max(1, encoderFormat.printWidth() / maxLength);

        while (maxLength * columns + columns > encoderFormat.printWidth()) {
            columns--;
        }
        return Math.max(1, columns);
    }

    public static class DefaultRandomGenerator implements RandomGenerator {
        private final Encoder encoder;
        private final DeterministicRandomBitGenerator drbg;
        private final int length;
        private final boolean crc32;
        private final boolean padding;
        private final boolean urlencode;

        public DefaultRandomGenerator(Encoder encoder, DeterministicRandomBitGenerator drbg, int length) {
            this(encoder, drbg, length, false, false, false);
        }

        public DefaultRandomGenerator(Encoder encoder,
                                      DeterministicRandomBitGenerator drbg,
                                      int length, boolean crc32, boolean padding, boolean urlencode) {
            this.encoder = encoder;
            this.drbg = drbg;
            this.length = length;
            this.crc32 = crc32;
            this.padding = padding;
            this.urlencode = urlencode;
        }

        @Override
        public void request(String[] fillArray) {
            for (int i = 0; i < fillArray.length; i++) {
                byte[] rnd = drbg.nextBytes(length);

                if (crc32) {
                    rnd = ByteUtils.appendCrc32(rnd);
                }

                String randomEncodedString = padding ? encoder.encodePadded(rnd) : encoder.encode(rnd);

                if (urlencode) {
                    try {
                        randomEncodedString = new URLCodec().encode(randomEncodedString);
                    } catch (EncoderException e) {
                        throw new IllegalStateException("could not url encode", e);
                    }
                }
                fillArray[i] = randomEncodedString;
            }
        }

        @Override
        public int byteLengthPerRandom() {
            return length;
        }
    }

    public interface RandomGenerator {
        void request(String[] fillArray);

        int byteLengthPerRandom();
    }
}
