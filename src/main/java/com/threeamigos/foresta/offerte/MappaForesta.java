package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

public class MappaForesta implements Offerta {

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return gruppo.getMonete() >= Costanti.COSTO_MAPPA_DELLA_FORESTA;
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
			sb.append(" di loro per dieci monete");
		} else {
			sb.append("Per dieci monete ").append(capoAvversario.getADS()).append(capoAvversario.getNomeSingolare());
		}
		sb.append(" è dispost").append(capoAvversario.getLetteraFinaleAttributo()).append(" a vendere una mappa della foresta.");
		return sb.toString();
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		gruppo.subMonete(Costanti.COSTO_MAPPA_DELLA_FORESTA);
		Foresta.ottieniMappa();
		BusEventi.pubblica(new NotificaVariazioneConoscenzaMappa(0, 0, Foresta.getDimensioneX(), Foresta.getDimensioneY()));
	}
}
