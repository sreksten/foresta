package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.OggettoConArticoli;

public interface Oggetto extends OggettoConArticoli {

	/**
	 * La classe dell'oggetto da ClassiOggetto
	 */
    ClassiOggetto getClasse();

	/**
	 * Il nome singolare dell'oggetto
	 */
    String getNomeSingolare();
	/**
	 * Il nome plurale dell'oggetto
	 */
    String getNomePlurale();

	/**
	 * Quanti oggetti il gruppo giocante ha trovato
	 */
    int getQuantita();

	/**
	 * @param azione il numero del personaggio che prende l'oggetto, scelto tra
	 *        Azione.INVALIDA (in questo caso il personaggio principale del gruppo prende
	 *        l'oggetto) e Azione.PERSONAGGIO_[1|2|3|4|5].
	 * @return true se è possibile prendere l'oggetto mediante Azione.AZIONE_INVALIDA
	 *         false se occorre specificare il personaggio attivo, ossia il personaggio che
	 *         prende l'oggetto e ne subisce le conseguenze.
	 */
    boolean prendi(GruppoGiocatore gruppo, Comando azione);
}
