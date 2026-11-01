package com.threeamigos.foresta.ui.testo;

/**
 * Un font bitmap nello stile del gioco originale: ogni carattere è una piccola immagine di bit, di una certa altezza, con la sua larghezza. Ne esistono due misure ({@link DoomdarkFontSmall} e {@link DoomdarkFontMedium}).
 */
public interface DoomdarkFont {

	/**
	 * L'altezza di una riga di testo scritto con questo font, comprese le righe in più per accenti e cediglie se ci sono
	 */
	int getHeight();

	/**
	 * Quante righe stanno sopra il corpo del font, per gli accenti: chi posiziona un testo in un punto fisso (un titolo
	 * su una cornice) le toglie dalla quota, perché il corpo del testo stia dov'era. I font di bit non ne hanno.
	 */
	default int getRigheInCima() {
		return 0;
	}

	/**
	 * Le righe del corpo del font, senza quelle in più per accenti e cediglie
	 */
	default int getAltezzaDelCorpo() {
		return getHeight();
	}

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
