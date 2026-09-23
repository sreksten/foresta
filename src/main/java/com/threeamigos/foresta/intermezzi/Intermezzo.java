package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.Costanti;

import java.util.List;

/**
 * Un intermezzo (cutscene): una sequenza di pagine di testo mostrate a tutto schermo
 * quando si verificano determinate condizioni di gioco. Come una Missione ha un innesco,
 * ma a differenza di questa non ha progressi: una volta mostrato, il registro lo segna
 * come già scattato e non si ripete più.
 * <p>
 * Le pagine avanzano al click sulla pergamena e, se {@link #getSecondiPerPagina()} è
 * maggiore di zero, anche da sole dopo quel numero di secondi.
 */
public interface Intermezzo {

	/**
	 * L'identificativo con cui il registro ricorda che l'intermezzo è già scattato.
	 */
	String getId();

	/**
	 * L'innesco: se nel momento indicato l'intermezzo deve essere mostrato. Viene
	 * interrogato solo finché l'intermezzo non è ancora scattato.
	 */
	boolean deveScattare(MomentoIntermezzo momento);

	/**
	 * Le pagine da mostrare, generate al momento in cui l'intermezzo scatta (possono
	 * quindi usare il nome dell'eroe, la città più vicina, eccetera).
	 */
	List<String> getPagine();

	/**
	 * Dopo quanti secondi una pagina avanza da sola; 0 perché avanzi solo al click.
	 */
	default int getSecondiPerPagina() {
		return Costanti.SECONDI_PER_PAGINA_INTERMEZZO;
	}
}
