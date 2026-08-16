package com.threeamigos.foresta.motore;

public class TestFiabe {

	private static final int MAX_LINE_LENGTH = 120;

	public static void main(String[] args) throws Exception {
		GrammarBean gBean = new GrammarBean(
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/fiabe.txt"),
				TestMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		for (int i = 0; i < 5; i++) {
			for (String s : gBean.produce()) {
				for (String line : s.replace("\\n", "\n").split("\n", -1)) {
					printWrapped(line);
				}
			}
			System.out.println("-----");
		}
	}

	private static void printWrapped(String line) {
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
