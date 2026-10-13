package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStatistiche;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.tipi.Comando;

/**
 * La base degli oggetti delle locazioni: tiene la quantità, che di solito si tira a caso fra uno e il massimo della classe, e
 * quando l'oggetto viene preso assegna i punti del suo valore e riapre le finestre di stato e di statistiche.
 */
public abstract class OggettoBase implements Oggetto {
	
	protected int quantita;
	
	OggettoBase() {
		int quantitaMassima = getClasse().getQuantitaMassima();
		quantita = Dado.tiraAncheAUnaFaccia(quantitaMassima);
	}

	/**
	 * Un oggetto in una quantità decisa da chi lo crea, non dal dado (vedi OggettoMissione).
	 */
	OggettoBase(int quantita) {
		this.quantita = quantita;
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
		BusEventi.pubblica(new InternoMostraFinestraStato());
		BusEventi.pubblica(new InternoMostraFinestraStatistiche());
		return true;
	}
}
