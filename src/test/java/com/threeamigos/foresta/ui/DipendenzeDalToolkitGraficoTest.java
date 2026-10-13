package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il motore non conosce il toolkit grafico: nessun sorgente fuori da ui usa java.awt, javax.swing, javax.imageio o
 * javax.sound. Così motore, dominio, eventi e interfacce si possono isolare in un modulo che non dipende da Swing
 * (e riusare con un altro front-end). Il thread principale si raggiunge da {@code strumenti.ThreadPrincipale}.
 */
class DipendenzeDalToolkitGraficoTest {

	private static final Path SORGENTI = Paths.get("src/main/java/com/threeamigos/foresta");

	private static final Pattern TOOLKIT = Pattern.compile("\\b(java\\.awt|javax\\.swing|javax\\.imageio|javax\\.sound)\\.");

	@Test
	void nessunoFuoriDallaUIUsaIlToolkitGrafico() throws IOException {
		Set<String> vietati = new TreeSet<>();
		for (Path file : sorgenti()) {
			String nome = SORGENTI.relativize(file).toString().replace('\\', '/');
			if (!nome.startsWith("ui/") && TOOLKIT.matcher(codice(file)).find()) {
				vietati.add(nome);
			}
		}
		assertTrue(vietati.isEmpty(), "Questi sorgenti fuori da ui dipendono dal toolkit grafico:\n" + String.join("\n", vietati));
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
