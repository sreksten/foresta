package com.threeamigos.foresta.offerte;

import java.util.List;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public class AiutoGratuito implements Offerta {

	private Personaggio personaggio;
	private int durata;

	public AiutoGratuito() {
		durata = Random.getInt(5) + 1;
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gng) {
		List<Personaggio> personaggiAvversari = gng.getPersonaggiVivi();
		if (!personaggiAvversari.isEmpty()) {
			personaggio = personaggiAvversari.get(0);
			return true;
		}
		return false;
	}

	@Override
	// Qualcuno con se ha comunque un costo...
	public boolean isGratuita(GruppoGiocatore g, GruppoAvversario gng) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capoAvversario = gruppoAvversario.getCapo();
		Personaggio.Sesso sesso = capoAvversario.getSesso();
		StringBuilder sb = new StringBuilder();
		if (gruppoAvversario.getNumeroPersonaggiVivi() > 1) {
			if (sesso == Personaggio.Sesso.MASCHIO) {
				sb.append("Uno");
			} else {
				sb.append("Una");
			}
			sb.append(" di loro ");
		} else {
			sb.append(Character.toUpperCase(capoAvversario.getADS().charAt(0)))
			.append(capoAvversario.getADS().substring(1))
			.append(capoAvversario.getNomeSingolare());
		}
		sb.append(" e' dispost").append(sesso == Personaggio.Sesso.MASCHIO ? 'o' : 'a').append(" ad accompagnare ");
		if (gruppo.getNumeroPersonaggiVivi() > 1) {
			sb.append("il gruppo");
		} else {
			sb.append(gruppo.getCapo().getNome());
		}
		sb.append(" per un po'.");
		return sb.toString();
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// il primo turno scatta subito
		personaggio.setTempo(durata + 1);
		gruppo.aggiungiPersonaggio(personaggio);
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
		UI.rinfresca();
	}
}