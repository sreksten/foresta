package com.threeamigos.foresta.ui.testo;

/**
 * Un font Doomdark che prende i glifi dall'immagine (vedi {@link ImmagineDelFont}) invece che dalle tabelle di bit dei
 * font {@link DoomdarkFontSmall} e {@link DoomdarkFontMedium}. La larghezza di ogni glifo è quella misurata sui pixel
 * all'apertura dell'immagine. Le immagini hanno righe in più in cima (per gli accenti) e in fondo (per cediglie e
 * simili) oltre a quelle del font vero: una riga di testo è alta quanto l'immagine intera (8 + 3 + 2 nel piccolo, 16 + 5
 * + 3 nel medio), così gli accenti e le cediglie si vedono e le righe di testo sono già separate da queste righe in
 * più (l'interlinea fra righe può essere di un pixel al massimo). Le righe del corpo del font sono
 * {@link #getAltezzaDelCorpo()}, più in basso di {@link #getRigheInCima()}.
 * <p>
 * Chi ha in mano uno dei due font di sempre ottiene questo con {@link #di(DoomdarkFont)}. Non ha i dati dei glifi in
 * bit: {@link #getGlyphData} e {@link #getDataWidthInBytes} non ci sono, e lo dicono.
 */
public final class ImageBasedDoomdarkFont implements DoomdarkFont {

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

	private static final String RISORSA_PICCOLO = "/com/threeamigos/foresta/img/font/doomdark_small.gif";
	private static final String RISORSA_MEDIO = "/com/threeamigos/foresta/img/font/doomdark_medium.gif";

	// Aperti la prima volta che servono: chi non scrive testo non legge le immagini
	private static final class Piccolo {
		static final ImageBasedDoomdarkFont ISTANZA = new ImageBasedDoomdarkFont(RISORSA_PICCOLO, DoomdarkFontSmall.getInstance(), EXTRA_IN_CIMA_PICCOLO, EXTRA_IN_FONDO_PICCOLO);
	}

	private static final class Medio {
		static final ImageBasedDoomdarkFont ISTANZA = new ImageBasedDoomdarkFont(RISORSA_MEDIO, DoomdarkFontMedium.getInstance(), EXTRA_IN_CIMA_MEDIO, EXTRA_IN_FONDO_MEDIO);
	}

	private final ImmagineDelFont immagine;
	// Le righe del corpo del font, e quelle in più sopra e sotto
	private final int altezzaDelCorpo;
	private final int righeInCima;
	private final int altezza;
	private final int spaziatura;
	private final int margine;

	private ImageBasedDoomdarkFont(String risorsa, DoomdarkFont corrispondente, int righeInCima, int righeInFondo) {
		this.altezzaDelCorpo = corrispondente.getHeight();
		this.righeInCima = righeInCima;
		this.altezza = righeInCima + altezzaDelCorpo + righeInFondo;
		this.spaziatura = corrispondente.getSpacing();
		this.margine = corrispondente.getPadding();
		// Lo spazio non ha pixel accesi: la sua larghezza è quella che ha il font di sempre
		this.immagine = ImmagineDelFont.carica(risorsa, corrispondente.getGlyphWidth(' '));
		if (immagine.getAltezza() != altezza) {
			throw new IllegalStateException("L'immagine " + risorsa + " è alta " + immagine.getAltezza()
					+ " righe, ne servono " + altezza + " (" + righeInCima + " in cima, " + altezzaDelCorpo + " del font, "
					+ righeInFondo + " in fondo)");
		}
	}

	public static ImageBasedDoomdarkFont piccolo() {
		return Piccolo.ISTANZA;
	}

	public static ImageBasedDoomdarkFont medio() {
		return Medio.ISTANZA;
	}

	/**
	 * Il font a immagine che corrisponde a uno dei due font di sempre (o lo stesso, se lo è già).
	 */
	public static ImageBasedDoomdarkFont di(DoomdarkFont font) {
		if (font instanceof ImageBasedDoomdarkFont) {
			return (ImageBasedDoomdarkFont) font;
		}
		if (font == DoomdarkFontSmall.getInstance()) {
			return piccolo();
		}
		if (font == DoomdarkFontMedium.getInstance()) {
			return medio();
		}
		throw new IllegalArgumentException("Nessun font a immagine per " + font);
	}

	/**
	 * L'altezza di una riga di testo: tutte le righe dell'immagine, quelle in più sopra e sotto comprese
	 */
	@Override
	public int getHeight() {
		return altezza;
	}

	/**
	 * Le righe del font vero, senza quelle in più per accenti e cediglie
	 */
	@Override
	public int getAltezzaDelCorpo() {
		return altezzaDelCorpo;
	}

	/**
	 * Quante righe in più stanno sopra il corpo, per gli accenti
	 */
	@Override
	public int getRigheInCima() {
		return righeInCima;
	}

	@Override
	public int getSpacing() {
		return spaziatura;
	}

	@Override
	public int getPadding() {
		return margine;
	}

	/**
	 * Non ha i glifi in bit: i livelli si prendono con {@link #getLivello}.
	 */
	@Override
	public int getDataWidthInBytes() {
		throw new UnsupportedOperationException("Il font a immagine non ha i glifi in bit");
	}

	@Override
	public byte[] getGlyphData(char c) {
		throw new UnsupportedOperationException("Il font a immagine non ha i glifi in bit");
	}

	/**
	 * La larghezza del glifo, misurata sui pixel quando si è aperta l'immagine
	 */
	@Override
	public int getGlyphWidth(char c) {
		return immagine.getLarghezzaDelGlifo(c);
	}

	@Override
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
}
