package com.threeamigos.foresta.ui.testo;

/**
 * Un font Doomdark che prende i glifi dall'immagine (vedi {@link ImmagineDelFont}). La larghezza di ogni glifo è quella misurata sui pixel
 * all'apertura dell'immagine. Le immagini hanno righe in più in cima (per gli accenti) e in fondo (per cediglie e
 * simili) oltre a quelle del font vero: una riga di testo è alta quanto l'immagine intera (8 + 3 + 2 nel piccolo, 16 + 5
 * + 3 nel medio), così gli accenti e le cediglie si vedono e le righe di testo sono già separate da queste righe in
 * più (l'interlinea fra righe può essere di un pixel al massimo). Le righe del corpo del font sono
 * {@link #getAltezzaDelCorpo()}, più in basso di {@link #getRigheInCima()}.
 * <p>
 * I font sono due, {@link #piccolo()} e {@link #medio()}.
 */
public final class DoomdarkFont {

	/**
	 * Le righe in più in cima a ogni riquadro delle immagini, per gli accenti, e in fondo, per cediglie, virgole sotto e
	 * ogonek: nel font piccolo...
	 */
	public static final int EXTRA_IN_CIMA_PICCOLO = 3;
	public static final int EXTRA_IN_FONDO_PICCOLO = 2;
	/**
	 * ... e nel medio
	 */
	public static final int EXTRA_IN_CIMA_MEDIO = 5;
	public static final int EXTRA_IN_FONDO_MEDIO = 3;

	/**
	 * Corpo, spaziatura fra i glifi, margine e larghezza dello spazio (che non ha pixel accesi) dei due font
	 */
	private static final int CORPO_PICCOLO = 8;
	private static final int CORPO_MEDIO = 16;
	private static final int SPAZIO = 4;

	private static final String RISORSA_PICCOLO = "/com/threeamigos/foresta/img/font/doomdark_small.png";
	private static final String RISORSA_MEDIO = "/com/threeamigos/foresta/img/font/doomdark_medium.png";

	// Aperti la prima volta che servono: chi non scrive testo non legge le immagini
	private static final class Piccolo {
		static final DoomdarkFont ISTANZA = new DoomdarkFont(RISORSA_PICCOLO, CORPO_PICCOLO, EXTRA_IN_CIMA_PICCOLO, EXTRA_IN_FONDO_PICCOLO);
	}

	private static final class Medio {
		static final DoomdarkFont ISTANZA = new DoomdarkFont(RISORSA_MEDIO, CORPO_MEDIO, EXTRA_IN_CIMA_MEDIO, EXTRA_IN_FONDO_MEDIO);
	}

	private final ImmagineDelFont immagine;
	// Le righe del corpo del font, e quelle in più sopra e sotto
	private final int altezzaDelCorpo;
	private final int righeInCima;
	private final int altezza;
	private final int spaziatura = 1;
	private final int margine = 1;

	private DoomdarkFont(String risorsa, int altezzaDelCorpo, int righeInCima, int righeInFondo) {
		this.altezzaDelCorpo = altezzaDelCorpo;
		this.righeInCima = righeInCima;
		this.altezza = righeInCima + altezzaDelCorpo + righeInFondo;
		// Lo spazio non ha pixel accesi: la sua larghezza è fissa
		this.immagine = ImmagineDelFont.carica(risorsa, SPAZIO);
		if (immagine.getAltezza() != altezza) {
			throw new IllegalStateException("L'immagine " + risorsa + " è alta " + immagine.getAltezza()
					+ " righe, ne servono " + altezza + " (" + righeInCima + " in cima, " + altezzaDelCorpo + " del font, "
					+ righeInFondo + " in fondo)");
		}
	}

	public static DoomdarkFont piccolo() {
		return Piccolo.ISTANZA;
	}

	public static DoomdarkFont medio() {
		return Medio.ISTANZA;
	}

	/**
	 * L'altezza di una riga di testo: tutte le righe dell'immagine, quelle in più sopra e sotto comprese
	 */
	public int getHeight() {
		return altezza;
	}

	/**
	 * Le righe del font vero, senza quelle in più per accenti e cediglie
	 */
	public int getAltezzaDelCorpo() {
		return altezzaDelCorpo;
	}

	/**
	 * Quante righe in più stanno sopra il corpo, per gli accenti
	 */
	public int getRigheInCima() {
		return righeInCima;
	}

	public int getSpacing() {
		return spaziatura;
	}

	public int getPadding() {
		return margine;
	}

	/**
	 * La larghezza del glifo, misurata sui pixel quando si è aperta l'immagine
	 */
	public int getGlyphWidth(char c) {
		return immagine.getLarghezzaDelGlifo(c);
	}

	public boolean supportaIlCarattere(char c) {
		return immagine.supportaIlCarattere(c);
	}

	/**
	 * Il livello del pixel di un glifo, da 0 (trasparente) a 255 (il colore pieno del testo), in mezzo il colore sfuma
	 * sullo sfondo; la riga 0 è la prima dell'immagine, cioè la più alta delle righe in più per gli accenti.
	 */
	public int getLivello(char c, int x, int riga) {
		return immagine.getGlifo(c).getLivello(x, riga);
	}

	/**
	 * Lanciata quando si chiede un carattere che il font non ha.
	 */
	public static class UnsupportedCharacterException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public UnsupportedCharacterException(char c) {
			super("Carattere non supportato: " + c + " [" + (int) c + "]");
		}

	}
}
