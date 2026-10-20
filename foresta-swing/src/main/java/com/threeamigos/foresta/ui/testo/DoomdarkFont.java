package com.threeamigos.foresta.ui.testo;

/**
 * Un font bitmap nello stile del gioco originale: ogni carattere è una piccola immagine di bit, di una certa altezza, con la sua larghezza. Ne esistono due misure ({@link DoomdarkFontSmall} e {@link DoomdarkFontMedium}).
 */
public interface DoomdarkFont {

	int getHeight();

	int getSpacing();

	int getPadding();
	
	int getDataWidthInBytes();
	
	int getGlyphWidth(char c);

	byte[] getGlyphData(char c);

	/**
	 * Se il font ha il carattere: chi lo scrive o lo misura salta gli altri (vedi {@link UnsupportedCharacterException}).
	 */
	default boolean supportaIlCarattere(char c) {
		try {
			getGlyphWidth(c);
			return true;
		} catch (UnsupportedCharacterException e) {
			return false;
		}
	}

	/**
	 * Lanciata quando si chiede un carattere che il font non ha (per esempio una minuscola accentata).
	 */
	class UnsupportedCharacterException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public UnsupportedCharacterException(char c) {
			super("Carattere non supportato: " + c + " [" + (int)c + "]");
		}

	}
	
}
