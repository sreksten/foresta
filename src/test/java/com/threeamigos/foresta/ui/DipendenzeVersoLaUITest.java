package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il motore non conosce la UI: nel verso opposto a {@link DipendenzeUITest}, nessun sorgente fuori da ui importa
 * classi di ui, salvo Main (che assembla le parti) e le eccezioni di {@link #ECCEZIONI}.
 */
class DipendenzeVersoLaUITest {

	private static final Path SORGENTI = Paths.get("src/main/java/com/threeamigos/foresta");

	/**
	 * Eventi che portano tipi della UI (sprite e fumetti): li crea e li riceve solo la UI, e andrebbero spostati nel
	 * suo modulo. Quando uno smette di dipendere dalla UI, va tolto da qui (il test lo pretende).
	 */
	private static final Set<String> ECCEZIONI = new TreeSet<>(Arrays.asList(
			"eventi/interni/InternoCreazioneSpriteATempo.java",
			"eventi/interni/InternoCreazioneSpriteAnnuncioGlobale.java",
			"eventi/interni/InternoCreazioneSpriteEffettoDiStato.java",
			"eventi/interni/InternoCreazioneSpriteFumettoATempo.java",
			"eventi/interni/InternoCreazioneSpriteInDissolvenza.java",
			"eventi/interni/InternoNotificaViaFumettoATempo.java"));

	@Test
	void nessunoFuoriDallaUIDipendeDallaUI() throws IOException {
		Set<String> trovati = new TreeSet<>();
		Set<String> vietati = new TreeSet<>();
		for (Path file : sorgenti()) {
			String nome = SORGENTI.relativize(file).toString().replace('\\', '/');
			if (nome.startsWith("ui/") || nome.equals("Main.java")) {
				continue;
			}
			if (codice(file).contains("com.threeamigos.foresta.ui.")) {
				trovati.add(nome);
				if (!ECCEZIONI.contains(nome)) {
					vietati.add(nome);
				}
			}
		}
		assertTrue(vietati.isEmpty(), "Questi sorgenti dipendono dalla UI:\n" + String.join("\n", vietati));
		assertEquals(ECCEZIONI, trovati, "Un'eccezione non serve più: toglierla da ECCEZIONI");
	}

	private static String codice(Path file) throws IOException {
		return new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("//[^\\n]*", "");
	}

	private static List<Path> sorgenti() throws IOException {
		try (Stream<Path> percorsi = Files.walk(SORGENTI)) {
			return percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}
}
