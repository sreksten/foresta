package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi dei suggerimenti: ogni suggerimento ha da una a tre pagine, in italiano e in inglese, e nessuna pagina vuota.
 */
class TipoSuggerimentoTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ognunoHaDaUnaATrePagineInOgniLingua() {
		for (String lingua : new String[] {null, "en", "en-GB"}) {
			Testi.impostaLingua(lingua);
			for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
				int pagine = suggerimento.getNumeroPagine();
				assertTrue(pagine >= 1 && pagine <= TipoSuggerimento.PAGINE_MASSIME, suggerimento + " in " + lingua + ": " + pagine);
				for (int pagina = 1; pagina <= pagine; pagina++) {
					assertFalse(suggerimento.getPagina(pagina, false).trim().isEmpty(), suggerimento + " pagina " + pagina);
					assertNotEquals(suggerimento.name(), suggerimento.getPagina(pagina, false), "manca " + suggerimento + " pagina " + pagina);
				}
			}
		}
	}

	@Test
	void nonCiSonoPagineOltreIlMassimo() {
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			assertFalse(Testi.testo(suggerimento, "PAGINA_" + (TipoSuggerimento.PAGINE_MASSIME + 1)).isPresent(), suggerimento.name());
		}
	}

	@Test
	void laVarianteDelDitoSostituisceLaPaginaESeMancaValeLaNormale() {
		assertNotEquals(TipoSuggerimento.MISSIONI.getPagina(1, false), TipoSuggerimento.MISSIONI.getPagina(1, true));
		assertEquals(TipoSuggerimento.COMBATTIMENTO.getPagina(1, false), TipoSuggerimento.COMBATTIMENTO.getPagina(1, true));
	}

	@Test
	void ogniSuggerimentoHaUnInnescoEUnBersaglio() {
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			assertNotNull(suggerimento.getInnesco(), suggerimento.name());
			assertNotNull(suggerimento.getBersaglio(), suggerimento.name());
		}
	}

	@Test
	void glInnescoSonoTuttiDiversiPerCheUnaSituazioneNonNeFacciaScattareDue() {
		java.util.Set<Situazione> viste = java.util.EnumSet.noneOf(Situazione.class);
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			assertTrue(viste.add(suggerimento.getInnesco()), "innesco doppio: " + suggerimento.getInnesco());
		}
	}
}
