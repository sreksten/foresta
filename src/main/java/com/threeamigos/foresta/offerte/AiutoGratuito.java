package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMostraFinestra;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;

import java.util.List;

public class AiutoGratuito implements Offerta {

	private Personaggio personaggio;
	private final int durata;

	public AiutoGratuito() {
		durata = Dado.tira(6);
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
			sb.append(capoAvversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
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
		personaggio.setTempo(durata + 1);
		gruppo.aggiungiPersonaggio(personaggio);
		BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
	}
}