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
				int massime = suggerimento == TipoSuggerimento.LUOGHI ? TipoSuggerimento.PAGINE_MASSIME : TipoSuggerimento.PAGINE_NORMALI;
				assertTrue(pagine >= 1 && pagine <= massime, suggerimento + " in " + lingua + ": " + pagine);
				for (int pagina = 1; pagina <= pagine; pagina++) {
					assertFalse(suggerimento.getPagina(pagina, false).trim().isEmpty(), suggerimento + " pagina " + pagina);
					assertNotEquals(suggerimento.name(), suggerimento.getPagina(pagina, false), "manca " + suggerimento + " pagina " + pagina);
				}
			}
		}
	}

	@Test
	void soloLElencoDeiLuoghiHaPiuDiTrePagine() {
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			if (suggerimento != TipoSuggerimento.LUOGHI) {
				assertTrue(suggerimento.getNumeroPagine() <= TipoSuggerimento.PAGINE_NORMALI, suggerimento.name());
			}
		}
		assertTrue(TipoSuggerimento.LUOGHI.getNumeroPagine() > TipoSuggerimento.PAGINE_NORMALI);
	}

	@Test
	void nonCiSonoPagineOltreIlMassimo() {
		for (TipoSuggerimento suggerimento : TipoSuggerimento.values()) {
			assertFalse(Testi.testo(suggerimento, "PAGINA_" + (TipoSuggerimento.PAGINE_MASSIME + 1)).isPresent(), suggerimento.name());
		}
	}

	@Test
	void ognuniPaginaPuntaAQuelloDiCuiParla() {
		// La pagina sulla mappa punta alla mappa, non alle frecce; quella sull'aiuto al punto interrogativo
		assertEquals(BersaglioDelSuggerimento.ICONE_DI_MOVIMENTO, TipoSuggerimento.MOVIMENTO_E_MAPPA.getBersaglio(1));
		assertEquals(BersaglioDelSuggerimento.ICONA_DELLA_MAPPA, TipoSuggerimento.MOVIMENTO_E_MAPPA.getBersaglio(2));
		assertEquals(BersaglioDelSuggerimento.BARRA_DELLE_ICONE, TipoSuggerimento.BARRA_DELLE_ICONE.getBersaglio(1));
		assertEquals(BersaglioDelSuggerimento.ICONA_DELL_AIUTO, TipoSuggerimento.BARRA_DELLE_ICONE.getBersaglio(2));
		// Le pagine oltre l'ultimo bersaglio ripetono l'ultimo; senza bersagli per pagina vale sempre lo stesso
		assertEquals(BersaglioDelSuggerimento.ICONA_DELLA_MAPPA, TipoSuggerimento.MOVIMENTO_E_MAPPA.getBersaglio(5));
		assertEquals(BersaglioDelSuggerimento.ICONA_DELLA_MAPPA, TipoSuggerimento.LUOGHI.getBersaglio(4));
		assertEquals(TipoSuggerimento.COMBATTIMENTO.getBersaglio(), TipoSuggerimento.COMBATTIMENTO.getBersaglio(2));
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
