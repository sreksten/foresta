package com.threeamigos.foresta.artefatti;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Una grammatica tradotta ha le stesse produzioni dell'italiana e almeno quasi altrettante alternative: se una versione
 * viene "ridotta" (meno produzioni, o meno parole in una produzione) il test fallisce, così una traduzione a metà
 * non passa inosservata.
 * <p>
 * In italiano gli artefatti hanno il genere: ogni sezione c'è al femminile singolare, al maschile singolare e al
 * maschile plurale, con le stesse parole declinate. In inglese il genere non c'è, quindi si confronta la sezione
 * senza genere con quella al femminile singolare e si scartano le altre due.
 */
class ConfrontoGrammaticheArtefattiTest {

	private static final String PERCORSO = "/com/threeamigos/foresta/artefatti/";
	private static final String[] GRAMMATICHE = {"artefatti2", "ingredienti", "leggendari"};

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
		Map<String, Integer> italiano = alternativePerProduzione(PERCORSO + nome + ".txt", true);
		Map<String, Integer> inglese = alternativePerProduzione(PERCORSO + nome + "_en.txt", false);
		List<String> differenze = new ArrayList<>();
		for (Map.Entry<String, Integer> produzione : italiano.entrySet()) {
			Integer trovate = inglese.get(produzione.getKey());
			if (trovate == null) {
				differenze.add(produzione.getKey() + ": manca in inglese");
				continue;
			}
			int attese = produzione.getValue();
			int margine = Math.max(TOLLERANZA_MINIMA, (int) Math.ceil(attese * TOLLERANZA));
			if (trovate < attese - margine) {
				differenze.add(produzione.getKey() + ": " + attese + " alternative in italiano, " + trovate + " in inglese");
			}
		}
		assertTrue(differenze.isEmpty(), nome + ": " + differenze);
	}

	/**
	 * Quante alternative ha ogni produzione: ogni riga rientrata ne vale una, più una per ogni barra verticale (le
	 * alternative scritte sulla stessa riga). Le righe di commento non contano. Con {@code senzaGenere} le produzioni al
	 * maschile (singolare e plurale) si scartano e quelle al femminile singolare perdono il suffisso.
	 */
	static Map<String, Integer> alternativePerProduzione(String risorsa, boolean senzaGenere) throws IOException {
		Map<String, Integer> produzioni = new LinkedHashMap<>();
		try (InputStream flusso = ConfrontoGrammaticheArtefattiTest.class.getResourceAsStream(risorsa)) {
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
					if (senzaGenere) {
						if (produzione.endsWith("_MASCHILE_SINGOLARE") || produzione.endsWith("_MASCHILE_PLURALE")) {
							produzione = null;
							continue;
						}
						produzione = produzione.replaceAll("_FEMMINILE_SINGOLARE$", "");
					}
					produzioni.put(produzione, 0);
				} else if (produzione != null) {
					produzioni.merge(produzione, 1 + (int) riga.chars().filter(c -> c == '|').count(), Integer::sum);
				}
			}
		}
		return produzioni;
	}
}
