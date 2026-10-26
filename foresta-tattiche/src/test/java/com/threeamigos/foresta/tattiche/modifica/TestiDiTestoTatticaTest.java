package com.threeamigos.foresta.tattiche.modifica;

import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ogni valore degli enumerati che la schermata delle tattiche spiega ha il suo testo in <code>testo_tattica_it.properties</code>:
 * una chiave che manca si vedrebbe nella schermata come la chiave stessa.
 */
class TestiDiTestoTatticaTest {

	private static <T extends Enum<T>> void tuttiHannoUnTesto(T[] valori, Function<T, String> testo, List<String> mancanti) {
		for (T valore : valori) {
			String frase = testo.apply(valore);
			if (frase.isEmpty() || frase.startsWith("spiega.") || frase.startsWith("confronto.") || frase.startsWith("criterio.")) {
				mancanti.add(valore.getDeclaringClass().getSimpleName() + "." + valore.name());
			}
		}
	}

	@Test
	void ogniValoreSpiegatoHaIlSuoTesto() {
		List<String> mancanti = new ArrayList<>();
		tuttiHannoUnTesto(TipoConfronto.values(), TestoTattica::confronto, mancanti);
		tuttiHannoUnTesto(TipoConfronto.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(CriterioIncantesimo.values(), TestoTattica::criterio, mancanti);
		tuttiHannoUnTesto(CriterioIncantesimo.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(TipoRosa.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(TipoFiltro.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(TipoPredicato.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(TipoOrdinamento.values(), TestoTattica::spiega, mancanti);
		tuttiHannoUnTesto(TipoAzione.values(), TestoTattica::spiega, mancanti);
		assertTrue(mancanti.isEmpty(), "Testi mancanti in testo_tattica_it.properties: " + mancanti);
	}

	@Test
	void unaChiaveCheMancaSiVedeComeLaChiaveStessa() {
		assertEquals("chiave.che.non.esiste", TestoTattica.testo("chiave.che.non.esiste"));
	}

	@Test
	void iValoriSiInserisconoAlPostoDeiSegnaposto() {
		assertEquals("Tattica 3", BozzaTattica.nomePredefinito(2));
		assertEquals("nel 40% dei casi", TestoTattica.testo("predicato.PROBABILITA", 40));
	}

	@Test
	void unaLinguaSenzaIlFileRipiegaSuiTestiItaliani() {
		try {
			Testi.impostaLingua("xx");
			assertEquals("a caso", TestoTattica.criterio(CriterioIncantesimo.A_CASO));
		} finally {
			Testi.impostaLingua(null);
		}
	}
}
