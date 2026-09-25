package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPortaInPrimoPiano;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;

import java.util.List;

public class AiutoMercenario implements Offerta {

	private Personaggio personaggio;
	private final int costo;

	public AiutoMercenario() {
		costo = Dado.tira(6);
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		// A gruppo pieno nessuno si puo' unire (vedi anche PersonaggioBase.getOfferta)
		if (gruppo.getNumeroPersonaggi() >= Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
			return false;
		}
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
			sb.append(capoAvversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		}
		sb.append(" è dispost").append(capoAvversario.getLetteraFinaleAttributo()).append(" ad accompagnare ");
		if (gruppo.getNumeroPersonaggiVivi() > 1) {
			sb.append("il gruppo");
		} else {
			sb.append(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
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
		BusEventi.pubblica(new InternoPortaInPrimoPiano(InterfacciaUtente.Finestra.STATO));
	}
}