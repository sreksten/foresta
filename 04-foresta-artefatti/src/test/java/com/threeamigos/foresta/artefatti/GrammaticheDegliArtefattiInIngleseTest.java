package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le grammatiche inglesi degli artefatti e degli ingredienti (artefatti2_en.txt, ingredienti_en.txt): stesse radici e stessi
 * marcatori di quelle italiane, nomi che cominciano con "the".
 */
class GrammaticheDegliArtefattiInIngleseTest {

	private static final int GIRI = 150;
	private static final List<TipoArtefatto> TIPI = Arrays.asList(TipoArtefatto.SPADA, TipoArtefatto.ARMATURA, TipoArtefatto.MASCHERA,
			TipoArtefatto.VESTE, TipoArtefatto.ELMO, TipoArtefatto.SCUDO, TipoArtefatto.SCHINIERI);
	private static final List<TipoArtefatto> INGREDIENTI = Arrays.asList(TipoArtefatto.PERGAMENA, TipoArtefatto.GEMMA, TipoArtefatto.MONILE,
			TipoArtefatto.GINGILLO, TipoArtefatto.SIGILLO);

	@BeforeEach
	void inglese() {
		Testi.impostaLingua("en");
	}

	@AfterEach
	void italiano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ogniArtefattoHaTantiEffettiQuantiNeChiede() {
		GrammaticaArtefatti grammatica = GrammaticaArtefatti.artefatti();
		assertNotNull(grammatica, "artefatti2_en.txt non si carica");
		for (TipoArtefatto tipo : TIPI) {
			assertTrue(grammatica.supporta(tipo), tipo.toString());
			for (int effetti = 0; effetti <= 3; effetti++) {
				for (int i = 0; i < GIRI; i++) {
					GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, effetti);
					String nome = risultato.getNome();
					assertTrue(nome.startsWith("the "), nome);
					assertFalse(nome.matches(".*[@\\[\\]{}|<>\"].*"), nome);
					assertFalse(nome.contains(" ,") || nome.contains("  "), nome);
					assertEquals(effetti, risultato.getEffetti(), tipo + ": " + nome);
				}
			}
		}
	}

	@Test
	void ogniIngredienteHaTantiEffettiQuantiNeChiede() {
		GrammaticaArtefatti grammatica = GrammaticaArtefatti.ingredienti();
		assertNotNull(grammatica, "ingredienti_en.txt non si carica");
		for (TipoArtefatto tipo : INGREDIENTI) {
			assertTrue(grammatica.supporta(tipo), tipo.toString());
			for (int effetti = 1; effetti <= 3; effetti++) {
				for (int i = 0; i < GIRI; i++) {
					GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, effetti);
					assertTrue(risultato.getNome().startsWith("the "), risultato.getNome());
					assertEquals(effetti, risultato.getEffetti(), risultato.getNome());
				}
			}
		}
	}

	@Test
	void ilGeneratoreDelGiocoProduceNomiInglesi() {
		for (int i = 0; i < 100; i++) {
			String nome = GeneratoreArtefatti.istanza().generaArtefatto(TipoArtefatto.SPADA, 3).getModelloDati().getNome();
			assertTrue(nome.startsWith("the ") && nome.contains("sword"), nome);
		}
	}
}
