package at.favre.tools.dice.ui;

import org.apache.tools.ant.types.Commandline;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CLIParserTest {
    @Test
    public void testSimpleDefaults() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("67"));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 67, null, false, false, false, false, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testSimpleCount() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("4 -" + CLIParser.ARG_COUNT + " 1123 " + "-" + CLIParser.ARG_ENCODING + " testTest"));
        Arg expectedArg = Arg.create(System.out, "testTest", null, 4, 1123L, false, false, false, false, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testSimpleSeed() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("128 -" + CLIParser.ARG_SEED + " ahdalsudzasldjhasdu"));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, "ahdalsudzasldjhasdu", 128, null, false, false, false, false, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testOnline() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("4 -" + CLIParser.ARG_COUNT + " 1123 " + "-" + CLIParser.ARG_ONLINE));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 4, 1123L, true, false, false, false, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testUrlEncode() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("4 -" + CLIParser.ARG_COUNT + " 1123 " + "-" + CLIParser.ARG_ONLINE));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 4, 1123L, true, false, false, false, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testPadding() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("64 " + "-" + CLIParser.ARG_PADDING));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 64, null, false, false, false, true, false, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testRobot() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("64 " + "-" + CLIParser.ARG_ROBOT));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 64, null, false, false, false, false, true, false, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testCrc32() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("12 " + "--" + CLIParser.ARG_CRC32));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 12, null, false, false, false, false, false, true, null);
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testOutFile() throws Exception {
        String fileString = "./file.txt";
        Arg parsedArg = CLIParser.parse(asArgArray("12 " + "-" + CLIParser.ARG_OUTFILE + " \"" + fileString + "\""));
        Arg expectedArg = Arg.create(System.out, Arg.DEFAULT_ENCODING, null, 12, null, false, false, false, false, false, false, new File(fileString));
        assertEquals(expectedArg, parsedArg);
    }

    @Test
    public void testHelp() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("--help"));
        assertNull(parsedArg);
    }

    @Test
    public void testVersion() throws Exception {
        Arg parsedArg = CLIParser.parse(asArgArray("--version"));
        assertNull(parsedArg);
    }

    public static String[] asArgArray(String cmd) {
        return Commandline.translateCommandline(cmd);
    }
}