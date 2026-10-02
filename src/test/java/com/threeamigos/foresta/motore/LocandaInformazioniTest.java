package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una locanda dà al massimo Costanti.LOCANDA_MASSIMO_INFORMAZIONI informazioni in tutta la partita:
 * andando avanti e indietro non si scopre tutta la Foresta.
 */
class LocandaInformazioniTest {

	@Test
	void unaLocandaDaAlMassimoTreInformazioni() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			// A gruppo pieno in locanda non si incontra nessuno: si ricevono informazioni
			while (partita.gruppo().getNumeroPersonaggiPermanenti() < Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
				partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			}

			for (int visita = 0; visita < Costanti.LOCANDA_MASSIMO_INFORMAZIONI + 2; visita++) {
				partita.comando(Comando.LOCANDA);
				partita.comando(Comando.PERGAMENA);
				// Niente pernottamento: si torna in piazza
				partita.comando(Comando.NO);
				partita.assertComandoDisponibile(Comando.LOCANDA);
			}

			assertEquals(Costanti.LOCANDA_MASSIMO_INFORMAZIONI, informazioniRicevute(partita));
			assertEquals(2, partita.eventi().tutti(NotificaTestoParagrafo.class).stream()
							.filter(n -> n.getMessaggio().equals("Nessuno ha più nulla di nuovo da raccontare."))
							.count(),
					"nelle due visite in piu' la locanda non ha piu' nulla da raccontare");

			// Il conto e' della locanda e resta anche dopo un caricamento
			GestoreSalvataggi.salva(Comando.NUMERO_3);
			assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_3));
			CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
			assertEquals(String.valueOf(Costanti.LOCANDA_MASSIMO_INFORMAZIONI),
					Foresta.getLocazioneMD(nyena).ottieniProprieta(Locanda.LOCANDA_INFORMAZIONI_DATE));
		}
	}

	private static long informazioniRicevute(PartitaDiTest partita) {
		return partita.eventi().tutti(NotificaTestoParagrafo.class).stream()
				.filter(n -> n.getMessaggio().startsWith("Scambiando quattro chiacchiere"))
				.count();
	}
}
