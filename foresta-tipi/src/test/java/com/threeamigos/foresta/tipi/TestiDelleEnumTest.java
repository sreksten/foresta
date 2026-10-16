package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoFiltro;
import com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento;
import com.threeamigos.foresta.tipi.tattiche.TipoPredicato;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I testi delle costanti di enum, letti da un file per enum ({@code tipo_attributo_it.properties}...): ogni costante ha
 * tutte le sue voci, e nei file non ci sono voci per costanti che non esistono.
 */
class TestiDelleEnumTest {

	// Le enum che hanno i testi in un file, con le voci che ogni costante deve avere (TipoLocazione è a parte: le voci
	// ci sono solo per le locazioni uniche)
	private static final Map<Class<? extends Enum<?>>, List<String>> ENUM = new LinkedHashMap<>();

	static {
		ENUM.put(CategoriaPersonaggio.class, Arrays.asList("NOME"));
		ENUM.put(ClasseIncantesimo.class, Arrays.asList("NOME", "NOME_SINGOLARE", "NOME_PLURALE", "EFFETTO"));
		ENUM.put(TipoArtefatto.class, Arrays.asList("DESCRIZIONE", "UTILIZZO"));
		ENUM.put(TipoAttributo.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(TipoDanno.class, Arrays.asList("NOME"));
		ENUM.put(TipoEffettoDiStato.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoInterazioneConEffettiDiStato.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoMotivoRifiutoIncantatura.class, Arrays.asList("FRASE"));
		ENUM.put(TipoNegozio.class, Arrays.asList("NOME"));
		ENUM.put(TipoRaritaArtefatto.class, Arrays.asList("NOME"));
		ENUM.put(TipoTrofeo.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(TipoCanto.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(GradoIncantamento.class, Arrays.asList("NOME"));
		ENUM.put(TipoAzione.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoFiltro.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoOrdinamento.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoPredicato.class, Arrays.asList("DESCRIZIONE"));
		ENUM.put(TipoRosa.class, Arrays.asList("DESCRIZIONE"));
	}

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ogniCostanteHaTutteLeSueVoci() {
		for (Map.Entry<Class<? extends Enum<?>>, List<String>> enumerazione : ENUM.entrySet()) {
			for (Enum<?> costante : enumerazione.getKey().getEnumConstants()) {
				for (String voce : enumerazione.getValue()) {
					assertTrue(Testi.testo(costante, voce).filter(t -> !t.trim().isEmpty()).isPresent(),
							"manca " + costante.getDeclaringClass().getSimpleName() + "." + costante + "." + voce);
				}
			}
		}
	}

	@Test
	void iNomiDelleLocazioniUnicheSonoNelFile() {
		for (TipoLocazione locazione : TipoLocazione.values()) {
			if (locazione.isLocazioneUnica()) {
				assertTrue(locazione.getNomeProprio() != null && !locazione.getNomeProprio().isEmpty(), "manca il nome di " + locazione);
			} else {
				assertEquals(null, locazione.getNomeProprio(), locazione + " non è unica e non ha un nome proprio");
			}
			if (locazione.getCategoria() == CategoriaLocazione.CITTA) {
				assertTrue(locazione.getNomeProprio().endsWith(locazione.getNomeDellaCitta()), locazione.getNomeProprio());
			} else {
				assertEquals(null, locazione.getNomeDellaCitta());
			}
		}
	}

	@Test
	void neiFileNonCiSonoVociPerCostantiOVociCheNonEsistono() throws IOException {
		for (Map.Entry<Class<? extends Enum<?>>, List<String>> enumerazione : ENUM.entrySet()) {
			verificaIlFile(enumerazione.getKey(), new HashSet<>(enumerazione.getValue()),
					enumerazione.getKey().getEnumConstants().length * enumerazione.getValue().size());
		}
		// Le locazioni: il nome proprio per le nove uniche, quello della città per le quattro città
		verificaIlFile(TipoLocazione.class, new HashSet<>(Arrays.asList("NOME_PROPRIO", "NOME_DELLA_CITTA")), 9 + 4);
	}

	private static void verificaIlFile(Class<? extends Enum<?>> enumerazione, Set<String> voci, int vociAttese) throws IOException {
		String nomeDelFile = Testi.nomeDelFile(enumerazione) + "_it.properties";
		Properties testi = new Properties();
		try (InputStream flusso = enumerazione.getResourceAsStream(nomeDelFile)) {
			assertTrue(flusso != null, "manca il file " + nomeDelFile);
			testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
		}
		Set<String> costanti = new HashSet<>();
		for (Enum<?> costante : enumerazione.getEnumConstants()) {
			costanti.add(costante.name());
		}
		for (String chiave : testi.stringPropertyNames()) {
			int punto = chiave.indexOf('.');
			assertTrue(punto > 0 && costanti.contains(chiave.substring(0, punto)), nomeDelFile + ": costante sconosciuta in " + chiave);
			assertTrue(voci.contains(chiave.substring(punto + 1)), nomeDelFile + ": voce sconosciuta in " + chiave);
			assertFalse(testi.getProperty(chiave).trim().isEmpty(), nomeDelFile + ": testo vuoto in " + chiave);
		}
		assertEquals(vociAttese, testi.size(), nomeDelFile + ": numero di voci");
	}

	@Test
	void ilNomeDelFileESiDeriveDalNomeDellEnum() {
		assertEquals("tipo_attributo", Testi.nomeDelFile(TipoAttributo.class));
		assertEquals("tipo_interazione_con_effetti_di_stato", Testi.nomeDelFile(TipoInterazioneConEffettiDiStato.class));
		assertEquals("tipo_rosa", Testi.nomeDelFile(TipoRosa.class));
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		String italiano = TipoAttributo.FORZA.getNome();

		Testi.impostaLingua("xx");

		assertEquals(italiano, TipoAttributo.FORZA.getNome());
		assertEquals("Forza", TipoAttributo.FORZA.getNome());
		assertEquals(TipoRosa.ALLEATI.getDescrizione(), Testi.testo(TipoRosa.ALLEATI, "DESCRIZIONE").get());
	}

	@Test
	void unaVoceCheNonEsisteDaUnRisultatoVuotoOIlNomeDellaCostante() {
		assertFalse(Testi.testo(TipoAttributo.FORZA, "NON_ESISTE").isPresent());
		assertEquals("FORZA", Testi.testoOCostante(TipoAttributo.FORZA, "NON_ESISTE"));
	}
}
