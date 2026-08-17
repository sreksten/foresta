package com.threeamigos.foresta.motore;

/**
 *
 * @author Stefano Reksten
 */
abstract class TestGrammatiche {

    private static final int MAX_LINE_LENGTH = 120;

    protected static void printProductions(GrammarBean gBean) {
        for (int i = 0; i < 5; i++) {
            for (String s : gBean.produce()) {
                for (String line : s.replace("\\n", "\n").split("\n", -1)) {
                    printWrapped(line);
                }
            }
            System.out.println("-----");
        }
    }

    protected static void printWrapped(String line) {
        if (line.isEmpty()) {
            System.out.println();
            return;
        }
        StringBuilder currentLine = new StringBuilder();
        for (String word : line.split(" ")) {
            if (currentLine.length() > 0 && currentLine.length() + 1 + word.length() > MAX_LINE_LENGTH) {
                System.out.println(currentLine);
                currentLine.setLength(0);
            }
            if (currentLine.length() > 0) {
                currentLine.append(' ');
            }
            currentLine.append(word);
        }
        System.out.println(currentLine);
    }
}
