package com.threeamigos.foresta.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le immagini citate nel codice per nome ("icone/Aiuto.gif") esistono con quel nome esatto, maiuscole comprese.
 * Su macOS e Windows il file system non distingue le maiuscole e il gioco funziona anche con un nome sbagliato, ma
 * dentro il JAR (e su Linux) no, e una risorsa di base mancante fa uscire il gioco: per questo si confronta con i nomi
 * veri dei file e non con {@code getResource} o {@code File.exists}.
 */
class RiferimentiAlleImmaginiTest {

	private static final Path IMMAGINI = Paths.get("src/main/resources/com/threeamigos/foresta/img");
	private static final Path SORGENTI = Paths.get("src/main/java");
	private static final Pattern RIFERIMENTO = Pattern.compile(
			"\"((?:icone|fondi|fondinon2x2|personaggi|oggetti|mappa|locazioni|alfabeto|intermezzi)/[^\"/]+\\.(?:gif|png)|[A-Za-z0-9]+\\.(?:gif|png))\"");

	@Test
	void ogniImmagineCitataEsisteConIlNomeEsatto() throws IOException {
		Set<String> esistenti;
		try (Stream<Path> file = Files.walk(IMMAGINI)) {
			esistenti = file.filter(Files::isRegularFile)
					.map(p -> IMMAGINI.relativize(p).toString().replace('\\', '/'))
					.collect(Collectors.toSet());
		}
		Set<String> mancanti = new TreeSet<>();
		try (Stream<Path> sorgenti = Files.walk(SORGENTI)) {
			for (Path sorgente : (Iterable<Path>) sorgenti.filter(p -> p.toString().endsWith(".java"))::iterator) {
				// Senza i commenti: lì ci sono esempi, non immagini vere
				String codice = Files.readAllLines(sorgente, StandardCharsets.UTF_8).stream()
						.filter(riga -> !riga.trim().startsWith("*") && !riga.trim().startsWith("//") && !riga.trim().startsWith("/*"))
						.collect(Collectors.joining("\n"));
				Matcher m = RIFERIMENTO.matcher(codice);
				while (m.find()) {
					// Il nome senza cartella può essere altro (un nome di file in un commento o un'altra risorsa)
					String nome = m.group(1);
					boolean conCartella = nome.contains("/");
					if (conCartella && !esistenti.contains(nome)) {
						mancanti.add(nome + "  (in " + sorgente.getFileName() + ")");
					} else if (!conCartella && nome.startsWith("Logo") && !esistenti.contains(nome)) {
						mancanti.add(nome + "  (in " + sorgente.getFileName() + ")");
					}
				}
			}
		}
		assertTrue(mancanti.isEmpty(), "Immagini citate ma assenti, o con le maiuscole diverse:\n" + String.join("\n", mancanti));
	}
}
