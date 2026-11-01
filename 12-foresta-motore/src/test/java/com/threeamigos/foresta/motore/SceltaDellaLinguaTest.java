package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
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
}
