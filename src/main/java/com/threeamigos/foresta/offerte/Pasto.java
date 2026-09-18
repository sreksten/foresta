package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMostraFinestra;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;

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
			BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
		}
	}
}
