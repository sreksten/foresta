package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Situazione;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SituazioniDellaUITest {

	private static Set<Situazione> correnti(SituazioniDellaUI ui, Comando... comandi) {
		return ui.correnti(Arrays.asList(comandi), false, false);
	}

	@Test
	void fuoriDallaSchermataDiGiocoNonCeNiente() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		assertTrue(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).isEmpty());
	}

	@Test
	void laPrimaSchermataDiGiocoELaBarraDelleIcone() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertEquals(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), correnti(ui));
		assertEquals(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), correnti(ui, Comando.MAPPA));
	}

	@Test
	void leQuattroIconeDiMovimentoLeggonoLeIcone() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD).contains(Situazione.ICONE_DI_MOVIMENTO_PRESENTI));
		assertTrue(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).contains(Situazione.ICONE_DI_MOVIMENTO_PRESENTI));
	}

	@Test
	void ilCombattimentoELeScelteInCombattimento() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaCombattimentoAperto();
		assertTrue(correnti(ui, Comando.SINGOLO_ATTACCO).contains(Situazione.COMBATTIMENTO_APERTO));
		assertFalse(correnti(ui, Comando.SINGOLO_ATTACCO).contains(Situazione.SCELTE_IN_COMBATTIMENTO));
		assertTrue(correnti(ui, Comando.COMBATTIMENTO, Comando.FUGA).contains(Situazione.SCELTE_IN_COMBATTIMENTO));
		ui.segnaCombattimentoChiuso();
		assertFalse(correnti(ui, Comando.SINGOLO_ATTACCO).contains(Situazione.COMBATTIMENTO_APERTO));
	}

	@Test
	void unOggettoTrovatoVaViaConLaNuovaLocazione() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaOggettoTrovato();
		assertTrue(correnti(ui).contains(Situazione.OGGETTO_TROVATO));
		ui.segnaNuovaLocazione();
		assertFalse(correnti(ui).contains(Situazione.OGGETTO_TROVATO));
	}

	@Test
	void leTatticheSiVedonoSoloDopoDueScontri() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaCombattimentoAperto();
		ui.segnaCombattimentoChiuso();
		assertFalse(correnti(ui, Comando.MOSTRA_TATTICHE).contains(Situazione.TATTICHE_DISPONIBILI));
		ui.segnaCombattimentoAperto();
		// Lo stesso scontro che si riapre non è un altro scontro
		ui.segnaCombattimentoAperto();
		assertEquals(2, ui.getScontriVisti());
		assertTrue(correnti(ui, Comando.MOSTRA_TATTICHE).contains(Situazione.TATTICHE_DISPONIBILI));
		assertFalse(correnti(ui).contains(Situazione.TATTICHE_DISPONIBILI));
	}

	@Test
	void leIconeDiMostriPozioniBottegheEAccampamento() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		Set<Situazione> s = correnti(ui, Comando.CORRUZIONE, Comando.AMICIZIA, Comando.INCANTESIMO, Comando.POZIONE_SALUTE_GRANDE,
				Comando.NEGOZIO_ARMAIOLO, Comando.ACCAMPAMENTO);
		assertTrue(s.containsAll(EnumSet.of(Situazione.AVVERSARIO_CORROMPIBILE, Situazione.AVVERSARIO_AMICHEVOLE, Situazione.INCANTESIMO_LANCIABILE,
				Situazione.FERITO_CON_UNA_POZIONE, Situazione.INGRESSO_IN_BOTTEGA, Situazione.NOTTE)));
	}

	@Test
	void laMissioneEIlDitoVengonoDaFuori() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		Set<Situazione> s = ui.correnti(Collections.<Comando>emptyList(), true, true);
		assertTrue(s.contains(Situazione.MISSIONE_ATTIVA));
		assertTrue(s.contains(Situazione.DITO_SULLO_SCHERMO));
	}

	@Test
	void uscendoDalGiocoSiRicomincia() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaFuoriDalGioco();
		assertTrue(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).isEmpty());
		ui.segnaSchermataDiGioco();
		assertTrue(correnti(ui).contains(Situazione.PRIMA_SCHERMATA_DI_GIOCO));
	}
}
