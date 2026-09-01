package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * Facendo amicizia un gruppo puo' ricevere un'offerta.
 */

public interface Offerta {

	/**
	 * Se esistono dei prerequisiti (ad esempio per comprare la mappa occorrono delle monete)
	 */
	boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gng);

	/**
	 * Se è gratuita viene accettata subito
	 */
	boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gng);

	/**
	 * La descrizione dell'offerta, per sapere cosa ci danno/vendono
	 */
	String getDescrizione(GruppoGiocatore g, GruppoAvversario gng);

	/**
	 * Il gruppo accetta l'offerta. Potrebbe essere necessario abbassare il numero
	 * di monete, alzare la forza, eccetera.
	 */
	void accetta(GruppoGiocatore g, GruppoAvversario gng);
}
