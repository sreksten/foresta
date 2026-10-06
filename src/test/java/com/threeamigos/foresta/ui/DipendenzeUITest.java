package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.Bosco;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La UI legge lo stato da interfacce e parla al motore con gli eventi (vedi todo.md, "Separare motore, modello dati
 * e UI"): qui si controlla, leggendo i sorgenti, che non dipenda da altro.
 * <p>
 * Dalla UI si possono usare i pacchetti di {@link #PACCHETTI_AMMESSI}; da quelli di {@link #PACCHETTI_DI_SERVIZIO}
 * solo le classi che a loro volta non dipendono dal motore o dal dominio (per esempio i dati degli intermezzi o
 * Misc). Gli eventi portano ancora oggetti del dominio: è il punto 8 del todo, e questo test non lo guarda.
 */
class DipendenzeUITest {

	private static final String BASE = "com.threeamigos.foresta.";
	private static final Path SORGENTI = Paths.get("src/main/java/com/threeamigos/foresta");

	private static final Set<String> PACCHETTI_AMMESSI = new HashSet<>(Arrays.asList(
			"ui", "tipi", "interfacce", "eventi", "modellodati"));

	private static final Set<String> PACCHETTI_DI_SERVIZIO = new HashSet<>(Arrays.asList("strumenti", "intermezzi"));

	/**
	 * Le dipendenze che restano da togliere (oggi nessuna): quando se ne toglie una, va tolta anche da qui (il test
	 * lo pretende)
	 */
	private static final Set<String> ECCEZIONI = new TreeSet<>();

	private static final Pattern RIFERIMENTO = Pattern.compile("\\bcom\\.threeamigos\\.foresta\\.([a-z]+)\\.([A-Z]\\w*|\\*)");

	@Test
	void laUIDipendeSoloDaPacchettiAmmessi() throws IOException {
		Set<String> vietate = new TreeSet<>();
		Set<String> eccezioniUsate = new TreeSet<>();
		for (Path file : sorgenti(SORGENTI.resolve("ui"))) {
			for (String riferimento : riferimenti(file)) {
				String pacchetto = riferimento.substring(0, riferimento.indexOf('.'));
				if (PACCHETTI_AMMESSI.contains(pacchetto)) {
					continue;
				}
				if (ECCEZIONI.contains(riferimento)) {
					eccezioniUsate.add(riferimento);
				} else if (!PACCHETTI_DI_SERVIZIO.contains(pacchetto) || !diServizioPulita(riferimento)) {
					vietate.add(SORGENTI.relativize(file) + " -> " + riferimento);
				}
			}
		}
		assertTrue(vietate.isEmpty(), "La UI dipende da classi del motore o del dominio:\n" + String.join("\n", vietate));
		assertEquals(ECCEZIONI, eccezioniUsate, "Un'eccezione non serve più: toglierla da ECCEZIONI");
	}

	/**
	 * Una classe di servizio (strumenti, intermezzi) è pulita se dipende solo dai pacchetti ammessi o dal suo stesso
	 * pacchetto (che non si segue oltre)
	 */
	private static boolean diServizioPulita(String riferimento) throws IOException {
		if (riferimento.endsWith("*")) {
			return false;
		}
		String pacchetto = riferimento.substring(0, riferimento.indexOf('.'));
		Path file = SORGENTI.resolve(riferimento.replace('.', '/') + ".java");
		for (String suo : riferimenti(file)) {
			String suoPacchetto = suo.substring(0, suo.indexOf('.'));
			if (!PACCHETTI_AMMESSI.contains(suoPacchetto) && !suoPacchetto.equals(pacchetto)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Le classi del gioco che il file nomina nel codice (import compresi, commenti esclusi), come "pacchetto.Classe"
	 */
	private static Set<String> riferimenti(Path file) throws IOException {
		String codice = new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("//[^\\n]*", "");
		Set<String> riferimenti = new TreeSet<>();
		Matcher m = RIFERIMENTO.matcher(codice);
		while (m.find()) {
			riferimenti.add(m.group(1) + "." + m.group(2));
		}
		return riferimenti;
	}

	private static List<Path> sorgenti(Path cartella) throws IOException {
		try (Stream<Path> percorsi = Files.walk(cartella)) {
			return percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}

	/**
	 * ImageCache non legge più Bosco.NUMERO_VARIANTI_MAPPA: le due cose le tiene allineate questo test
	 */
	@Test
	void leImmaginiDelBoscoSonoQuanteLeVarianti() {
		assertEquals(Bosco.NUMERO_VARIANTI_MAPPA, ImageCache.RISORSE_VARIANTI_BOSCO.length);
	}
}
