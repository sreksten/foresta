package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Area;

/**
 * Il tempo di uno sprite, senza disegno: qualcosa che si muove da un punto a un altro, cambia scala e sfuma, e quando la durata è finita non è più in vita.
 * Misura il tempo che passa (con l'orologio, {@link #aggiornaConOrologio(long)}, o a passi, {@link #avanzaDi(float)}), e da quello calcola per interpolazione
 * posizione e scala e, con la curva scelta ({@link Dissolvenza}), l'opacità. Chi disegna (Swing, libGDX) a ogni fotogramma chiama {@link #prepara()}, e se
 * dice che c'è da disegnare legge {@link #getX()}, {@link #getY()}, {@link #getScala()} e {@link #getAlpha()}; poi fa avanzare il tempo.
 * Se si indica un ritaglio, lo sprite si vede solo dentro quell'area.
 */
public final class ModelloDelloSprite {

	/**
	 * Come cresce la trasparenza di uno sprite
	 */
	public enum Dissolvenza {
		/**
		 * Intero fino alla soglia, poi sfuma con una curva iperbolica, rapida all'inizio e lenta in coda
		 */
		IPERBOLICA_CON_SOGLIA,
		/**
		 * Intero fino alla soglia, poi sfuma in linea retta fino alla fine della durata
		 */
		LINEARE_CON_SOGLIA,
		/**
		 * Sfuma da subito, con la curva 1/(1 + 10 t)
		 */
		DA_SUBITO
	}

	private final float durataInSecondi;
	private final float sogliaDellaDissolvenza;
	private final Dissolvenza dissolvenza;
	private float xIniziale;
	private float yIniziale;
	private float xFinale;
	private float yFinale;
	private final float scalaIniziale;
	private final float scalaFinale;
	private float secondiTrascorsi;
	private boolean attivo = true;
	private Area ritaglio;
	// -1 = nessuna chiamata precedente: la prima non fa avanzare il tempo, le successive misurano quello reale trascorso, senza supporre una chiamata
	// ogni tanto (il disegno può essere richiesto anche da un ridisegno estraneo all'animatore, per esempio per il mouse)
	private long ultimoAggiornamentoNanos = -1;

	/**
	 * @param durataInSecondi        quanto dura lo sprite
	 * @param sogliaDellaDissolvenza da quando sfuma (ignorata da {@link Dissolvenza#DA_SUBITO})
	 */
	public ModelloDelloSprite(float durataInSecondi, float sogliaDellaDissolvenza, Dissolvenza dissolvenza,
							  float xIniziale, float yIniziale, float xFinale, float yFinale, float scalaIniziale, float scalaFinale) {
		this.durataInSecondi = durataInSecondi;
		this.sogliaDellaDissolvenza = sogliaDellaDissolvenza;
		this.dissolvenza = dissolvenza;
		this.xIniziale = xIniziale;
		this.yIniziale = yIniziale;
		this.xFinale = xFinale;
		this.yFinale = yFinale;
		this.scalaIniziale = scalaIniziale;
		this.scalaFinale = scalaFinale;
	}

	/**
	 * Limita il disegno a quest'area, in coordinate dello schermo (null: nessun limite)
	 */
	public void setRitaglio(Area ritaglio) {
		this.ritaglio = ritaglio;
	}

	public Area getRitaglio() {
		return ritaglio;
	}

	/**
	 * Sposta lo sprite in orizzontale, fermo in x: per metterlo a posto dopo averlo creato
	 */
	public void impostaX(float x) {
		this.xIniziale = x;
		this.xFinale = x;
	}

	/**
	 * Fa ripartire da capo il tempo (e quindi la dissolvenza e il movimento), anche l'orologio: la prossima misura parte da lì
	 */
	public void resettaTempoTrascorso() {
		secondiTrascorsi = 0;
		ultimoAggiornamentoNanos = -1;
	}

