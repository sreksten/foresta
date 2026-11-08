package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TutorDelGiocoTest {

	private static final Predicate<TipoSuggerimento> SI = suggerimento -> true;
	private static final Predicate<TipoSuggerimento> NO = suggerimento -> false;

	@BeforeEach
	@AfterEach
	void ripristina() {
		RegistroSuggerimenti.ripristina();
		RegistroSuggerimenti.setMostrabile(false);
	}

	private static TutorDelGioco tutor() {
		TutorDelGioco tutor = new TutorDelGioco();
		tutor.setAbilitato(true);
		return tutor;
	}

	@Test
	void unSuggerimentoLasciatoAMetaRiprendeDallaPaginaACuiEraArrivato() {
		TutorDelGioco tutor = tutor();
		EnumSet<Situazione> situazioni = EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI);
		FumettoDelTutorial primo = tutor.aggiorna(situazioni, SI).orElseThrow(AssertionError::new);
		assertEquals(TipoSuggerimento.MOVIMENTO_E_MAPPA, primo.getSuggerimento());
		assertFalse(tutor.pollice(), "va alla seconda pagina");
		assertEquals(2, primo.getPagina());
		// Si apre la mappa o l'inventario: il fumetto si chiude senza essere letto; tornando riparte da dove era
		tutor.chiudi();
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.MOVIMENTO_E_MAPPA));
		FumettoDelTutorial secondo = tutor.aggiorna(situazioni, SI).orElseThrow(AssertionError::new);
		assertEquals(2, secondo.getPagina());
		// Letto fino in fondo non si ricorda più niente
		assertFalse(tutor.pollice());
		assertTrue(tutor.pollice());
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.MOVIMENTO_E_MAPPA));
	}

	@Test
	void lasciandoLaSchermataSullUltimaPaginaIlSuggerimentoEraLetto() {
		TutorDelGioco tutor = tutor();
		EnumSet<Situazione> situazioni = EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI);
		tutor.aggiorna(situazioni, SI);
		tutor.pollice();
		tutor.pollice();
		assertTrue(tutor.getFumetto().get().isUltimaPagina());
		// L'ultima pagina manda ad aprire la mappa: chi ci va l'ha capita, e tornando il suggerimento non si riapre
		tutor.chiudi();
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.MOVIMENTO_E_MAPPA));
		assertFalse(tutor.aggiorna(situazioni, SI).filter(f -> f.getSuggerimento() == TipoSuggerimento.MOVIMENTO_E_MAPPA).isPresent());
	}

	@Test
	void unMessaggioDelMotoreSiApreSubitoConIlSuoLuogoQualunqueCosaSiaGiaVista() {
		TutorDelGioco tutor = tutor();
		RegistroSuggerimenti.visto(TipoSuggerimento.NON_ANCORA_IL_MOMENTO);
		FumettoDelTutorial messaggio = tutor.mostra(TipoSuggerimento.NON_ANCORA_IL_MOMENTO, java.util.Collections.singletonList("un tempio"))
				.orElseThrow(AssertionError::new);
		assertTrue(messaggio.getTesto().contains("un tempio"), messaggio.getTesto());
		assertFalse(messaggio.getTesto().contains("{0}"));
		assertTrue(messaggio.isUltimaPagina(), "una pagina sola: il pollice lo chiude");
		assertTrue(tutor.pollice());
		assertFalse(tutor.isAperto());
		// A tutorial spento non dice niente
		tutor.annulla();
		assertFalse(tutor.mostra(TipoSuggerimento.NON_ANCORA_IL_MOMENTO, java.util.Collections.singletonList("una grotta")).isPresent());
	}

	@Test
	void ogniPaginaDeiLuoghiParlaDiUnSoloLuogoConIlSuoTitoloELaSuaTessera() {
		TutorDelGioco tutor = tutor();
		FumettoDelTutorial fumetto = tutor.aggiorna(EnumSet.of(Situazione.ICONA_DELLA_MAPPA_PRESENTE), SI).orElseThrow(AssertionError::new);
		assertEquals(TipoSuggerimento.LUOGHI, fumetto.getSuggerimento());
		java.util.Set<String> risorse = new java.util.HashSet<>();
		do {
			assertTrue(fumetto.getTitolo().isPresent(), "pagina " + fumetto.getPagina() + ": manca il titolo");
			assertEquals(1, fumetto.getRisorseDelleIcone().size(), "pagina " + fumetto.getPagina() + ": manca la tessera");
			assertTrue(risorse.add(fumetto.getRisorseDelleIcone().get(0)), "ogni pagina ha la sua tessera: " + fumetto.getRisorseDelleIcone().get(0));
		} while (!tutor.pollice() && fumetto.getPagina() > 0 && tutor.isAperto());
	}

	@Test
	void cambiandoPartitaSiRipartDaCapo() {
		TutorDelGioco tutor = tutor();
		EnumSet<Situazione> situazioni = EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI);
		tutor.aggiorna(situazioni, SI);
		tutor.pollice();
		tutor.chiudiEDimentica();
		assertEquals(1, tutor.aggiorna(situazioni, SI).orElseThrow(AssertionError::new).getPagina());
	}

	@Test
	void senzaIlToolkitCheLoSaDisegnareNonSiApreNiente() {
		TutorDelGioco tutor = new TutorDelGioco();
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI).isPresent());
		tutor.setAbilitato(true);
		assertTrue(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI).isPresent());
		tutor.setAbilitato(false);
		assertFalse(tutor.isAperto());
	}

	@Test
	void apreIlSuggerimentoDellaSituazioneEIlPoliceLoPortaAvantiPoiLoChiude() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.getFumetto().isPresent());
		Optional<FumettoDelTutorial> aperto = tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI);
		assertTrue(aperto.isPresent());
		assertEquals(TipoSuggerimento.PRIMI_PASSI, aperto.get().getSuggerimento());
		assertTrue(tutor.isAperto());
		// PRIMI_PASSI ha due pagine: il primo pollice passa alla seconda, il secondo chiude e segna visto
		assertEquals(1, aperto.get().getPagina());
		assertFalse(tutor.pollice());
		assertEquals(2, aperto.get().getPagina());
		assertTrue(aperto.get().isUltimaPagina());
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
		assertTrue(tutor.pollice());
		assertFalse(tutor.isAperto());
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI).isPresent());
	}

	@Test
	void mentreUnFumettoEApertoNonNeApreUnAltro() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), SI);
		assertFalse(tutor.aggiorna(EnumSet.allOf(Situazione.class), SI).isPresent());
		assertEquals(TipoSuggerimento.PRIMI_PASSI, tutor.getFumetto().get().getSuggerimento());
		// Chiuso il primo, il successivo, nell'ordine fisso
		while (!tutor.pollice()) {
			// avanti fino all'ultima pagina
		}
		assertEquals(TipoSuggerimento.BARRA_DELLE_ICONE,
				tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), SI).get().getSuggerimento());
	}

	@Test
	void seIlMomentoNonLoConsenteNonApreNiente() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), NO).isPresent());
		assertFalse(tutor.isAperto());
	}

	@Test
	void l_annullaSpegneTuttoIlTutorial() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI);
		tutor.annulla();
		assertFalse(tutor.isAperto());
		assertFalse(RegistroSuggerimenti.isAttivo());
		assertFalse(tutor.aggiorna(EnumSet.allOf(Situazione.class), SI).isPresent());
		// Spegnere non segna come visto: ripristinando si rivedono
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
	}

	@Test
	void chiudereSenzaSegnareLoFaRiproporre() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), SI);
		tutor.chiudi();
		assertFalse(tutor.isAperto());
		assertTrue(tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), SI).isPresent());
	}

	@Test
	void ilTestoSegueIlDito() {
		TutorDelGioco tutor = tutor();
		tutor.setDito(true);
		String conIlDito = tutor.aggiorna(EnumSet.of(Situazione.MISSIONE_ATTIVA), SI).get().getTesto();
		tutor.chiudi();
		tutor.setDito(false);
		String conIlMouse = tutor.aggiorna(EnumSet.of(Situazione.MISSIONE_ATTIVA), SI).get().getTesto();
		assertNotEquals(conIlDito, conIlMouse);
	}

	@Test
	void ilBordoLampeggiaSoloMentreIlFumettoEAperto() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.isBordoAcceso(0));
		tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), SI);
		assertTrue(tutor.isBordoAcceso(0));
		assertTrue(tutor.isBordoAcceso(TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS - 1));
		assertFalse(tutor.isBordoAcceso(TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS));
		assertTrue(tutor.isBordoAcceso(2 * TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS));
		assertTrue(tutor.isBordoAcceso(-5));
	}

	@Test
	void sceglieIlPrimoSuggerimentoCheSiPuoAprireAdessoGliAltriAspettano() {
		TutorDelGioco tutor = tutor();
		EnumSet<Situazione> insieme = EnumSet.of(Situazione.OGGETTO_TROVATO, Situazione.COMBATTIMENTO_APERTO);
		// L'oggetto trovato punta all'icona dell'inventario, che non c'è ancora: si apre il combattimento
		Optional<FumettoDelTutorial> aperto = tutor.aggiorna(insieme, s -> s != TipoSuggerimento.OGGETTO_TROVATO);
		assertEquals(TipoSuggerimento.COMBATTIMENTO, aperto.get().getSuggerimento());
		while (!tutor.pollice()) {
			// avanti fino all'ultima pagina
		}
		// Finché l'icona non c'è, l'oggetto trovato aspetta (non si apre un fumetto che punta a una barra vuota)
		assertFalse(tutor.aggiorna(insieme, s -> s != TipoSuggerimento.OGGETTO_TROVATO).isPresent());
		// Appena compare, si apre
		assertEquals(TipoSuggerimento.OGGETTO_TROVATO, tutor.aggiorna(insieme, SI).get().getSuggerimento());
	}

	@Test
	void laSceltaDellaClasseVieneDaSolaPrimaDiTutto() {
		TutorDelGioco tutor = tutor();
		assertEquals(TipoSuggerimento.SCELTA_DELLA_CLASSE,
				tutor.aggiorna(EnumSet.of(Situazione.SCELTA_DELLA_CLASSE, Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI).get().getSuggerimento());
	}
}
