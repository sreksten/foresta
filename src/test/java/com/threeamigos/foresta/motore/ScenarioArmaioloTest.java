package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
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
				() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		partita.assertComandoDisponibile(Comando.ARMAIOLO);
	}

	@AfterEach
	void chiudi() {
		partita.close();
	}

	@Test
	void compraDallArmaioloAlPrezzoTrattato() {
		// Il Ladro imbraccia lo Scudo Fiscale, che aggiunge 4 alla sua contrattazione
		int contrattazioneSenzaScudo = partita.gruppo().getContrattazione();
		partita.gruppo().getCapo().addArtefatto(Leggendari.con(Leggendari.SCUDO_DELL_ESATTORE).costruisci());
		VistaScambio bottega = entraDallArmaiolo();
		Artefatto scelto = new ArrayList<>(bottega.getInventarioParteRemota()).get(0);
		int moneteIniziali = partita.gruppo().getMonete();
		int prezzo = partita.gruppo().prezzoAcquisto(scelto.getCostoAcquisto());

		compra(bottega, scelto);

		assertTrue(partita.eventi().haRicevuto(NotificaApprovazioneAcquistoArtefatto.class));
		assertEquals(moneteIniziali - prezzo, partita.gruppo().getMonete());
		assertTrue(PartitaDiTest.contiene(partita.gruppo().getInventario(), scelto));
		assertFalse(PartitaDiTest.contiene(bottega.getInventarioParteRemota(), scelto));
		// Lo sconto è quello della sua contrattazione, scudo compreso
		int contrattazione = partita.gruppo().getContrattazione();
		assertTrue(contrattazione > contrattazioneSenzaScudo, "contrattazione " + contrattazione);
		assertEquals(RegoleContrattazione.prezzoAcquisto(scelto.getCostoAcquisto(), contrattazione), prezzo);
	}

	@Test
	void rivendeAllArmaioloAlPrezzoTrattato() {
		VistaScambio bottega = entraDallArmaiolo();
		Artefatto scelto = new ArrayList<>(bottega.getInventarioParteRemota()).get(0);
		compra(bottega, scelto);
		int moneteDopoAcquisto = partita.gruppo().getMonete();

		vendi(bottega, scelto);

		assertTrue(partita.eventi().haRicevuto(NotificaApprovazioneVenditaArtefatto.class));
		assertEquals(moneteDopoAcquisto + partita.gruppo().prezzoVendita(scelto.getCostoAcquisto()), partita.gruppo().getMonete());
		assertTrue(PartitaDiTest.contiene(bottega.getInventarioParteRemota(), scelto));
		assertTrue(partita.gruppo().prezzoVendita(scelto.getCostoAcquisto()) < partita.gruppo().prezzoAcquisto(scelto.getCostoAcquisto()));
	}

	@Test
	void usciteDallArmaioloSiTornaInPiazza() {
		entraDallArmaiolo();
		partita.comando(Comando.ANNULLA);
		partita.assertStato(Stato.IN_LOCAZIONE);
		partita.assertComandoDisponibile(Comando.ESCI_DA_CITTA);
	}

	private VistaScambio entraDallArmaiolo() {
		partita.comando(Comando.ARMAIOLO);
		List<ComandoAperturaInventarioCommerciante> aperture = partita.eventi().tutti(ComandoAperturaInventarioCommerciante.class);
		assertEquals(1, aperture.size());
		VistaScambio bottega = aperture.get(0).getScambio();
		assertFalse(bottega.getInventarioParteRemota().isEmpty(), "l'armaiolo ha il magazzino vuoto");
		return bottega;
	}

	// Come il doppio click della UI
	private void compra(VistaScambio bottega, Artefatto artefatto) {
		partita.pubblica(new ComandoScambioArtefatto(bottega, ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, artefatto));
	}

	private void vendi(VistaScambio bottega, Artefatto artefatto) {
		partita.pubblica(new ComandoScambioArtefatto(bottega, ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto));
	}
}
