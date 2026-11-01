package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * La notte del gruppo: il racconto, l'orologio e gli eventi che dipendono dal giorno. Il riposo vero e proprio lo fa il gruppo
 * ({@link GruppoGiocatore#riposa(int, TipoRiposo)}), che non sa niente del tempo del mondo né di come si racconta.
 */
public final class Pernottamento {

	private Pernottamento() {
	}

	/**
	 * Fa passare la notte al gruppo, fino al mattino, con il tipo di riposo indicato: l'orologio arriva alle otto del mattino, e gli eventi
	 * che dipendono dal giorno (il drago e le città) hanno la loro occasione di accadere, in una locanda come all'accampamento.
	 */
	public static void pernotta(GruppoGiocatore gruppo, TipoRiposo tipoRiposo) {
		Logger.log("Inizio pernottamento");
		TipoLocazione tipoLocazione = Foresta.getLocazioneDelGruppo();
		int ore = LineaTemporale.oreFinoAlMattino();
		boolean alCoperto = tipoLocazione == TipoLocazione.LOCANDA
				|| tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA;
		Logger.log(() -> "Pernottamento al coperto? " + alCoperto);
		Personaggio capo = gruppo.getCapo();
		if (alCoperto) {
			BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "Pernottamento.alCoperto",
					gruppo.getNumeroPersonaggiVivi() > 1 ? Testi.formatta("gruppo", "TUTTO") : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE))));
		} else {
			// Prima l'accampamento, poi un giudizio sulle ore di riposo (1-5 ore ne hanno uno loro, oltre bastano)
			String giudizio = ore >= 1 && ore <= 5 ? Testi.formatta("messaggi_motore", "Pernottamento.ore." + ore)
					: Testi.formatta("messaggi_motore", "Pernottamento.abbastanza", gruppo.getNumeroPersonaggiVivi() > 1 ? Lingue.corrente().inizialeMaiuscola(Testi.formatta("gruppo", "CHI"))
							: capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
			BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "Pernottamento.accampamento",
					Lingue.corrente().cardinale(gruppo.getNumeroPersonaggi(), Genere.MASCHILE), giudizio)));
		}
		gruppo.riposa(ore, tipoRiposo);
		// Dormendo passa il tempo: prima il riposo, poi gli effetti di stato e le ricariche scorrono un turno per ogni ora di sonno, pesata dal tipo di riposo
		// e arrotondata per eccesso (un'ora all'aperto senza fuoco conta comunque uno)
		int turni = (int) Math.ceil(ore * tipoRiposo.getMoltiplicatore());
		for (int turno = 0; turno < turni && gruppo.getCapo().isVivo(); turno++) {
			gruppo.trascorriUnTurno();
		}
		LineaTemporale.mattinoSeguente();
		LineaTemporale.eventiDiNotte(gruppo);
		// La locanda fuori città non può cadere: solo una città può essere distrutta
		if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA && Foresta.isCittaDistrutta(tipoLocazione)) {
			// La città in cui si dormiva è caduta stanotte: lo si racconta subito, non alla locazione dopo
			String evento = LineaTemporale.getEvento();
			if (evento != null) {
				BusEventi.pubblica(new NotificaTestoParagrafo(evento));
			}
		}
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "Pernottamento.1")));
	}
}
