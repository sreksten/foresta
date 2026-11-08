package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Che cosa mostra un fumetto, senza immagini: un rettangolo bianco smussato con il testo su più righe e una punta che indica chi parla. Dice com'è
 * fatta la nuvola (dimensioni, righe e loro posti) e dov'è il triangolo della punta ({@link #punta}). Cresce verso l'alto da un punto e svanisce dopo
 * qualche secondo: il movimento e la dissolvenza sono di {@link ModelloDelloSprite}.
 */
public final class DescrizioneFumetto {

	public static final float DURATA_IN_SECONDI = 3.2f;
	public static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	/**
	 * Quanto è smussato il bordo della nuvola, e quanto è larga la base della punta
	 */
	public static final int SMUSSAMENTO_BORDO = 8;

	/**
	 * Una riga del testo: che cosa dice e dove sta nella nuvola (angolo in alto a sinistra)
	 */
	public static final class Riga {
		private final String testo;
		private final int x;
		private final int y;

		private Riga(String testo, int x, int y) {
			this.testo = testo;
			this.x = x;
			this.y = y;
		}

		public String getTesto() {
			return testo;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	private final String testo;
	private final DoomdarkFont font;
	private final List<Riga> righe;
	private final int larghezza;
	private final int altezza;

	/**
	 * @param larghezzaMassima la larghezza massima della nuvola: il testo va a capo entro quella meno i bordi (la nuvola può risultare più stretta)
	 */
	public DescrizioneFumetto(String testo, int larghezzaMassima, DoomdarkFont font) {
		this(testo, larghezzaMassima, font, 0, 0);
	}

	/**
	 * Con una riga di intestazione in cima (per esempio un'icona e un titolo, che disegna chi usa la nuvola) larga e alta quanto detto: il testo scende
	 * della sua altezza e la nuvola è larga almeno quanto lei. Le righe del testo vanno a capo come sempre.
	 *
	 * @param larghezzaIntestazione 0 se non c'è
	 * @param altezzaIntestazione   0 se non c'è
	 */
	public DescrizioneFumetto(String testo, int larghezzaMassima, DoomdarkFont font, int larghezzaIntestazione, int altezzaIntestazione) {
		this.testo = testo;
		this.font = font;
		List<String> parti = FontTool.split(font, testo, larghezzaMassima - 2 * SMUSSAMENTO_BORDO);
		// Ogni riga ha già in cima e in fondo le righe in più per accenti e cediglie: fra due righe basta la più alta delle due fasce, e se un
		// accento tocca una cediglia pazienza
		int righeInFondo = font.getHeight() - font.getAltezzaDelCorpo() - font.getRigheInCima();
		int passo = font.getAltezzaDelCorpo() + Math.max(font.getRigheInCima(), righeInFondo);
		int piuLarga = 0;
		List<Riga> composte = new ArrayList<>();
		int y = SMUSSAMENTO_BORDO + altezzaIntestazione;
		for (String parte : parti) {
			piuLarga = Math.max(piuLarga, Math.max(1, FontTool.getWidth(font, parte)));
			composte.add(new Riga(parte, SMUSSAMENTO_BORDO, y));
			y += passo;
		}
		this.larghezza = Math.max(piuLarga, larghezzaIntestazione) + 2 * SMUSSAMENTO_BORDO;
		// L'ultima riga è intera, le altre avanzano di un passo
		this.altezza = 2 * SMUSSAMENTO_BORDO + altezzaIntestazione + (parti.size() - 1) * passo + font.getHeight();
		this.righe = Collections.unmodifiableList(composte);
	}

	public String getTesto() {
		return testo;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	public List<Riga> getRighe() {
		return righe;
	}

	/**
	 * La larghezza vera della nuvola: quella della sua riga più larga più i bordi
	 */
	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il triangolo della punta, dal bordo della nuvola verso il punto indicato: {x1, y1, x2, y2, xPunta, yPunta}, o null se il punto cade dentro la nuvola
	 * (nessuna punta). (x, y) è l'angolo IN BASSO a sinistra della nuvola, come in CoordinateFumetto.
	 */
	public static int[] punta(int x, int y, int larghezza, int altezza, int puntaVersoX, int puntaVersoY) {
		final int limiteSinistro = x;
		final int limiteDestro = x + larghezza;
		final int limiteSuperiore = y - altezza;
		final int limiteInferiore = y;

		final boolean puntaVersoSinistra = puntaVersoX < limiteSinistro;
		final boolean puntaVersoDestra = puntaVersoX > limiteDestro;
		final boolean puntaSopra = puntaVersoY < limiteSuperiore;
		final boolean puntaSotto = puntaVersoY > limiteInferiore;

		int x1;
		int y1;
		int x2;
		int y2;
		if (puntaVersoSinistra && puntaSotto) {
			// Il fumetto appare in alto a destra rispetto al punto interessato: la punta parte dallo smusso dell'angolo in basso a sinistra
			x1 = limiteSinistro;
			y1 = limiteInferiore - SMUSSAMENTO_BORDO;
			x2 = limiteSinistro + SMUSSAMENTO_BORDO;
			y2 = limiteInferiore;
		} else if (puntaVersoDestra && puntaSotto) {
			// In alto a sinistra rispetto al punto: dallo smusso dell'angolo in basso a destra
			x1 = limiteDestro - SMUSSAMENTO_BORDO;
			y1 = limiteInferiore;
			x2 = limiteDestro;
			y2 = limiteInferiore - SMUSSAMENTO_BORDO;
		} else if (puntaVersoSinistra && puntaSopra) {
			// In basso a destra rispetto al punto: dallo smusso dell'angolo in alto a sinistra
			x1 = limiteSinistro;
			y1 = limiteSuperiore + SMUSSAMENTO_BORDO;
			x2 = limiteSinistro + SMUSSAMENTO_BORDO;
			y2 = limiteSuperiore;
		} else if (puntaVersoDestra && puntaSopra) {
			// In basso a sinistra rispetto al punto: dallo smusso dell'angolo in alto a destra
			x1 = limiteDestro - SMUSSAMENTO_BORDO;
			y1 = limiteSuperiore;
			x2 = limiteDestro;
			y2 = limiteSuperiore + SMUSSAMENTO_BORDO;
		} else if (puntaSopra) {
			// Il punto è sopra il fumetto, allineato orizzontalmente: la punta parte dal centro del lato superiore
			int centroX = limiteSinistro + larghezza / 2;
			x1 = centroX - SMUSSAMENTO_BORDO;
			y1 = limiteSuperiore;
			x2 = centroX + SMUSSAMENTO_BORDO;
			y2 = limiteSuperiore;
		} else if (puntaSotto) {
			// Il punto è sotto il fumetto, allineato orizzontalmente: dal centro del lato inferiore
			int centroX = limiteSinistro + larghezza / 2;
			x1 = centroX - SMUSSAMENTO_BORDO;
			y1 = limiteInferiore;
			x2 = centroX + SMUSSAMENTO_BORDO;
			y2 = limiteInferiore;
		} else if (puntaVersoSinistra) {
			// Il punto è a sinistra del fumetto, allineato verticalmente: dal centro del lato sinistro
			int centroY = limiteSuperiore + altezza / 2;
			x1 = limiteSinistro;
			y1 = centroY - SMUSSAMENTO_BORDO;
			x2 = limiteSinistro;
			y2 = centroY + SMUSSAMENTO_BORDO;
		} else if (puntaVersoDestra) {
			// Il punto è a destra del fumetto, allineato verticalmente: dal centro del lato destro
			int centroY = limiteSuperiore + altezza / 2;
			x1 = limiteDestro;
			y1 = centroY - SMUSSAMENTO_BORDO;
			x2 = limiteDestro;
			y2 = centroY + SMUSSAMENTO_BORDO;
		} else {
			// Il punto è coperto dal fumetto: nessuna punta
			return null;
		}
		return new int[] {x1, y1, x2, y2, puntaVersoX, puntaVersoY};
	}
}
