package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.interfacce.GestoreSuggerimenti;
import com.threeamigos.foresta.modellodati.SuggerimentiMD;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistroSuggerimentiTest {

	@BeforeEach
	@AfterEach
	void ripristina() {
		RegistroSuggerimenti.ripristina();
	}

	@Test
	void ilTutorialPartePerDefaultAccesoESenzaSuggerimentiVisti() {
		assertTrue(RegistroSuggerimenti.isAttivo());
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			assertFalse(RegistroSuggerimenti.isVisto(suggerimento), suggerimento.name());
		}
	}

	@Test
	void siAccendeESiSpegne() {
		RegistroSuggerimenti.setAttivo(false);
		assertFalse(RegistroSuggerimenti.isAttivo());
		RegistroSuggerimenti.setAttivo(true);
		assertTrue(RegistroSuggerimenti.isAttivo());
	}

	@Test
	void unaSituazioneFaScattareIlSuggerimentoCheLaInnescaUnaVoltaSola() {
		Optional<TipoSuggerimento> primo = RegistroSuggerimenti.situazione(EnumSet.of(Situazione.COMBATTIMENTO_APERTO));
		assertEquals(Optional.of(TipoSuggerimento.COMBATTIMENTO), primo);
		RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.COMBATTIMENTO));
		assertFalse(RegistroSuggerimenti.situazione(EnumSet.of(Situazione.COMBATTIMENTO_APERTO)).isPresent());
	}

	@Test
	void conPiuSituazioniSiDaUnSuggerimentoAllaVoltaNellOrdineFisso() {
		EnumSet<Situazione> insieme = EnumSet.of(Situazione.NOTTE, Situazione.PRIMA_SCHERMATA_DI_GIOCO, Situazione.BARRA_DELLE_ICONE_VISIBILE);
		assertEquals(Optional.of(TipoSuggerimento.PRIMI_PASSI), RegistroSuggerimenti.situazione(insieme));
		RegistroSuggerimenti.visto(TipoSuggerimento.PRIMI_PASSI);
		assertEquals(Optional.of(TipoSuggerimento.BARRA_DELLE_ICONE), RegistroSuggerimenti.situazione(insieme));
		RegistroSuggerimenti.visto(TipoSuggerimento.BARRA_DELLE_ICONE);
		assertEquals(Optional.of(TipoSuggerimento.ACCAMPAMENTO), RegistroSuggerimenti.situazione(insieme));
		RegistroSuggerimenti.visto(TipoSuggerimento.ACCAMPAMENTO);
		assertFalse(RegistroSuggerimenti.situazione(insieme).isPresent());
	}

	@Test
	void senzaSituazioniOASpentoNonScattaNiente() {
		assertFalse(RegistroSuggerimenti.situazione(EnumSet.noneOf(Situazione.class)).isPresent());
		RegistroSuggerimenti.setAttivo(false);
		assertFalse(RegistroSuggerimenti.situazione(EnumSet.allOf(Situazione.class)).isPresent());
	}

	@Test
	void ripristinareRiaccendeEFaRivedereTutto() {
		RegistroSuggerimenti.visto(TipoSuggerimento.MISSIONI);
		RegistroSuggerimenti.setAttivo(false);
		RegistroSuggerimenti.ripristina();
		assertTrue(RegistroSuggerimenti.isAttivo());
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.MISSIONI));
	}

	/** Salva in una stringa, come farebbe un gestore su file. */
	private static final class GestoreInMemoria implements GestoreSuggerimenti {
		String contenuto;
		int salvataggi;

		@Override
		public boolean carica(SuggerimentiMD suggerimentiMD) {
			if (contenuto == null) {
				return false;
			}
			try {
				suggerimentiMD.leggi(new BufferedReader(new StringReader(contenuto)));
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		public boolean salva(SuggerimentiMD suggerimentiMD) {
			try {
				StringWriter scritto = new StringWriter();
				PrintWriter stream = new PrintWriter(scritto);
				suggerimentiMD.salva(stream);
				stream.flush();
				contenuto = scritto.toString();
				salvataggi++;
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Test
	void conUnGestoreSiSalvaAOgniNovitaESiRilegge() {
		GestoreInMemoria gestore = new GestoreInMemoria();
		try {
			RegistroSuggerimenti.impostaGestore(gestore);
			RegistroSuggerimenti.visto(TipoSuggerimento.OGGETTO_TROVATO);
			int salvataggi = gestore.salvataggi;
			// Rivedere lo stesso suggerimento non è una novità
			RegistroSuggerimenti.visto(TipoSuggerimento.OGGETTO_TROVATO);
			assertEquals(salvataggi, gestore.salvataggi);
			RegistroSuggerimenti.setAttivo(false);

			// Come riavviare il gioco
			RegistroSuggerimenti.impostaGestore(gestore);
			assertFalse(RegistroSuggerimenti.isAttivo());
			assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.OGGETTO_TROVATO));
			assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.POZIONI));
		} finally {
			// Un gestore non si toglie: se ne mette uno vuoto, perché i test seguenti non salvino da nessuna parte
			RegistroSuggerimenti.impostaGestore(new GestoreInMemoria());
			RegistroSuggerimenti.ripristina();
		}
	}
}
