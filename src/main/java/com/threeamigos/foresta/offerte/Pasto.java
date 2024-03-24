package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public class Pasto implements Offerta {

	private int gustoso = Random.getInt(3);
	
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
		if (gustoso > 0) {
			return "Viene offerto un pasto caldo che risulta essere squisito.";
		} else {
			StringBuilder sb = new StringBuilder("Viene offerto un pasto caldo che pero' non incontra i gusti ");
			if (gruppo.getNumeroPersonaggiVivi() == 1) {
				sb.append("di ").append(gruppo.getCapo().getNome()).append('.');
			} else {
				sb.append("del gruppo.");
			}
			return sb.toString();
		}
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		if (gustoso > 0) {
			for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
				personaggio.addForza(50);
			}
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);
			UI.rinfresca();
		}
	}
}
