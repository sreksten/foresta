package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.SorgentiDelProgetto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il motore non conosce il toolkit grafico: nessun sorgente fuori dal modulo foresta-swing usa java.awt, javax.swing, javax.imageio o
 * javax.sound. Così motore, dominio, eventi e interfacce si possono isolare in un modulo che non dipende da Swing
 * (e riusare con un altro front-end). Il thread principale si raggiunge da {@code temporizzatore.ThreadPrincipale}.
 */
class DipendenzeDalToolkitGraficoTest {

	private static final Pattern TOOLKIT = Pattern.compile("\\b(java\\.awt|javax\\.swing|javax\\.imageio|javax\\.sound)\\.");

	@Test
	void nessunoFuoriDallaUIUsaIlToolkitGrafico() throws IOException {
		Set<String> vietati = new TreeSet<>();
		for (Path file : SorgentiDelProgetto.sorgenti(true)) {
			if (TOOLKIT.matcher(codice(file)).find()) {
				vietati.add(SorgentiDelProgetto.radice().relativize(file).toString().replace('\\', '/'));
			}
		}
		assertTrue(vietati.isEmpty(), "Questi sorgenti fuori dal modulo della UI dipendono dal toolkit grafico:\n" + String.join("\n", vietati));
	}

	private static String codice(Path file) throws IOException {
		return new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("//[^\\n]*", "");
	}
}
