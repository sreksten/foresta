package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * Un'offerta che il gruppo può ricevere facendo amicizia, corrompendo o in una locanda: un aiuto, delle pergamene, delle informazioni, una
 * mappa, un pasto. Alcune sono gratuite e si accettano subito, altre costano monete e hanno dei prerequisiti.
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
