package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.mappa.CategoriaLocazione;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;

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
		TipoLocazione tipoLocazione = gruppo.getTipoLocazioneCorrente();
		int ore = LineaTemporale.oreFinoAlMattino();
		boolean alCoperto = tipoLocazione == TipoLocazione.LOCANDA
				|| tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA;
		Logger.log(() -> "Pernottamento al coperto? " + alCoperto);
		Personaggio capo = gruppo.getCapo();
		if (alCoperto) {
			StringBuilder sb = new StringBuilder("La stanchezza accumulata ed il tepore delle coperte fanno addormentare subito ");
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				sb.append("tutto il gruppo");
			} else {
				sb.append(capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			}
			sb.append(". La notte alla locanda trascorre placida e tranquilla.");
			BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
		} else {
			StringBuilder sb = new StringBuilder("Il gruppo decide di accamparsi qui per riposare un po'. Dopo aver stabilito i turni di guardia, i ")
					.append(Numeri.getCardinaleM(gruppo.getNumeroPersonaggi())).append(" intrepidi avventurieri si godono un meritato riposo. ");
			if (ore == 1) {
				sb.append("Ma un'ora sola è veramente insufficiente");
			} else if (ore == 2) {
				sb.append("Ma due ore sono insufficienti");
			} else if (ore == 3) {
				sb.append("Ma tre ore sono troppo poche");
			} else if (ore == 4) {
				sb.append("Ma quattro ore sono un po' poche");
			} else if (ore == 5) {
				sb.append("Cinque ore sono appena sufficienti");
			} else {
				if (gruppo.getNumeroPersonaggiVivi() > 1) {
					sb.append("Il gruppo");
				} else {
					sb.append(capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
				}
				sb.append(" ha riposato abbastanza");
			}
			sb.append(" per rimettersi in forze.");
			BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
		}
		gruppo.riposa(ore, tipoRiposo);
		LineaTemporale.mattinoSeguente();
		LineaTemporale.eventiDiNotte(gruppo);
		// La locanda fuori città non può cadere: solo una città può essere distrutta
		if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA && LineaTemporale.isCittaDistrutta(tipoLocazione)) {
			// La città in cui si dormiva è caduta stanotte: lo si racconta subito, non alla locazione dopo
			String evento = LineaTemporale.getEvento();
			if (evento != null) {
				BusEventi.pubblica(new NotificaTestoParagrafo(evento));
			}
		}
		BusEventi.pubblica(new NotificaTestoFrase("Il sole sorge e l'avventura ricomincia."));
	}
}
