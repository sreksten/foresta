package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I produttori di testi con le grammatiche inglesi (sfide_en, templi_en, rovine_en, oroscopo_en): niente marcatori
 * rimasti, niente spazi doppi, niente testi vuoti.
 */
class ProduttoriDiTestiInIngleseTest {

	private static final int GIRI = 300;

	@BeforeEach
	void inglese() {
		Testi.impostaLingua("en");
	}

	@AfterEach
	void italiano() {
		Testi.impostaLingua(null);
	}

	private static void controlla(String nome, Supplier<String> produttore) {
		for (int i = 0; i < GIRI; i++) {
			String testo = produttore.get();
			assertNotNull(testo, nome);
			assertFalse(testo.trim().isEmpty(), nome);
			assertFalse(testo.matches("(?s).*[\\[\\]{}|<>@].*"), nome + ": " + testo);
			assertFalse(testo.contains("  ") || testo.contains(" ,"), nome + ": " + testo);
		}
	}

	@Test
	void sfide() {
		controlla("sfida", ProduttoreDiSfideCasuale::fraseDiSfida);
		controlla("vince", ProduttoreDiSfideCasuale::fraseSeIlGiocatoreVince);
		controlla("perde", ProduttoreDiSfideCasuale::fraseSeIlGiocatorePerde);
		controlla("torneo", ProduttoreDiSfideCasuale::fraseDelTorneo);
		controlla("leggendario", ProduttoreDiSfideCasuale::nomeLeggendarioDeiCampioni);
	}

	@Test
	void fiabe() {
		for (int i = 0; i < 200; i++) {
			String fiaba = String.join(" ", ProduttoreDiFiabeCasuale.fiaba());
			assertFalse(fiaba.trim().isEmpty());
			assertFalse(fiaba.matches("(?s).*[\\[\\]{}|<>@].*"), fiaba);
			assertFalse(fiaba.contains("  ") || fiaba.contains(" ,") || fiaba.contains(" ."), fiaba);
		}
	}

	@Test
	void luoghi() {
		controlla("tempio", ProduttoreDiLuoghiCasuale::nomeTempio);
		controlla("rovine", ProduttoreDiLuoghiCasuale::nomeRovine);
	}

	@Test
	void oroscopi() {
		for (int i = 0; i < GIRI; i++) {
			ProduttoreDiOroscopiCasuale.DialogoSullOroscopo d = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo();
			for (String t : new String[] {d.getOroscopo(), d.getDomanda(), d.getRisposta()}) {
				assertNotNull(t);
				assertFalse(t.trim().isEmpty());
				assertFalse(t.matches("(?s).*[\\[\\]{}|<>@].*"), t);
				assertFalse(t.contains("  "), t);
			}
		}
	}
	@Test
	void locande() {
		ProduttoriDiTesti.resetProduzioni();
		java.util.List<ProduttoreDiLocandeCasuale.DatiLocanda> locande = ProduttoreDiLocandeCasuale.getDatiLocanda(35);
		assertEquals(35, locande.size());
		for (ProduttoreDiLocandeCasuale.DatiLocanda l : locande) {
			for (String t : new String[] {l.getNome(), l.getNomeLocandiere(), l.getDialogo(), l.getRecensione()}) {
				assertFalse(t.trim().isEmpty(), l.getIdentificativo());
				assertFalse(t.matches("(?s).*[\\[\\]{}|<>@].*"), l.getIdentificativo() + ": " + t);
				assertFalse(t.contains("  "), t);
			}
			assertTrue(l.getDialogo().contains(l.getNomeLocandiere()), l.getIdentificativo());
			for (int i = 0; i < 40; i++) {
				String n = ProduttoreDiLocandeCasuale.notiziaGrezza(l.getIdentificativo());
				assertTrue(n.matches("(?s)[A-Z0-9]+-.+"), n);
				assertFalse(n.matches("(?s).*[\\[\\]{}|<>@].*"), n);
			}
		}
		controlla("reazione", ProduttoreDiLocandeCasuale::reazioneLocandiere);
		ProduttoriDiTesti.resetProduzioni();
	}
}
