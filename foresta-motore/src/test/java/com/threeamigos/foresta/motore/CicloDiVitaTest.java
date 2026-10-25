package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'app che va in background e torna (e che il sistema può chiudere): il temporizzatore si ferma, fra una locazione e
 * l'altra si scrive il salvataggio automatico, e alla riapertura si riprende da lì.
 */
class CicloDiVitaTest {

	private static PartitaDiTest iniziaInCitta(PartitaDiTest partita) {
		return partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
	}

	@Test
	void inBackgroundIlTemporizzatoreSiFermaETornaInPrimoPianoRipassa() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(141)) {
			assertFalse(partita.temporizzatore().isSospeso());

			partita.inBackground();
			assertTrue(partita.temporizzatore().isSospeso());

			partita.inPrimoPiano();
			assertFalse(partita.temporizzatore().isSospeso());
		}
	}

	@Test
	void fraUnaLocazioneELAltraInBackgroundScriveIlSalvataggioAutomatico() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(141)) {
			iniziaInCitta(partita).comando(Comando.ESCI_DA_CITTA);
			partita.assertStato(Stato.SCELTA_DIREZIONE);

			partita.inBackground();

			assertTrue(partita.salvataggi().contiene(Comando.SALVATAGGIO_AUTOMATICO));
			assertTrue(partita.salvataggi().getSalvataggiDisponibili().isEmpty(),
					"lo slot automatico non e' fra quelli che sceglie il giocatore");
		}
	}

	@Test
	void dentroUnaLocazioneNonSiScriveNiente() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(142)) {
			iniziaInCitta(partita);
			partita.assertStato(Stato.IN_LOCAZIONE);

			partita.inBackground();

			assertFalse(partita.salvataggi().contiene(Comando.SALVATAGGIO_AUTOMATICO), "a meta' di una locazione non si salva");
			assertTrue(partita.temporizzatore().isSospeso(), "ma il temporizzatore si ferma comunque");
		}
	}

	@Test
	void unaPartitaInterrottaSiRiprendeDalSalvataggioAutomatico() {
		GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
		CoordinateMD doveEra;
		try (PartitaDiTest prima = PartitaDiTest.nuovaConSalvataggi(143, salvataggi)) {
			iniziaInCitta(prima).comando(Comando.ESCI_DA_CITTA);
			doveEra = Foresta.getPosizioneDelGruppo();
			prima.inBackground();
		}

		// Il processo e' stato chiuso: l'app riparte e trova il salvataggio automatico
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(144, salvataggi)) {
			dopo.assertStato(Stato.INTRO);

			assertTrue(dopo.riprendiPartitaInterrotta());

			assertEquals(doveEra, Foresta.getPosizioneDelGruppo());
			assertEquals(1, dopo.gruppo().getNumeroPersonaggi());
			assertTrue(dopo.stato() == Stato.ATTESA_DIREZIONE || dopo.stato() == Stato.SCELTA_DIREZIONE, "" + dopo.stato());
		}
	}

	@Test
	void ripresaLaPartitaIlSalvataggioAutomaticoResta() {
		GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
		try (PartitaDiTest prima = PartitaDiTest.nuovaConSalvataggi(147, salvataggi)) {
			iniziaInCitta(prima).comando(Comando.ESCI_DA_CITTA);
			prima.inBackground();
		}
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(148, salvataggi)) {
			assertTrue(dopo.riprendiPartitaInterrotta());

			assertTrue(salvataggi.contiene(Comando.SALVATAGGIO_AUTOMATICO), "è il punto da cui ripartire se l'app si richiude prima di un altro background");
		}
	}

	@Test
	void unaPartitaNuovaCancellaIlSalvataggioAutomaticoDiQuellaDiPrima() {
		GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
		try (PartitaDiTest prima = PartitaDiTest.nuovaConSalvataggi(149, salvataggi)) {
			iniziaInCitta(prima).comando(Comando.ESCI_DA_CITTA);
			prima.inBackground();
			assertTrue(salvataggi.contiene(Comando.SALVATAGGIO_AUTOMATICO));
		}
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(150, salvataggi)) {
			iniziaInCitta(dopo);

			assertFalse(salvataggi.contiene(Comando.SALVATAGGIO_AUTOMATICO), "la partita nuova non può essere sostituita da quella vecchia");
		}
	}

	@Test
	void caricareUnAltroSalvataggioCancellaQuelloAutomatico() {
		GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
		try (PartitaDiTest prima = PartitaDiTest.nuovaConSalvataggi(151, salvataggi)) {
			iniziaInCitta(prima).comando(Comando.ESCI_DA_CITTA);
			salvataggi.salva(Comando.NUMERO_1);
			prima.inBackground();
			assertTrue(salvataggi.contiene(Comando.SALVATAGGIO_AUTOMATICO));
		}
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(152, salvataggi)) {
			dopo.comando(Comando.FLOPPY_CARICA).comando(Comando.NUMERO_1);

			assertTrue(dopo.stato() == Stato.ATTESA_DIREZIONE || dopo.stato() == Stato.SCELTA_DIREZIONE, "" + dopo.stato());
			assertFalse(salvataggi.contiene(Comando.SALVATAGGIO_AUTOMATICO), "l'automatico è di un'altra partita");
		}
	}

	@Test
	void senzaSalvataggioAutomaticoNonSiRiprendeNienteESiRestaAllIntro() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(145)) {
			assertFalse(partita.riprendiPartitaInterrotta());

			partita.assertStato(Stato.INTRO);
			assertTrue(partita.comandiDisponibili().contains(Comando.PERGAMENA), "l'intro e' di nuovo a posto");
		}
	}

	@Test
	void aPartitaInCorsoNonSiRipreneNiente() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(146)) {
			iniziaInCitta(partita);
			partita.salvataggi().salva(Comando.SALVATAGGIO_AUTOMATICO);

			assertFalse(partita.riprendiPartitaInterrotta());

			partita.assertStato(Stato.IN_LOCAZIONE);
		}
	}
}
