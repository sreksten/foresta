package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;

public interface Oggetto {

	/**
	 * Articolo indeterminativo singolare
	 */
	public String getAIS();
	/**
	 * Una sorta di "articolo indeterminativo plurale" (alcuni, alcune)
	 */
	public String getAIP();
	/**
	 * Articolo determinativo singolare
	 */
	public String getADS();
	/**
	 * Articolo determinativo plurale
	 */
	public String getADP();

	/**
	 * La classe dell'oggetto da ClassiOggetto
	 */
	public ClassiOggetto getClasse();

	/**
	 * Il nome singolare dell'oggetto
	 */
	public String getNomeSingolare();
	/**
	 * Il nome plurale dell'oggetto
	 */
	public String getNomePlurale();

	/**
	 * Quanti oggetti il gruppo giocante ha trovato
	 */
	public int getQuantita();

	/**
	 * @param azione il numero del personaggio che prende l'oggetto, scelto tra
	 *        Azione.INVALIDA (in questo caso il personaggio principale del gruppo prende
	 *        l'oggetto) e Azione.PERSONAGGIO_[1|2|3|4|5].
	 * @return true se e' possibile prendere l'oggetto mediante Azione.AZIONE_INVALIDA
	 *         false se occorre specificare il personaggio attivo, ossia il personaggio che
	 *         prende l'oggetto e ne subisce le conseguenze.
	 */
	public boolean prendi(GruppoGiocatore gruppo, Comando azione);
}
