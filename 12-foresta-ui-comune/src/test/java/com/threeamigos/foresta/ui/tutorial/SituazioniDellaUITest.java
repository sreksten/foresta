package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SituazioniDellaUITest {

	private static Set<Situazione> correnti(SituazioniDellaUI ui, Comando... comandi) {
		return ui.correnti(Arrays.asList(comandi), false, false, false);
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
		assertEquals(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), correnti(ui, Comando.INVENTARIO));
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
		assertFalse(correnti(ui, Comando.INVENTARIO).contains(Situazione.TATTICHE_DISPONIBILI));
		ui.segnaCombattimentoAperto();
		// Lo stesso scontro che si riapre non è un altro scontro
		ui.segnaCombattimentoAperto();
		assertEquals(2, ui.getScontriVisti());
		// Non serve l'icona delle tattiche, che sta dentro l'inventario: basta aver visto i due scontri
		assertTrue(correnti(ui, Comando.INVENTARIO).contains(Situazione.TATTICHE_DISPONIBILI));
		assertTrue(correnti(ui).contains(Situazione.TATTICHE_DISPONIBILI));
	}

	@Test
	void l_iconaDellaMappaSiLeggeDallaBarra() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaMovimentoSpiegato(true);
		assertFalse(correnti(ui, Comando.NORD).contains(Situazione.ICONA_DELLA_MAPPA_PRESENTE));
		assertTrue(correnti(ui, Comando.MAPPA).contains(Situazione.ICONA_DELLA_MAPPA_PRESENTE));
	}

	@Test
	void ILuoghiAspettanoCheSiSiaSpiegatoIlMovimento() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.MAPPA).contains(Situazione.ICONA_DELLA_MAPPA_PRESENTE));
	}

	@Test
	void ilMovimentoAspettaL_oggettoTrovatoSeCeStato() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		Comando[] frecce = {Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST};
		assertTrue(correnti(ui, frecce).contains(Situazione.ICONE_DI_MOVIMENTO_PRESENTI), "senza oggetti non c'è niente da aspettare");
		ui.segnaOggettoTrovato();
		assertFalse(correnti(ui, frecce).contains(Situazione.ICONE_DI_MOVIMENTO_PRESENTI));
		ui.segnaOggettoSpiegato(true);
		assertTrue(correnti(ui, frecce).contains(Situazione.ICONE_DI_MOVIMENTO_PRESENTI));
	}

	@Test
	void l_iconaDelCombattimentoAutomaticoSiLeggeDallaBarra() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.SINGOLO_ATTACCO).contains(Situazione.COMBATTIMENTO_AUTOMATICO_DISPONIBILE));
		assertTrue(correnti(ui, Comando.SINGOLO_ATTACCO, Comando.COMBATTIMENTO).contains(Situazione.COMBATTIMENTO_AUTOMATICO_DISPONIBILE));
	}

	@Test
	void ilJoystickEMappaGrandeSiDiconoSoloSullaMappa() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		// Sulla schermata di gioco la mappa non c'è: non si parla né del joystick né di trascinare
		ui.segnaLaSchermata(StatoDelloSchermo.IN_GIOCO, true, true);
		Set<Situazione> inGioco = correnti(ui, Comando.MAPPA);
		assertFalse(inGioco.contains(Situazione.JOYSTICK_SULLA_MAPPA));
		assertFalse(inGioco.contains(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO));
		ui.segnaLaSchermata(StatoDelloSchermo.MAPPA, true, true);
		Set<Situazione> sullaMappa = correnti(ui);
		assertTrue(sullaMappa.contains(Situazione.JOYSTICK_SULLA_MAPPA));
		assertTrue(sullaMappa.contains(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO));
		ui.segnaLaSchermata(StatoDelloSchermo.MAPPA, false, false);
		Set<Situazione> senza = correnti(ui);
		assertFalse(senza.contains(Situazione.JOYSTICK_SULLA_MAPPA));
		assertFalse(senza.contains(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO));
	}

	@Test
	void l_inventarioAperto() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui).contains(Situazione.INVENTARIO_APERTO));
		ui.segnaLaSchermata(StatoDelloSchermo.INVENTARIO, false, false);
		assertTrue(correnti(ui).contains(Situazione.INVENTARIO_APERTO));
	}

	@Test
	void iPuntiAbilitaSiDicononSoloNellInventario() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaPuntiAbilita(true);
		assertFalse(correnti(ui).contains(Situazione.PUNTI_ABILITA_DA_SPENDERE), "sulla schermata di gioco non si dice come si spendono");
		assertFalse(correnti(ui).contains(Situazione.ATTRIBUTI_DA_INTRODURRE));
		assertTrue(correnti(ui).contains(Situazione.PUNTI_ABILITA_DISPONIBILI), "sulla schermata di gioco si dice che il livello è salito");
		ui.segnaLaSchermata(StatoDelloSchermo.INVENTARIO, false, false);
		assertTrue(correnti(ui).contains(Situazione.PUNTI_ABILITA_DA_SPENDERE));
		assertTrue(correnti(ui).contains(Situazione.ATTRIBUTI_DA_INTRODURRE), "prima gli attributi");
		assertFalse(correnti(ui).contains(Situazione.PUNTI_ABILITA_DISPONIBILI), "nell'inventario non si dice più che il livello è salito");
		ui.segnaPuntiAbilita(false);
		assertFalse(correnti(ui).contains(Situazione.PUNTI_ABILITA_DA_SPENDERE));
		assertFalse(correnti(ui).contains(Situazione.ATTRIBUTI_DA_INTRODURRE));
	}

	@Test
	void unEffettoDiStatoEUnaReazioneSiDiconoPerLaLocazioneInCorso() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui).contains(Situazione.EFFETTO_DI_STATO_APPLICATO));
		ui.segnaEffettoDiStatoApplicato();
		ui.segnaReazioneScatenata();
		Set<Situazione> s = correnti(ui);
		assertTrue(s.contains(Situazione.EFFETTO_DI_STATO_APPLICATO));
		assertTrue(s.contains(Situazione.REAZIONE_ELEMENTALE_SCATENATA));
		ui.segnaNuovaLocazione();
		assertFalse(correnti(ui).contains(Situazione.EFFETTO_DI_STATO_APPLICATO));
		assertFalse(correnti(ui).contains(Situazione.REAZIONE_ELEMENTALE_SCATENATA));
	}

	@Test
	void dopoIlMovimentoSiDiceDiAndareVersoIBoschiSoloConLeFreccePresenti() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).contains(Situazione.MOVIMENTO_SPIEGATO));
		ui.segnaMovimentoSpiegato(true);
		assertTrue(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).contains(Situazione.MOVIMENTO_SPIEGATO));
		assertFalse(correnti(ui, Comando.NORD).contains(Situazione.MOVIMENTO_SPIEGATO), "senza tutte le frecce non c'è dove andare");
	}

	@Test
	void unCadutoSiDiceSoloAFineCombattimento() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaQualcunoCaduto(true);
		ui.segnaCombattimentoAperto();
		assertFalse(correnti(ui).contains(Situazione.PERSONAGGIO_CADUTO), "durante lo scontro no");
		ui.segnaCombattimentoChiuso();
		assertTrue(correnti(ui).contains(Situazione.PERSONAGGIO_CADUTO));
		ui.segnaQualcunoCaduto(false);
		assertFalse(correnti(ui).contains(Situazione.PERSONAGGIO_CADUTO));
	}

	@Test
	void leIconeDiMostriPozioniBottegheEAccampamento() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		Set<Situazione> s = correnti(ui, Comando.CORRUZIONE, Comando.AMICIZIA, Comando.INCANTESIMO, Comando.NEGOZIO_ARMAIOLO, Comando.ACCAMPAMENTO);
		assertTrue(s.containsAll(EnumSet.of(Situazione.AVVERSARIO_CORROMPIBILE, Situazione.AVVERSARIO_AMICHEVOLE, Situazione.INCANTESIMO_LANCIABILE,
				Situazione.INGRESSO_IN_BOTTEGA, Situazione.NOTTE)));
	}

	@Test
	void ilFeritoConUnaPozioneNonSiLeggeDalleIconeDelleBevande() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		// In combattimento le icone delle pozioni ci sono anche a chi sta bene: non bastano
		assertFalse(correnti(ui, Comando.POZIONE_SALUTE, Comando.POZIONE_SALUTE_GRANDE).contains(Situazione.FERITO_CON_UNA_POZIONE));
		// Lo dice chi guarda il gruppo
		assertTrue(ui.correnti(Collections.<Comando>emptyList(), false, false, true).contains(Situazione.FERITO_CON_UNA_POZIONE));
	}

	@Test
	void laMissioneEIlDitoVengonoDaFuori() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		Set<Situazione> s = ui.correnti(Collections.<Comando>emptyList(), true, true, false);
		assertTrue(s.contains(Situazione.MISSIONE_ATTIVA));
		assertTrue(s.contains(Situazione.DITO_SULLO_SCHERMO));
	}

	@Test
	void laLocazioneAffollataLaDiceChiContaIPersonaggiESoloInGioco() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaLocazioneAffollata(true);
		assertFalse(ui.correnti(Collections.<Comando>emptyList(), false, false, false).contains(Situazione.LOCAZIONE_AFFOLLATA),
				"fuori dalla schermata di gioco no");
		ui.segnaSchermataDiGioco();
		assertTrue(ui.correnti(Collections.<Comando>emptyList(), false, false, false).contains(Situazione.LOCAZIONE_AFFOLLATA));
		ui.segnaLocazioneAffollata(false);
		assertFalse(ui.correnti(Collections.<Comando>emptyList(), false, false, false).contains(Situazione.LOCAZIONE_AFFOLLATA));
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

	@Test
	void laSceltaDellaClasseSiVedeSubitoEFinisceConLaSchermataDiGioco() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		// Prima della schermata di gioco: l'unica situazione è la scelta
		ui.segnaSceltaDellaClasse(true);
		assertEquals(EnumSet.of(Situazione.SCELTA_DELLA_CLASSE), correnti(ui, Comando.GUERRIERO, Comando.MAGO));
		ui.segnaSceltaDellaClasse(false);
		assertTrue(correnti(ui, Comando.GUERRIERO, Comando.MAGO).isEmpty());
		// Se si arriva al gioco senza che la fase sia stata chiusa, la scelta è comunque finita
		ui.segnaSceltaDellaClasse(true);
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST).contains(Situazione.SCELTA_DELLA_CLASSE));
	}

	@Test
	void sulGiocoDueOPiuPersonaggiNellaBarraSonoUnaScelta() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		assertFalse(correnti(ui, Comando.PERSONAGGIO_1, Comando.ANNULLA).contains(Situazione.SCELTA_DEL_PERSONAGGIO));
		assertTrue(correnti(ui, Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_3, Comando.ANNULLA).contains(Situazione.SCELTA_DEL_PERSONAGGIO));
		// Nell'inventario le icone dei personaggi sfogliano le pagine
		ui.segnaLaSchermata(StatoDelloSchermo.INVENTARIO, false, false);
		assertFalse(correnti(ui, Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2).contains(Situazione.SCELTA_DEL_PERSONAGGIO));
	}

	@Test
	void ilCompagnoInLocandaVaViaConLaNuovaLocazione() {
		SituazioniDellaUI ui = new SituazioniDellaUI();
		ui.segnaSchermataDiGioco();
		ui.segnaDalMotore(Situazione.COMPAGNO_IN_LOCANDA);
		ui.segnaDalMotore(Situazione.RECUPERO_FRA_LOCAZIONI);
		assertTrue(correnti(ui, Comando.SI, Comando.NO).contains(Situazione.COMPAGNO_IN_LOCANDA));
		ui.segnaNuovaLocazione();
		assertFalse(correnti(ui, Comando.SI, Comando.NO).contains(Situazione.COMPAGNO_IN_LOCANDA));
		assertTrue(correnti(ui).contains(Situazione.RECUPERO_FRA_LOCAZIONI));
	}
}
