package com.threeamigos.foresta.serializzazione;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il salvataggio è a righe con i campi separati da "|": i testi liberi non devono poter spezzare la riga né spostare i
 * campi.
 */
class CampiNeiSalvataggiTest {

	@Test
	void pulisciCampoTogliePipeETrasformaGliACapoInSpazi() {
		assertEquals("Pippo", Serializzabile.pulisciCampo("Pip|po"));
		assertEquals("uno due", Serializzabile.pulisciCampo("uno\ndue"));
		assertEquals("uno due", Serializzabile.pulisciCampo("uno\r\ndue"));
		assertEquals("uno due tre", Serializzabile.pulisciCampo("uno\rdue\ntre"));
		assertNull(Serializzabile.pulisciCampo(null));
	}

	@Test
	void senzaACapoLasciaIlPipe() {
		assertEquals("a|b c", Serializzabile.senzaACapo("a|b\nc"));
	}

	@Test
	void unaChiaveDiProprietaConACapoNonSiSalva() {
		assertThrows(IllegalStateException.class,
				() -> MappaProprieta.salva(java.util.Collections.singletonMap("CHIAVE\nSTORTA", "valore")));
	}
}
