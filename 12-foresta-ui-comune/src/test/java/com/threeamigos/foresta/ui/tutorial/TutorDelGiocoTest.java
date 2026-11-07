package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TutorDelGiocoTest {

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
	void senzaIlToolkitCheLoSaDisegnareNonSiApreNiente() {
		TutorDelGioco tutor = new TutorDelGioco();
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true).isPresent());
		tutor.setAbilitato(true);
		assertTrue(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true).isPresent());
		tutor.setAbilitato(false);
		assertFalse(tutor.isAperto());
	}

	@Test
	void apreIlSuggerimentoDellaSituazioneEIlPoliceLoPortaAvantiPoiLoChiude() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.getFumetto().isPresent());
		Optional<FumettoDelTutorial> aperto = tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true);
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
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true).isPresent());
	}

	@Test
	void mentreUnFumettoEApertoNonNeApreUnAltro() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), true);
		assertFalse(tutor.aggiorna(EnumSet.allOf(Situazione.class), true).isPresent());
		assertEquals(TipoSuggerimento.PRIMI_PASSI, tutor.getFumetto().get().getSuggerimento());
		// Chiuso il primo, il successivo, nell'ordine fisso
		while (!tutor.pollice()) {
			// avanti fino all'ultima pagina
		}
		assertEquals(TipoSuggerimento.BARRA_DELLE_ICONE,
				tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE), true).get().getSuggerimento());
	}

	@Test
	void seIlMomentoNonLoConsenteNonApreNiente() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), false).isPresent());
		assertFalse(tutor.isAperto());
	}

	@Test
	void l_annullaSpegneTuttoIlTutorial() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true);
		tutor.annulla();
		assertFalse(tutor.isAperto());
		assertFalse(RegistroSuggerimenti.isAttivo());
		assertFalse(tutor.aggiorna(EnumSet.allOf(Situazione.class), true).isPresent());
		// Spegnere non segna come visto: ripristinando si rivedono
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
	}

	@Test
	void chiudereSenzaSegnareLoFaRiproporre() {
		TutorDelGioco tutor = tutor();
		tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), true);
		tutor.chiudi();
		assertFalse(tutor.isAperto());
		assertTrue(tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), true).isPresent());
	}

	@Test
	void ilTestoSegueIlDito() {
		TutorDelGioco tutor = tutor();
		tutor.setDito(true);
		String conIlDito = tutor.aggiorna(EnumSet.of(Situazione.MISSIONE_ATTIVA), true).get().getTesto();
		tutor.chiudi();
		tutor.setDito(false);
		String conIlMouse = tutor.aggiorna(EnumSet.of(Situazione.MISSIONE_ATTIVA), true).get().getTesto();
		assertNotEquals(conIlDito, conIlMouse);
	}

	@Test
	void ilBordoLampeggiaSoloMentreIlFumettoEAperto() {
		TutorDelGioco tutor = tutor();
		assertFalse(tutor.isBordoAcceso(0));
		tutor.aggiorna(EnumSet.of(Situazione.COMBATTIMENTO_APERTO), true);
		assertTrue(tutor.isBordoAcceso(0));
		assertTrue(tutor.isBordoAcceso(TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS - 1));
		assertFalse(tutor.isBordoAcceso(TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS));
		assertTrue(tutor.isBordoAcceso(2 * TutorDelGioco.MEZZO_PERIODO_DEL_LAMPEGGIO_MS));
		assertTrue(tutor.isBordoAcceso(-5));
	}
}
