package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La schermata delle opzioni, dall'intro: la lingua (che vale da lì in poi e si ricorda nelle opzioni), il tutorial da ripristinare, la classifica e i
 * salvataggi da cancellare (con la conferma). Una partita nuova non chiede più la lingua: comincia dal nome.
 */
class OpzioniTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void unaPartitaNuovaCominciaDalNome() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(201)) {
			assertTrue(partita.comandiDisponibili().contains(Comando.OPZIONI), "nell'intro c'è l'icona delle opzioni");
			partita.comando(Comando.PERGAMENA);
			partita.assertStato(Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO);
		}
	}

	@Test
	void dalleOpzioniSiTornaAllIntroConLAnnulla() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(202)) {
			partita.comando(Comando.OPZIONI);
			partita.assertStato(Stato.OPZIONI);
			assertEquals(Collections.singletonList(Comando.ANNULLA), partita.comandiDisponibili());
			partita.comando(Comando.ANNULLA);
			partita.assertStato(Stato.INTRO);
		}
	}

	@Test
	void laLinguaSceltaValeSubitoERestaNelleOpzioni() {
		Comando[] scelte = {Comando.LINGUA_INGLESE_GB, Comando.LINGUA_INGLESE_USA, Comando.LINGUA_ITALIANO};
		String[] lingue = {"en-GB", "en", "it"};
		for (int i = 0; i < scelte.length; i++) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(210 + i)) {
				partita.comando(Comando.OPZIONI);
				partita.comando(Comando.OPZIONI_LINGUA);
				partita.assertStato(Stato.OPZIONI_LINGUA);
				assertTrue(partita.comandiDisponibili().containsAll(Arrays.asList(Comando.LINGUA_ITALIANO, Comando.LINGUA_INGLESE_USA,
						Comando.LINGUA_INGLESE_GB, Comando.ANNULLA)));
				partita.comando(scelte[i]);
				assertEquals(lingue[i], Testi.getLingua());
				assertEquals(lingue[i], partita.opzioni().getLingua(), "si ricorda");
				partita.assertStato(Stato.OPZIONI);
			}
		}
	}

	@Test
	void unaPartitaNuovaNasceNellaLinguaDelleOpzioni() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(215)) {
			partita.comando(Comando.OPZIONI);
			partita.comando(Comando.OPZIONI_LINGUA);
			partita.comando(Comando.LINGUA_INGLESE_GB);
			partita.comando(Comando.ANNULLA);
			partita.comando(Comando.PERGAMENA);
			partita.testo("Arsenio");
			partita.assertStato(Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO);
			assertEquals("en-GB", ModelloDati.getIstanza().getLingua());
		}
	}

	@Test
	void ripristinareIlTutorialLoRiaccendeESiRestaNelleOpzioni() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(221)) {
			RegistroSuggerimenti.setAttivo(false);
			RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
			partita.comando(Comando.OPZIONI);
			partita.comando(Comando.RIPRISTINA_TUTORIAL);
			assertTrue(RegistroSuggerimenti.isAttivo());
			assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.COMBATTIMENTO));
			partita.assertStato(Stato.OPZIONI);
		} finally {
			RegistroSuggerimenti.ripristina();
		}
	}

	@Test
	void iPunteggiSiCancellanoSoloConIlSi() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(230)) {
			partita.punteggi().addPunteggio("Arsenio", 1000, "partita");
			partita.comando(Comando.OPZIONI);
			partita.comando(Comando.CANCELLA_PUNTEGGI);
			partita.assertStato(Stato.OPZIONI_CANCELLA_PUNTEGGI);
			assertEquals(Arrays.asList(Comando.SI, Comando.NO), partita.comandiDisponibili());
			partita.comando(Comando.NO);
			assertEquals(1, partita.punteggi().getConteggio(), "con il no restano");
			partita.assertStato(Stato.OPZIONI);
			partita.comando(Comando.CANCELLA_PUNTEGGI);
			partita.comando(Comando.SI);
			assertEquals(0, partita.punteggi().getConteggio());
			partita.assertStato(Stato.OPZIONI);
		}
	}

	@Test
	void iSalvataggiSiCancellanoSoloConIlSiEDallIntroSparisceIlCaricamento() {
		GestoreSalvataggiInMemoria salvataggi = new GestoreSalvataggiInMemoria();
		try (PartitaDiTest prima = PartitaDiTest.nuovaConSalvataggi(240, salvataggi)) {
			prima.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			salvataggi.salva(Comando.NUMERO_1);
		}
		try (PartitaDiTest partita = PartitaDiTest.nuovaConSalvataggi(241, salvataggi)) {
			assertTrue(partita.comandiDisponibili().contains(Comando.FLOPPY_CARICA), "c'è da caricare");
			partita.comando(Comando.OPZIONI);
			partita.comando(Comando.CANCELLA_SALVATAGGI);
			partita.comando(Comando.NO);
			assertFalse(partita.salvataggi().getSalvataggiDisponibili().isEmpty(), "con il no restano");
			partita.comando(Comando.CANCELLA_SALVATAGGI);
			partita.comando(Comando.SI);
			assertTrue(partita.salvataggi().getSalvataggiDisponibili().isEmpty());
			partita.comando(Comando.ANNULLA);
			assertFalse(partita.comandiDisponibili().contains(Comando.FLOPPY_CARICA));
		}
	}
}
