package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Primo scenario completo: un Ladro inizia la partita in città, va dall'armaiolo, compra un artefatto e ne
 * rivende un altro, poi torna in piazza. Tutto passa dall'Automa e dal bus, come in gioco.
 */
class ScenarioArmaioloTest {

	private PartitaDiTest partita;

	@BeforeEach
	void iniziaInCitta() {
		partita = PartitaDiTest.nuova(1234);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
				() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		partita.assertComandoDisponibile(Comando.ARMAIOLO);
	}

	@AfterEach
	void chiudi() {
		partita.close();
	}

	@Test
	void compraDallArmaioloAlPrezzoTrattato() {
		AutomaAcquistiArtefatti bottega = entraDallArmaiolo();
		Artefatto scelto = new ArrayList<>(bottega.getParteRemota().getInventario()).get(0);
		int moneteIniziali = partita.gruppo().getMonete();
		int prezzo = partita.gruppo().prezzoAcquisto(scelto.getCostoAcquisto());

		bottega.richiediSpostamentoSuParteAttiva(scelto);

		assertTrue(partita.eventi().haRicevuto(NotificaApprovazioneAcquistoArtefatto.class));
		assertEquals(moneteIniziali - prezzo, partita.gruppo().getMonete());
		assertTrue(PartitaDiTest.contiene(partita.gruppo().getInventario(), scelto));
		assertFalse(PartitaDiTest.contiene(bottega.getParteRemota().getInventario(), scelto));
		// Il Ladro porta lo Scudo Fiscale: tratta al massimo, lo sconto è il 20% pieno
		assertEquals(RegoleContrattazione.prezzoAcquisto(scelto.getCostoAcquisto(), 20), prezzo);
	}

	@Test
	void rivendeAllArmaioloAlPrezzoTrattato() {
		AutomaAcquistiArtefatti bottega = entraDallArmaiolo();
		Artefatto scelto = new ArrayList<>(bottega.getParteRemota().getInventario()).get(0);
		bottega.richiediSpostamentoSuParteAttiva(scelto);
		int moneteDopoAcquisto = partita.gruppo().getMonete();

		bottega.richiediSpostamentoSuParteRemota(scelto);

		assertTrue(partita.eventi().haRicevuto(NotificaApprovazioneVenditaArtefatto.class));
		assertEquals(moneteDopoAcquisto + partita.gruppo().prezzoVendita(scelto.getCostoAcquisto()), partita.gruppo().getMonete());
		assertTrue(PartitaDiTest.contiene(bottega.getParteRemota().getInventario(), scelto));
		assertTrue(partita.gruppo().prezzoVendita(scelto.getCostoAcquisto()) < partita.gruppo().prezzoAcquisto(scelto.getCostoAcquisto()));
	}

	@Test
	void usciteDallArmaioloSiTornaInPiazza() {
		entraDallArmaiolo();
		partita.comando(Comando.ANNULLA);
		partita.assertStato(Stato.IN_LOCAZIONE);
		partita.assertComandoDisponibile(Comando.ESCI_DA_CITTA);
	}

	private AutomaAcquistiArtefatti entraDallArmaiolo() {
		partita.comando(Comando.ARMAIOLO);
		List<ComandoAperturaInventarioCommerciante> aperture = partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class);
		assertEquals(1, aperture.size());
		AutomaAcquistiArtefatti bottega = aperture.get(0).getAutomaAcquistiArtefatti();
		assertFalse(bottega.getParteRemota().getInventario().isEmpty(), "l'armaiolo ha il magazzino vuoto");
		return bottega;
	}
}
