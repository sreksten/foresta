package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPortaInPrimoPiano;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public abstract class OggettoBase implements Oggetto {
	
	protected int quantita;
	
	OggettoBase() {
		int quantitaMassima = getClasse().getQuantitaMassima();
		quantita = Dado.tiraAncheAUnaFaccia(quantitaMassima);
	}
	
	public final int getQuantita() {
		return quantita;
	}

	/**
	 * Il gruppo riesce a prendere l'oggetto con l'azione specificata?
	 * (Se il gruppo è composto da più persone e l'oggetto interagisce 
	 * occorre passare una azione che indica il personaggio che lo fa)
	 * @return true se l'oggetto è stato raccolto
	 */
	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		Statistiche.addPunti(getClasse().getValore() * quantita);
		BusEventi.pubblica(new InternoPortaInPrimoPiano(InterfacciaUtente.Finestra.STATO, InterfacciaUtente.Finestra.STATISTICHE));
		return true;
	}
}
