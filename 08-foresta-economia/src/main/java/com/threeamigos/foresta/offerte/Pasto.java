package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Testi;

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
			return Testi.formatta("offerte", "PASTO.SQUISITO");
		}
		return gruppo.getNumeroPersonaggiVivi() == 1
				? Testi.formatta("offerte", "PASTO.NON_PIACE_A", gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA))
				: Testi.formatta("offerte", "PASTO.NON_PIACE_AL_GRUPPO");
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		if (gustoso > 1) {
			for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
				personaggio.addSalute(CostantiEconomia.RECUPERO_SALUTE_DA_PASTO);
			}
			BusEventi.pubblica(new InternoMostraFinestraStato());
		}
	}
}
