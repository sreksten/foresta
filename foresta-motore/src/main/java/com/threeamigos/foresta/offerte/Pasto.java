package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Costanti;

/**
 * L'offerta gratuita di un pasto caldo: di solito squisito, e allora rende un po' di salute a tutti i personaggi vivi; a volte non incontra i gusti del gruppo.
 */
public class Pasto implements Offerta {

	private final int gustoso = Dado.tira(4);
	
	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		if (gustoso > 1) {
			return "Viene offerto un pasto caldo che risulta essere squisito.";
		} else {
			StringBuilder sb = new StringBuilder("Viene offerto un pasto caldo che però non incontra i gusti ");
			if (gruppo.getNumeroPersonaggiVivi() == 1) {
				sb.append(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA)).append('.');
			} else {
				sb.append("del gruppo.");
			}
			return sb.toString();
		}
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		if (gustoso > 1) {
			for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
				personaggio.addSalute(Costanti.RECUPERO_SALUTE_DA_PASTO);
			}
			BusEventi.pubblica(new InternoMostraFinestraStato());
		}
	}
}
