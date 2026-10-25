package com.threeamigos.foresta.tipi;




import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi dei trofei, letti da tipo_trofeo_it.properties: ogni trofeo ha nome e descrizione, e nel file non ci sono voci per trofei che
 * non esistono.
 */
class TestiDelTipoTrofeoTest {

	// Le enum che hanno i testi in un file, con le voci che ogni costante deve avere
	private static final Map<Class<? extends Enum<?>>, List<String>> ENUM = new LinkedHashMap<>();

	static {
		ENUM.put(TipoTrofeo.class, Arrays.asList("NOME", "DESCRIZIONE"));
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
	void ogniGetterDeiTestiLeggeLaSuaVoce() throws Exception {
		for (Map.Entry<Class<? extends Enum<?>>, List<String>> enumerazione : ENUM.entrySet()) {
			for (String voce : enumerazione.getValue()) {
				// La voce NOME_SINGOLARE si legge con getNomeSingolare, e così via
				java.lang.reflect.Method getter = enumerazione.getKey().getMethod(getterDi(voce));
				for (Enum<?> costante : enumerazione.getKey().getEnumConstants()) {
					assertEquals(Testi.testo(costante, voce).get(), getter.invoke(costante), costante + "." + voce);
				}
			}
		}
		for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
			assertEquals(Testi.testoOCostante(interazione, "AREA"), interazione.getTestoAdArea(), interazione.name());
		}
	}

	private static String getterDi(String voce) {
		StringBuilder nome = new StringBuilder("get");
		for (String parola : voce.split("_")) {
			nome.append(parola.charAt(0)).append(parola.substring(1).toLowerCase(java.util.Locale.ROOT));
		}
		return nome.toString();
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
}
