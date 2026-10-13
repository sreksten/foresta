package com.threeamigos.foresta;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dove stanno i sorgenti e le risorse dei moduli, per i test che li leggono come file (le regole di dipendenza che il
 * compilatore non può controllare e i riferimenti alle immagini). Si parte dalla cartella da cui gira il test, che sia
 * quella del modulo (Maven) o quella del progetto (IntelliJ), e si sale fino a quella che contiene i moduli.
 */
public final class SorgentiDelProgetto {

	private static final String MODULO_UI = "foresta-swing";

	private SorgentiDelProgetto() {
	}

	/**
	 * La cartella del progetto, quella che contiene i moduli.
	 */
	public static Path radice() {
		Path corrente = Paths.get("").toAbsolutePath();
		while (corrente != null && !Files.isDirectory(corrente.resolve("foresta-tipi"))) {
			corrente = corrente.getParent();
		}
		if (corrente == null) {
			throw new IllegalStateException("Non trovo la cartella del progetto (quella con foresta-tipi)");
		}
		return corrente;
	}

	/**
	 * I sorgenti Java di tutti i moduli, senza quelli dell'interfaccia grafica se {@code senzaUI} è true.
	 */
	public static List<Path> sorgenti(boolean senzaUI) throws IOException {
		List<Path> file = new ArrayList<>();
		try (DirectoryStream<Path> moduli = Files.newDirectoryStream(radice(), "foresta-*")) {
			for (Path modulo : moduli) {
				Path cartella = modulo.resolve("src/main/java");
				if (!Files.isDirectory(cartella) || senzaUI && modulo.getFileName().toString().equals(MODULO_UI)) {
					continue;
				}
				try (Stream<Path> percorsi = Files.walk(cartella)) {
					file.addAll(percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList()));
				}
			}
		}
		return file;
	}

	/**
	 * La cartella delle immagini del gioco, nel modulo dell'interfaccia grafica.
	 */
	public static Path immagini() {
		return radice().resolve(MODULO_UI + "/src/main/resources/com/threeamigos/foresta/img");
	}

	/**
	 * Un file di risorse del motore, come nomi.txt.
	 */
	public static Path risorsaDelMotore(String nome) {
		return radice().resolve("foresta-motore/src/main/resources/com/threeamigos/foresta/motore/" + nome);
	}
}