	/**
	 * Se lo sprite è ancora in vita: finita la durata non lo è più (lo si vede con {@link #prepara()})
	 */
	public boolean isAttivo() {
		return attivo;
	}

	public float getDurataInSecondi() {
		return durataInSecondi;
	}

	public float getSecondiTrascorsi() {
		return secondiTrascorsi;
	}

	/**
	 * Da chiamare all'inizio di ogni fotogramma: se la durata è finita disattiva lo sprite. Dice se c'è da disegnare.
	 */
	public boolean prepara() {
		if (!attivo) {
			return false;
		}
		if (secondiTrascorsi >= durataInSecondi) {
			attivo = false;
			return false;
		}
		return true;
	}

	/**
	 * Da chiamare dopo aver disegnato: fa avanzare il tempo di quello che l'orologio dice che è passato dall'ultima volta (la prima volta non avanza)
	 *
	 * @param ora l'orologio in nanosecondi (per esempio <code>System.nanoTime()</code>)
	 */
	public void aggiornaConOrologio(long ora) {
		if (ultimoAggiornamentoNanos >= 0) {
			secondiTrascorsi += (ora - ultimoAggiornamentoNanos) / 1_000_000_000f;
		}
		ultimoAggiornamentoNanos = ora;
	}

	/**
	 * Fa avanzare il tempo di quei secondi
	 */
	public void avanzaDi(float secondi) {
		secondiTrascorsi += secondi;
	}

	private float progresso() {
		return Math.min(1f, secondiTrascorsi / durataInSecondi);
	}

	public float getX() {
		return interpola(xIniziale, xFinale, progresso());
	}

	public float getY() {
		return interpola(yIniziale, yFinale, progresso());
	}

	public float getScala() {
		return interpola(scalaIniziale, scalaFinale, progresso());
	}

	/**
	 * L'opacità, da 0 (invisibile) a 1, adesso
	 */
	public float getAlpha() {
		return alpha(dissolvenza, secondiTrascorsi, sogliaDellaDissolvenza, durataInSecondi);
	}

	/**
	 * L'opacità con quella curva dopo quel tempo dall'inizio
	 */
	public static float alpha(Dissolvenza dissolvenza, float secondiTrascorsi, float sogliaInSecondi, float durataInSecondi) {
		switch (dissolvenza) {
			case IPERBOLICA_CON_SOGLIA:
				return dissolvenzaIperbolicaConSoglia(secondiTrascorsi, sogliaInSecondi);
			case LINEARE_CON_SOGLIA:
				return dissolvenzaLineareConSoglia(secondiTrascorsi, sogliaInSecondi, durataInSecondi);
			default:
				return 1.0f / (1 + secondiTrascorsi * 10);
		}
	}

	/**
	 * Il valore che sta a quel progresso (da 0 a 1) fra l'iniziale e il finale.
	 */
	public static float interpola(float iniziale, float finale, float progresso) {
		return iniziale + (finale - iniziale) * progresso;
	}

	/**
	 * Una dissolvenza che non comincia finché non passa la soglia e poi sfuma con una curva iperbolica, rapida all'inizio e lenta in coda.
	 */
	public static float dissolvenzaIperbolicaConSoglia(float secondiTrascorsi, float sogliaInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		// Curva tarata sui tick storici da 0.1s (1/(ticks-limite)): moltiplicando per 10 i secondi oltre soglia si ottiene lo stesso andamento.
		return Math.min(1.0f, 1.0f / ((secondiTrascorsi - sogliaInSecondi) * 10));
	}

	/**
	 * Una dissolvenza che non comincia finché non passa la soglia e poi sfuma in linea retta fino alla fine della durata.
	 */
	public static float dissolvenzaLineareConSoglia(float secondiTrascorsi, float sogliaInSecondi, float durataInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		return Math.max(0.0f, 1.0f - (secondiTrascorsi - sogliaInSecondi) / (durataInSecondi - sogliaInSecondi));
	}
}
