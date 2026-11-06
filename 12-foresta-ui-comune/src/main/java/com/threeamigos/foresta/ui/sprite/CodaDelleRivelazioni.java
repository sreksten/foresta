package com.threeamigos.foresta.ui.sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Gli artefatti trovati nei cofani e nei templi, mostrati uno alla volta: una rivelazione alla volta, le altre aspettano in coda; con altre in coda la
 * sosta di quella in corso si accorcia ({@link InCoda#accelera()}). Chi disegna prende la rivelazione attiva, la disegna e dice quando ha finito; un effetto a
 * tutto schermo in corso (un annuncio globale) la fa aspettare. Non sa niente del toolkit: lo stesso per Swing e libGDX.
 *
 * @param <T> la rivelazione, nel suo toolkit
 */
public final class CodaDelleRivelazioni<T extends CodaDelleRivelazioni.InCoda> {

	/**
	 * Quel che la coda chiede a una rivelazione
	 */
	public interface InCoda {
		boolean isAttivo();

		/** Con altre in coda la sosta si accorcia */
		void accelera();
	}

	private final List<T> inAttesa = new ArrayList<>();
	private T attiva;

	public void aggiungi(T rivelazione) {
		inAttesa.add(rivelazione);
	}

	/**
	 * La rivelazione da disegnare adesso, o null: se non ce n'è una in corso prende la prima in coda (a meno che un altro effetto a tutto schermo sia in corso),
	 * accorcia la sosta se ne restano altre, e toglie quella finita.
	 *
	 * @param altroEffettoInCorso se un effetto a tutto schermo occupa lo schermo: la rivelazione aspetta
	 */
	public T attiva(boolean altroEffettoInCorso) {
		if (attiva == null && !inAttesa.isEmpty() && !altroEffettoInCorso) {
			attiva = inAttesa.remove(0);
		}
		if (attiva == null) {
			return null;
		}
		if (!inAttesa.isEmpty()) {
			attiva.accelera();
		}
		return attiva;
	}

	/**
	 * Da chiamare dopo aver disegnato la rivelazione attiva: se ha finito, la coda passa alla prossima.
	 */
	public void dopoIlDisegno() {
		if (attiva != null && !attiva.isAttivo()) {
			attiva = null;
		}
	}

	/**
	 * Se c'è una rivelazione in corso o in attesa: la UI è occupata
	 */
	public boolean isOccupata() {
		return attiva != null || !inAttesa.isEmpty();
	}
}
