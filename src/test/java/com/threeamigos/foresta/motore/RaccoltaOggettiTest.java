package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.OggettiDiProva;
import com.threeamigos.foresta.oggetti.Moneta;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'oggetto di fine locazione: chi lo raccoglie sa se era custodito dagli avversari. In
 * citta' non c'e' nessuno a custodirlo, anche se il gruppo avversario contiene il
 * personaggio che si puo' incontrare.
 */
class RaccoltaOggettiTest {

	@Test
	void unOggettoInCittaEIncustodito() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoOggettoRaccolto.class);
			Moneta moneta = new Moneta();
			OggettiDiProva.impostaOggetto(partita.gruppo().getLocazioneCorrente(), moneta);

			partita.comando(Comando.ESCI_DA_CITTA);

			InternoOggettoRaccolto raccolto = partita.eventi().ultimo(InternoOggettoRaccolto.class);
			assertEquals(moneta.getClasse(), raccolto.getClasse());
			assertEquals(moneta.getQuantita(), raccolto.getQuantita());
			assertFalse(raccolto.isCustodito());
			assertFalse(raccolto.getArtefatto().isPresent(), "una moneta non e' un artefatto");
		}
	}

	@Test
	void unOggettoTenutoDagliAvversariSconfittiECustodito() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoOggettoRaccolto.class);
			OggettiDiProva.impostaOggetto(partita.gruppo().getLocazioneCorrente(), new Moneta());
			for (Personaggio avversario : GruppoAvversario.getIstanza().getPersonaggiVivi()) {
				avversario.subSalute(avversario.getSalute(), partita.gruppo().getCapo(), Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
			}

			// Senza avversari vivi da attaccare la locazione e' completa e si raccoglie l'oggetto
			partita.comando(Comando.SINGOLO_ATTACCO);

			assertTrue(partita.eventi().ultimo(InternoOggettoRaccolto.class).isCustodito());
		}
	}
}
