package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Una grammatica tradotta ha le stesse produzioni dell'italiana e almeno quasi altrettante alternative (averne di più non è un difetto): se una versione
 * viene "ridotta" (meno produzioni, o meno parole in una produzione) il test fallisce, così una traduzione a metà
 * non passa inosservata.
 */
class ConfrontoGrammaticheTest {

	private static final String PERCORSO = "/com/threeamigos/foresta/testi/";
	private static final String[] GRAMMATICHE = {"fiabe", "locande", "missioni", "nomi", "oroscopo", "rovine", "sfide", "tappe", "templi"};

	/** La tolleranza: la traduzione adatta le battute e può unire o spezzare qualche alternativa. */
	private static final double TOLLERANZA = 0.03;
	private static final int TOLLERANZA_MINIMA = 2;

	@TestFactory
	List<DynamicTest> leVersioniIngleseNonSonoRidotte() {
		List<DynamicTest> prove = new ArrayList<>();
		for (String nome : GRAMMATICHE) {
			prove.add(DynamicTest.dynamicTest(nome, () -> confronta(nome)));
		}
		return prove;
	}

	private static void confronta(String nome) throws IOException {
		Map<String, Integer> italiano = alternativePerProduzione(PERCORSO + nome + ".txt");
		Map<String, Integer> inglese = alternativePerProduzione(PERCORSO + nome + "_en.txt");
		assertEquals(italiano.keySet(), inglese.keySet(), nome + ": le produzioni devono essere le stesse");
		List<String> differenze = new ArrayList<>();
		for (Map.Entry<String, Integer> produzione : italiano.entrySet()) {
			int attese = produzione.getValue();
			int trovate = inglese.get(produzione.getKey());
			int margine = Math.max(TOLLERANZA_MINIMA, (int) Math.ceil(attese * TOLLERANZA));
			if (trovate < attese - margine) {
				differenze.add(produzione.getKey() + ": " + attese + " alternative in italiano, " + trovate + " in inglese");
			}
		}
		assertTrue(differenze.isEmpty(), nome + ": " + differenze);
	}

	/**
	 * Quante alternative ha ogni produzione: ogni riga rientrata ne vale una, più una per ogni barra verticale (le
	 * alternative scritte sulla stessa riga). Le righe di commento non contano.
	 */
	static Map<String, Integer> alternativePerProduzione(String risorsa) throws IOException {
		Map<String, Integer> produzioni = new LinkedHashMap<>();
		try (InputStream flusso = ConfrontoGrammaticheTest.class.getResourceAsStream(risorsa)) {
			assertTrue(flusso != null, "Manca " + risorsa);
			BufferedReader lettore = new BufferedReader(new InputStreamReader(flusso, StandardCharsets.UTF_8));
			String produzione = null;
			String riga;
			while ((riga = lettore.readLine()) != null) {
				if (riga.trim().isEmpty() || riga.trim().startsWith("//") || riga.trim().startsWith("#")) {
					continue;
				}
				if (!Character.isWhitespace(riga.charAt(0))) {
					produzione = riga.trim();
					produzioni.put(produzione, 0);
				} else if (produzione != null) {
					produzioni.merge(produzione, 1 + (int) riga.chars().filter(c -> c == '|').count(), Integer::sum);
				}
			}
		}
		return produzioni;
	}
}
