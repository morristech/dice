package at.favre.tools.dice.ui;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;

public class ColumnRenderer {
    private static final int MAX_WIDTH = 80;
    private static final int LINE_BREAK_EVERY_LINES = 24;
    private static final char SEPARATOR = ' ';

    private final int targetWidth;

    public ColumnRenderer() {
        this(MAX_WIDTH);
    }

    public ColumnRenderer(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public void renderAutoColumn(int targetCount, List<String> outputList, PrintStream outStream) {
        final int columns = getColumnCount(getMaxLength(outputList));

        final int fill = columns - (targetCount % columns);
        render(outputList.subList(0, Math.min(outputList.size(), targetCount + fill)), outStream);
    }

    public void renderSingleColumn(List<String> outputList, PrintStream outStream) {
        outputList.forEach(s -> outStream.print(s + "\n"));
    }

    public void render(List<String> outputList, PrintStream outStream) {
        if (!outputList.isEmpty()) {
            final int maxLength = getMaxLength(outputList);
            final int columns = getColumnCount(maxLength);
            int columnCounter = columns;
            int lineCount = 0;

            for (int i = 0; i < outputList.size(); i++) {
                String randomString = outputList.get(i);
                if (columns == 1) {
                    outStream.print(randomString);
                } else {
                    outStream.print(String.format("%-" + maxLength + "s", randomString));
                }

                columnCounter--;

                if (columnCounter == 0 && i + 1 != outputList.size()) {
                    columnCounter = columns;
                    outStream.print("\n");
                    lineCount++;

                    if (lineCount % LINE_BREAK_EVERY_LINES == 0) {
                        outStream.print("\n");
                    }

                } else {
                    outStream.print(SEPARATOR);
                }
            }
        }

    }

    private int getMaxLength(List<String> outputList) {
        return outputList.stream().max(Comparator.comparingInt(String::length)).get().length();
    }

    private int getColumnCount(int maxLength) {
        int columns = Math.max(1, targetWidth / maxLength);

        while (maxLength * columns + columns > targetWidth) {
            columns--;
        }
        return Math.max(1, columns);
    }
}
