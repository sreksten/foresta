package com.threeamigos.foresta.tattiche;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I dati delle tattiche (<code>modellodati/tattiche</code>: TatticaMD, VoceMD, SelettoreMD, FiltroMD, PredicatoMD, AzioneMD, OrdinamentoMD) sono solo dati: la UI li legge e li
 * modifica, il salvataggio li scrive, quindi non devono dipendere dal motore né dal dominio, ma solo dai tipi (<code>tipi</code>) e fra loro. La logica che li applica sta
 * in <code>tattiche</code> (tattiche_di_combattimento_automatiche.md §11.11).
 */
class DipendenzeTatticheTest {

	private static final Path DATI = Paths.get("src/main/java/com/threeamigos/foresta/modellodati/tattiche");
	private static final Pattern RIFERIMENTO = Pattern.compile("\\bcom\\.threeamigos\\.foresta\\.([a-z]+)((?:\\.[a-z]+)*)\\.([A-Z]\\w*|\\*)");

	@Test
	void iDatiDelleTatticheDipendonoSoloDaiTipi() throws IOException {
		List<Path> file = sorgenti();
		assertFalse(file.isEmpty());
		Set<String> vietati = new TreeSet<>();
		for (Path sorgente : file) {
			String codice = new String(Files.readAllBytes(sorgente), StandardCharsets.UTF_8).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
			Matcher m = RIFERIMENTO.matcher(codice);
			while (m.find()) {
				String pacchetto = m.group(1) + m.group(2);
				if (!pacchetto.equals("tipi") && !pacchetto.startsWith("tipi.") && !pacchetto.equals("modellodati.tattiche")) {
					vietati.add(sorgente.getFileName() + " -> " + pacchetto + "." + m.group(3));
				}
			}
		}
		assertTrue(vietati.isEmpty(), "I dati delle tattiche dipendono da qualcosa che non sono i tipi:\n" + String.join("\n", vietati));
	}

	private static List<Path> sorgenti() throws IOException {
		try (Stream<Path> percorsi = Files.walk(DATI)) {
			return percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}
}
