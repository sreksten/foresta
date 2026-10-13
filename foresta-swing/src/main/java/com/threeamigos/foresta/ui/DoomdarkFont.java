package com.threeamigos.foresta.ui;

public interface DoomdarkFont {

	int getHeight();

	int getSpacing();

	int getPadding();
	
	int getDataWidthInBytes();
	
	int getGlyphWidth(char c);

	byte[] getGlyphData(char c);

	class UnsupportedCharacterException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public UnsupportedCharacterException(char c) {
			super("Carattere non supportato: " + c + " [" + (int)c + "]");
		}

	}
	
}
