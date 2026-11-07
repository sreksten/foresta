package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una partita nuova può cominciare chiedendo la lingua (se la piattaforma lo offre): la scelta vale per tutta la partita e finisce nel modello dati.
 */
class SceltaDellaLinguaTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void senzaLaSceltaSiPartePerIlNome() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(201)) {
			partita.comando(Comando.PERGAMENA);
			partita.assertStato(Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO);
		}
	}

	@Test
	void conLaSceltaSiChiedeLaLinguaPrimaDelNome() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(202).conSceltaDellaLingua()) {
			partita.comando(Comando.PERGAMENA);
			partita.assertStato(Stato.PRE_GAME_ATTESA_LINGUA);
			assertTrue(partita.comandiDisponibili().containsAll(java.util.Arrays.asList(Comando.LINGUA_ITALIANO, Comando.LINGUA_INGLESE_USA, Comando.LINGUA_INGLESE_GB)));

			partita.comando(Comando.LINGUA_INGLESE_GB);
			assertEquals("en-GB", Testi.getLingua());
			partita.assertStato(Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO);

			partita.testo("Arsenio");
			partita.assertStato(Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO);
			assertEquals("en-GB", ModelloDati.getIstanza().getLingua(), "la partita nasce nella lingua scelta");
		}
	}

	@Test
	void ognunaDelleTreScelteImpostaLaSuaLingua() {
		Comando[] scelte = {Comando.LINGUA_ITALIANO, Comando.LINGUA_INGLESE_USA, Comando.LINGUA_INGLESE_GB};
		String[] lingue = {"it", "en", "en-GB"};
		for (int i = 0; i < scelte.length; i++) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(210 + i).conSceltaDellaLingua()) {
				partita.comando(Comando.PERGAMENA);
				partita.comando(scelte[i]);
				assertEquals(lingue[i], Testi.getLingua());
			}
		}
	}

	@Test
	void conLaSceltaSiOffreAncheIlRipristinoDelTutorial() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(220).conSceltaDellaLingua()) {
			partita.comando(Comando.PERGAMENA);
			assertTrue(partita.comandiDisponibili().contains(Comando.RIPRISTINA_TUTORIAL));
		}
	}

	@Test
	void ripristinareIlTutorialLoRiaccendeERestaAScegliereLaLingua() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(221).conSceltaDellaLingua()) {
			RegistroSuggerimenti.setAttivo(false);
			RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
			partita.comando(Comando.PERGAMENA);
			partita.assertStato(Stato.PRE_GAME_ATTESA_LINGUA);

			partita.comando(Comando.RIPRISTINA_TUTORIAL);

			assertTrue(RegistroSuggerimenti.isAttivo());
			assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.COMBATTIMENTO));
			// Non è una lingua: si resta a scegliere, con le stesse icone
			partita.assertStato(Stato.PRE_GAME_ATTESA_LINGUA);
			assertTrue(partita.comandiDisponibili().containsAll(java.util.Arrays.asList(Comando.LINGUA_ITALIANO, Comando.LINGUA_INGLESE_USA,
					Comando.LINGUA_INGLESE_GB, Comando.RIPRISTINA_TUTORIAL)));
		} finally {
			RegistroSuggerimenti.ripristina();
		}
	}
}
