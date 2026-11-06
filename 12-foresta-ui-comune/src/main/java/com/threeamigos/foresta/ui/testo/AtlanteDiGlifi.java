package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.LetturaPng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Quel che hanno in comune le immagini dei font: i glifi di tutti i caratteri di {@link CaratteriDelFont} in tre righe,
 * ognuno in un riquadro, tutti della stessa misura. La prima riga e la prima colonna dell'immagine sono rosse, e ogni
 * riquadro ha a destra e in basso la sua linea rossa: l'immagine non si cerca la griglia, la calcola. Dall'altezza tolta
 * la linea in alto e divisa per le righe vengono la misura di una riga di riquadri e quella dei riquadri; allo stesso
 * modo dalla larghezza e dalle colonne.
 * <p>
 * Un riquadro con un pixel rosso (255, 0, 0) è un glifo che manca (un segnaposto da ridisegnare): il carattere non è
 * nella mappa, e chi lo chiede ottiene l'eccezione di sempre ({@link DoomdarkFont.UnsupportedCharacterException}). Come
 * si legge un riquadro, cioè che cosa sono i suoi pixel, lo dice chi lo estende (vedi {@link ImmagineDelFont} e
 * {@link ImmagineDelFontAColori}).
 *
 * @param <G> il tipo dei glifi
 */
public abstract class AtlanteDiGlifi<G extends GlifoDiAtlante> {

	/**
	 * Il rosso dei segnaposto e delle linee della griglia
	 */
	protected static final int ROSSO = 0xFF0000;

	/**
	 * Legge un riquadro dell'immagine: restituisce il glifo, o null se manca (ha un pixel rosso, oppure non ha pixel
	 * accesi e non è lo spazio).
	 */
	protected interface LettoreDiRiquadro<G> {
		G leggi(ImmagineDiPixel immagine, char carattere, int x0, int y0, int larghezza, int altezza);
	}

	/**
	 * Quel che si è letto da un'immagine
	 */
	protected static final class Letto<G> {
		private final int altezza;
		private final Map<Character, G> glifi;
		private final List<Character> mancanti;

		private Letto(int altezza, Map<Character, G> glifi, List<Character> mancanti) {
			this.altezza = altezza;
			this.glifi = glifi;
			this.mancanti = mancanti;
		}
	}

	private final int altezza;
	private final Map<Character, G> glifi;
	private final List<Character> mancanti;

	protected AtlanteDiGlifi(Letto<G> letto) {
		this.altezza = letto.altezza;
		this.glifi = Collections.unmodifiableMap(letto.glifi);
		this.mancanti = Collections.unmodifiableList(letto.mancanti);
	}

	/**
	 * Apre una risorsa, per esempio {@code /com/threeamigos/foresta/img/font/doomdark_small.png}
	 */
	protected static ImmagineDiPixel leggiRisorsa(String risorsa) {
		return LetturaPng.leggiRisorsa(risorsa);
	}

	/**
	 * Calcola la griglia dell'immagine e legge tutti i riquadri
	 */
	protected static <G> Letto<G> leggiLaGriglia(ImmagineDiPixel immagine, LettoreDiRiquadro<G> lettore) {
		int righe = CaratteriDelFont.RIGHE.length;
		int colonne = CaratteriDelFont.colonne();
		if ((immagine.getAltezza() - 1) % righe != 0 || (immagine.getLarghezza() - 1) % colonne != 0) {
			throw new IllegalStateException("L'immagine del font, " + immagine.getLarghezza() + "x" + immagine.getAltezza()
					+ ", non è una griglia di " + colonne + " colonne e " + righe + " righe con una linea sola attorno a ogni riquadro");
		}
		int passoX = (immagine.getLarghezza() - 1) / colonne;
		int passoY = (immagine.getAltezza() - 1) / righe;
		// L'ultima colonna e l'ultima riga di ogni riquadro sono la linea rossa di separazione
		int larghezzaRiquadro = passoX - 1;
		int altezzaRiquadro = passoY - 1;

		Map<Character, G> glifi = new LinkedHashMap<>();
		List<Character> mancanti = new ArrayList<>();
		for (int r = 0; r < righe; r++) {
			String riga = CaratteriDelFont.RIGHE[r];
			for (int c = 0; c < riga.length(); c++) {
				char carattere = riga.charAt(c);
				G glifo = lettore.leggi(immagine, carattere, 1 + c * passoX, 1 + r * passoY, larghezzaRiquadro, altezzaRiquadro);
				if (glifo == null) {
					mancanti.add(carattere);
				} else {
					glifi.put(carattere, glifo);
				}
			}
		}
		return new Letto<>(altezzaRiquadro, glifi, mancanti);
	}

	/**
	 * L'altezza di tutti i glifi, cioè quella di un riquadro
	 */
	public int getAltezza() {
		return altezza;
	}

	/**
	 * I glifi presenti, per carattere. Quelli che mancano non ci sono.
	 */
	public Map<Character, G> getGlifi() {
		return glifi;
	}

	/**
	 * I caratteri che nell'immagine sono ancora segnaposto (o riquadri vuoti)
	 */
	public List<Character> getCaratteriMancanti() {
		return mancanti;
	}

	public boolean supportaIlCarattere(char c) {
		return glifi.containsKey(c);
	}

	/**
	 * @throws DoomdarkFont.UnsupportedCharacterException se il carattere non c'è, come per i font di oggi
	 */
	public G getGlifo(char c) {
		G glifo = glifi.get(c);
		if (glifo == null) {
			throw new DoomdarkFont.UnsupportedCharacterException(c);
		}
		return glifo;
	}

	public int getLarghezzaDelGlifo(char c) {
		return getGlifo(c).getLarghezza();
	}
}
