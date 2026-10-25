package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I testi dei motivi di rifiuto dell'equipaggiamento, letti da rifiuti_equipaggiamento_it.properties.
 */
class TipoMotivoRifiutoEquipaggiamentoTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ogniMotivoHaLaSpiegazioneEIlFumetto() {
		for (TipoMotivoRifiutoEquipaggiamento motivo : TipoMotivoRifiutoEquipaggiamento.values()) {
			assertTrue(Testi.get("rifiuti_equipaggiamento", motivo.name() + ".SPIEGAZIONE").filter(t -> !t.trim().isEmpty()).isPresent(),
					"manca la spiegazione di " + motivo);
			assertTrue(Testi.get("rifiuti_equipaggiamento", motivo.name() + ".FUMETTO").filter(t -> !t.trim().isEmpty()).isPresent(),
					"manca il fumetto di " + motivo);
		}
	}

	@Test
	void iTestiSiComponganoComePrima() {
		assertEquals("Pippo non ha uno slot libero per usare la spada.",
				TipoMotivoRifiutoEquipaggiamento.SLOT_OCCUPATO.formattaSpiegazione("Pippo", "la spada"));
		assertEquals("Pippo è già al massimo del carico che può portare.",
				TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO.formattaSpiegazione("Pippo"));
		assertEquals("Sono già al massimo del carico che posso portare.",
				TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO.formattaFumetto());
		assertEquals("la lancia non è un'arma da mano secondaria.",
				TipoMotivoRifiutoEquipaggiamento.ARMA_NON_DA_MANO_SECONDARIA.formattaFumetto("la lancia"));
		assertEquals("Non uso questo tipo di oggetti.", TipoMotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE.formattaFumetto());
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		Testi.impostaLingua("xx");

		assertEquals("Non combatto con due armi.", TipoMotivoRifiutoEquipaggiamento.SECONDA_ARMA_NON_CONSENTITA.formattaFumetto());
	}

	@Test
	void ilFileNonHaVociPerMotiviCheNonEsistono() throws IOException {
		Properties testi = new Properties();
		try (InputStream flusso = Testi.class.getResourceAsStream("rifiuti_equipaggiamento_it.properties")) {
			testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
		}
		Set<String> motivi = new HashSet<>();
		for (TipoMotivoRifiutoEquipaggiamento motivo : TipoMotivoRifiutoEquipaggiamento.values()) {
			motivi.add(motivo.name());
		}
		for (String chiave : testi.stringPropertyNames()) {
			int punto = chiave.indexOf('.');
			assertTrue(punto > 0 && motivi.contains(chiave.substring(0, punto)), "motivo sconosciuto nella chiave " + chiave);
			String voce = chiave.substring(punto + 1);
			assertTrue(voce.equals("SPIEGAZIONE") || voce.equals("FUMETTO"), "voce sconosciuta nella chiave " + chiave);
		}
		assertEquals(motivi.size() * 2, testi.size(), "due voci per ogni motivo");
	}
}
