package com.threeamigos.foresta.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.ui.DoomdarkFont.UnsupportedCharacterException;

public class FontTool {
	
	private FontTool() {
	}

	public static final int getWidth(DoomdarkFont font, String s) {
		int width = 0;
		final int charSpacing = font.getSpacing();
		for (int i = 0; i < s.length(); i++) {
			try {
				width += font.getGlyphWidth(s.charAt(i)) + charSpacing;
			} catch (UnsupportedCharacterException e) {
				Logger.log(e);
				continue;
			}
		}
		return width;
	}

	// Spezza la frase in più stringhe per falr rientrare in una larghezza massima
	public static final List<String> split(DoomdarkFont font, String s, int width) {
		List<String> list = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(s, " ");
		int phraseWidth = 0;
		int wordWidth;
		StringBuilder phrase = new StringBuilder();
		String word;
		final int charSpacing = font.getSpacing();
		final int widthOfSpaceCharacter = font.getGlyphWidth(' ');
		while (st.hasMoreTokens()) {
			word = st.nextToken();
			wordWidth = getWidth(font, word);
			if (phraseWidth > 0 && (phraseWidth + 2 * charSpacing + widthOfSpaceCharacter + wordWidth >= width)) {
				list.add(phrase.toString());
				phrase = new StringBuilder();
				phraseWidth = 0;
			}
			if (phrase.length() > 0) {
				phrase.append(" ");
				phraseWidth += widthOfSpaceCharacter;
			}
			phrase.append(word);
			phraseWidth += wordWidth + charSpacing;
		}
		list.add(phrase.toString());
		return list;
	}

}
