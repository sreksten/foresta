package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.testi.ProduttoreDiOroscopiCasuale.DialogoSullOroscopo;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il capo chiede di una cosa che l'alchimista ha appena citato nell'oroscopo, con le domande accordate al genere e al numero
 * (vedi oroscopo.txt e ProduttoreDiOroscopiCasuale.dialogoSullOroscopo).
 */
class DialogoSullOroscopoTest {

	private static final String GRAMMATICA = "/com/threeamigos/foresta/testi/oroscopo.txt";
	private static final Pattern ARTICOLO = Pattern.compile("^(il|lo|la|i|gli|le|l') ?");

	private static String grammatica() throws IOException {
		try (BufferedReader lettore = new BufferedReader(new InputStreamReader(
				DialogoSullOroscopoTest.class.getResourceAsStream(GRAMMATICA), StandardCharsets.UTF_8))) {
			return lettore.lines().filter(riga -> !riga.startsWith("//")).collect(Collectors.joining("\n"));
		}
	}

	@Test
	void ogniFormaDeiTemiHaLeSueDomande() throws IOException {
		String grammatica = grammatica();
		Matcher tema = Pattern.compile("\\[TEMA=([A-Z]+):").matcher(grammatica);
		Set<String> forme = new HashSet<>();
		while (tema.find()) {
			forme.add(tema.group(1));
		}
		assertFalse(forme.isEmpty());
		for (String forma : forme) {
			assertTrue(grammatica.contains("\nDOMANDA_" + forma + "\n"), "mancano le domande per la forma " + forma);
		}
	}

	@Test
	void ilCapoChiedeDiUnaCosaCheHaAppenaSentito() {
		boolean generica = false;
		boolean sulTema = false;
		for (int i = 0; i < 500; i++) {
			DialogoSullOroscopo dialogo = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo();
			assertFalse(dialogo.getOroscopo().isEmpty());
			assertFalse(dialogo.getOroscopo().contains("/"), dialogo.getOroscopo());
			for (String testo : new String[] {dialogo.getDomanda(), dialogo.getRisposta()}) {
				assertFalse(testo.isEmpty());
				assertFalse(testo.contains("[") || testo.contains("]"), testo);
			}
			if (dialogo.getTema() == null) {
				generica = true;
				continue;
			}
			sulTema = true;
			String nome = ARTICOLO.matcher(dialogo.getTema()).replaceFirst("");
			assertTrue(dialogo.getOroscopo().toLowerCase(Locale.ROOT).contains(nome.toLowerCase(Locale.ROOT)),
					"\"" + nome + "\" non è nell'oroscopo: " + dialogo.getOroscopo());
		}
		assertTrue(sulTema, "il capo non chiede mai di una cosa citata");
		assertTrue(generica, "il capo non fa mai una domanda generica");
	}
}
