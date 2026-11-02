package com.threeamigos.foresta.artefatti;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * La variante britannica di una grammatica (nome_en_GB.txt) ha le stesse produzioni e lo stesso numero di alternative di
 * quella americana (cambia solo la grafia) e non lascia le grafie americane più comuni.
 */
class VariantiBritannicheDelleGrammaticheTest {

	private static final String PERCORSO = "/com/threeamigos/foresta/artefatti/";
	private static final String[] GRAMMATICHE = {"artefatti2", "ingredienti", "leggendari"};

	private static final Pattern AMERICANE = Pattern.compile("\\b(toward|travelers?|traveled|armor(?:ed|er|ers)?|colors?|colored|honor(?:s|ed)?|neighbors?|favors?|centers?|defenses?|artifacts?|organize[sd]?|recognize[sd]?)\\b",
			Pattern.CASE_INSENSITIVE);

	@TestFactory
	List<DynamicTest> leVariantiBritanniche() {
		List<DynamicTest> prove = new ArrayList<>();
		for (String nome : GRAMMATICHE) {
			if (getClass().getResource(PERCORSO + nome + "_en_GB.txt") != null) {
				prove.add(DynamicTest.dynamicTest(nome, () -> controlla(nome)));
			}
		}
		return prove;
	}

	private static void controlla(String nome) throws IOException {
		assertEquals(ConfrontoGrammaticheArtefattiTest.alternativePerProduzione(PERCORSO + nome + "_en.txt", false), ConfrontoGrammaticheArtefattiTest.alternativePerProduzione(PERCORSO + nome + "_en_GB.txt", false), nome + ": stesse produzioni e stesse alternative della versione americana");
		try (InputStream flusso = VariantiBritannicheDelleGrammaticheTest.class.getResourceAsStream(PERCORSO + nome + "_en_GB.txt")) {
			String testo = new String(readAll(flusso), StandardCharsets.UTF_8);
			Matcher m = AMERICANE.matcher(testo);
			boolean trovata = m.find();
			assertFalse(trovata, nome + ": grafia americana nella variante britannica: " + (trovata ? m.group() : ""));
		}
	}

	private static byte[] readAll(InputStream flusso) throws IOException {
		java.io.ByteArrayOutputStream uscita = new java.io.ByteArrayOutputStream();
		byte[] blocco = new byte[8192];
		int letti;
		while ((letti = flusso.read(blocco)) > 0) {
			uscita.write(blocco, 0, letti);
		}
		return uscita.toByteArray();
	}
}
