package com.threeamigos.foresta.offerte;

import java.util.List;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public class AiutoMercenario implements Offerta {

	private Personaggio personaggio;
	private int costo;

	public AiutoMercenario() {
		costo = Random.getInt(5) + 1;
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		if (gruppo.getMonete() < costo) {
			return false;
		}
		List<Personaggio> personaggiAvversari = gruppoAvversario.getPersonaggiVivi();
		if (!personaggiAvversari.isEmpty()) {
			personaggio = personaggiAvversari.get(0);
			return true;
		}
		return false;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
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
			sb.append(" di loro per ");
			if (costo == 1) {
				sb.append("una moneta");
			} else {
				sb.append(costo).append(" monete");
			}
		} else {
			sb.append("Per ");
			if (costo == 1) {
				sb.append("una moneta ");
			} else {
				sb.append(costo).append(" monete ");
			}
			sb.append(capoAvversario.getADS()).append(capoAvversario.getNomeSingolare());
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
		personaggio.setTempo(costo + 1);
		gruppo.aggiungiPersonaggio(personaggio);
		gruppo.subMonete(costo);
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
		UI.rinfresca();
	}
}