package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * La locazione viva in cui si trova il gruppo, quella che l'{@link Automa} ha costruito per la visita: dura quanto la visita e poi si butta.
 * Non è della mappa (che sa solo di che tipo è la casella e le sue proprietà) né del gruppo (che sa solo dove sta): è del motore.
 */
public final class LocazioneCorrente {

	private static volatile Locazione corrente;

	private LocazioneCorrente() {
	}

	/**
	 * La locazione in cui si trova il gruppo, o null se non ne ha ancora costruita una (per esempio dopo un caricamento).
	 */
	public static Locazione get() {
		return corrente;
	}

	/**
	 * Cambia la locazione in cui si trova il gruppo.
	 */
	public static void set(Locazione locazione) {
		corrente = locazione;
	}

	/**
	 * L'oggetto che si vede a terra nella locazione corrente, o null se non ce n'è.
	 */
	public static TipoBottino getTipoBottino() {
		Locazione locazione = corrente;
		Bottino oggetto = locazione == null ? null : locazione.getOggetto();
		return oggetto == null ? null : oggetto.getClasse();
	}

	/**
	 * Dimentica la locazione corrente.
	 */
	static void azzera() {
		corrente = null;
	}
}
