package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

import static com.threeamigos.foresta.tipi.CostantiEconomia.COSTO_MAPPA_DELLA_FORESTA;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'offerta della mappa completa della Foresta, a pagamento.
 */
public class MappaForesta implements Offerta {

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return Cassa.getMonete() >= COSTO_MAPPA_DELLA_FORESTA;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capo = gruppoAvversario.getCapo();
		String sesso = capo.getSesso() == Personaggio.Sesso.MASCHIO ? "M" : "F";
		boolean inPiu = gruppoAvversario.getNumeroPersonaggiVivi() > 1;
		String prezzo = Testi.formatta("offerte", "MONETE_N", COSTO_MAPPA_DELLA_FORESTA);
		return inPiu ? Testi.formatta("offerte", "MAPPA_FORESTA.GRUPPO." + sesso, prezzo)
				: Testi.formatta("offerte", "MAPPA_FORESTA.SINGOLO." + sesso, prezzo, capo.getADS() + capo.getNomeSingolare());
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Cassa.subMonete(COSTO_MAPPA_DELLA_FORESTA);
		Foresta.ottieniMappa();
		BusEventi.pubblica(new NotificaVariazioneConoscenzaMappa(0, 0, Foresta.getDimensioneX(), Foresta.getDimensioneY()));
	}
}
