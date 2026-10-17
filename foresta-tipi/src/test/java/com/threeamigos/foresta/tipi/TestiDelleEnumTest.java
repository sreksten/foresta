package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.tipi.tattiche.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi delle costanti di enum, letti da un file per enum ({@code tipo_attributo_it.properties}...): ogni costante ha
 * tutte le sue voci, e nei file non ci sono voci per costanti che non esistono.
 */
class TestiDelleEnumTest {

	// Le enum che hanno i testi in un file, con le voci che ogni costante deve avere (TipoLocazione è a parte: le voci
	// ci sono solo per le locazioni uniche)
	private static final Map<Class<? extends Enum<?>>, List<String>> ENUM = new LinkedHashMap<>();

	static {
		ENUM.put(CategoriaPersonaggio.class, Collections.singletonList("NOME"));
		ENUM.put(TipoIncantesimo.class, Arrays.asList("NOME", "NOME_SINGOLARE", "NOME_PLURALE", "EFFETTO"));
		ENUM.put(TipoArtefatto.class, Arrays.asList("DESCRIZIONE", "UTILIZZO"));
		ENUM.put(TipoAttributo.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(TipoDanno.class, Collections.singletonList("NOME"));
		ENUM.put(TipoEffettoDiStato.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoInterazioneConEffettiDiStato.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoMotivoRifiutoIncantatura.class, Collections.singletonList("FRASE"));
		ENUM.put(TipoNegozio.class, Collections.singletonList("NOME"));
		ENUM.put(TipoRaritaArtefatto.class, Collections.singletonList("NOME"));
		ENUM.put(TipoTrofeo.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(TipoCanto.class, Arrays.asList("NOME", "DESCRIZIONE"));
		ENUM.put(GradoIncantamento.class, Collections.singletonList("NOME"));
		ENUM.put(TipoAzione.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoFiltro.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoOrdinamento.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoPredicato.class, Collections.singletonList("DESCRIZIONE"));
		ENUM.put(TipoRosa.class, Collections.singletonList("DESCRIZIONE"));
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
	void ogniInterazioneAdAreaHaLaSuaFrase() {
		for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
			if (interazione.getDannoAdArea() != null) {
				assertTrue(Testi.testo(interazione, "AREA").filter(t -> !t.trim().isEmpty()).isPresent(), "manca " + interazione + ".AREA");
			}
		}
	}

	@Test
	void iNomiDelleLocazioniUnicheSonoNelFile() {
		for (TipoLocazione locazione : TipoLocazione.values()) {
			if (locazione.isLocazioneUnica()) {
				assertTrue(locazione.getNomeProprio() != null && !locazione.getNomeProprio().isEmpty(), "manca il nome di " + locazione);
			} else {
                assertNull(locazione.getNomeProprio(), locazione + " non è unica e non ha un nome proprio");
			}
			if (locazione.getCategoria() == CategoriaLocazione.CITTA) {
				assertTrue(locazione.getNomeProprio().endsWith(locazione.getNomeDellaCitta()), locazione.getNomeProprio());
			} else {
                assertNull(locazione.getNomeDellaCitta());
			}
		}
	}

	@Test
	void neiFileNonCiSonoVociPerCostantiOVociCheNonEsistono() throws IOException {
		for (Map.Entry<Class<? extends Enum<?>>, List<String>> enumerazione : ENUM.entrySet()) {
			if (enumerazione.getKey() == TipoInterazioneConEffettiDiStato.class) {
				continue;
			}
			verificaIlFile(enumerazione.getKey(), new HashSet<>(enumerazione.getValue()),
					enumerazione.getKey().getEnumConstants().length * enumerazione.getValue().size());
		}
		// Le interazioni: la descrizione per tutte, la frase del danno ad area per quelle che ne hanno uno
		long adArea = Arrays.stream(TipoInterazioneConEffettiDiStato.values()).filter(i -> i.getDannoAdArea() != null).count();
		verificaIlFile(TipoInterazioneConEffettiDiStato.class, new HashSet<>(Arrays.asList("DESCRIZIONE", "AREA")),
				TipoInterazioneConEffettiDiStato.values().length + (int) adArea);
		// Le locazioni: il nome proprio per le nove uniche, quello della città per le quattro città
		verificaIlFile(TipoLocazione.class, new HashSet<>(Arrays.asList("NOME_PROPRIO", "NOME_DELLA_CITTA")), 9 + 4);
	}

	private static void verificaIlFile(Class<? extends Enum<?>> enumerazione, Set<String> voci, int vociAttese) throws IOException {
		String nomeDelFile = Testi.nomeDelFile(enumerazione) + "_it.properties";
		Properties testi = new Properties();
		try (InputStream flusso = enumerazione.getResourceAsStream(nomeDelFile)) {
            assertNotNull(flusso, "manca il file " + nomeDelFile);
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
