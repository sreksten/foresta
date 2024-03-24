package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public abstract class OggettoBase implements Oggetto {
	
	protected int quantita;
	
	OggettoBase() {
		quantita = Random.getInt(getClasse().getQuantitaMassima() + 1);
	}
	
	public final int getQuantita() {
		return quantita;
	}

	/**
	 * Il gruppo riesce a prendere l'oggetto con l'azione specificata?
	 * (Se il gruppo è composto da più persone e l'oggetto interagisce 
	 * occorre passare una azione che indica il personaggio che lo fa)
	 * @param gruppo
	 * @param azione
	 * @return true se l'oggetto è stato raccolto
	 */
	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		Statistiche.addPunti(getClasse().getValore() * quantita);
		UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
		UI.rinfresca();
		return true;
	}
}
