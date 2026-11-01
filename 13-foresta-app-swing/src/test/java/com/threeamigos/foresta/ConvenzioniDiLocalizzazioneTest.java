package com.threeamigos.foresta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La localizzazione dei testi (vedi {@code assessment_portabilita.md}, «Testi italiani nel codice»).
 * <ul>
 * <li>I file {@code .properties} di una lingua non hanno chiavi che l'italiano non ha, e portano gli stessi segnaposto
 * ({@code {0}}, {@code {1}}...): un segnaposto perso o inventato in una traduzione rompe la frase.</li>
 * <li>Le stringhe italiane che arrivano sulla UI e sono ancora nel codice non possono aumentare: il numero per modulo
 * sta in {@code localizzazione_baseline.properties}. Chi sposta testi nei file lo abbassa.</li>
 * </ul>
 */
class ConvenzioniDiLocalizzazioneTest {

	private static final Pattern SEGNAPOSTO = Pattern.compile("\\{\\d+}");
	private static final Pattern FILE_DI_UNA_LINGUA = Pattern.compile("(.+)_([a-z]{2}(?:_[A-Z]{2})?)\\.properties");

	@Test
	void ogniTraduzioneHaSoloChiaviEsistentiInItalianoEGliStessiSegnaposto() throws IOException {
		List<String> problemi = new ArrayList<>();
		for (Path file : filePropertiesDelleRisorse()) {
			Matcher m = FILE_DI_UNA_LINGUA.matcher(file.getFileName().toString());
			if (!m.matches() || m.group(2).equals("it")) {
				continue;
			}
			Path italiano = file.resolveSibling(m.group(1) + "_it.properties");
			if (!Files.exists(italiano)) {
				continue; // i file di prova di un test hanno le loro lingue
			}
			Properties it = leggi(italiano);
			Properties altra = leggi(file);
			for (String chiave : altra.stringPropertyNames()) {
				if (!it.containsKey(chiave)) {
					problemi.add(file.getFileName() + ": la chiave " + chiave + " non esiste in italiano");
				} else if (!segnaposto(it.getProperty(chiave)).equals(segnaposto(altra.getProperty(chiave)))) {
					problemi.add(file.getFileName() + ": segnaposto diversi in " + chiave);
				}
			}
		}
		assertTrue(problemi.isEmpty(), String.join("\n", problemi));
	}

	@Test
	void l_inglese_americano_ha_tutte_le_chiavi_dell_italiano() throws IOException {
		List<String> mancanti = new ArrayList<>();
		for (Path file : filePropertiesDelleRisorse()) {
			String nome = file.getFileName().toString();
			if (!nome.endsWith("_it.properties")) {
				continue;
			}
			Path inglese = file.resolveSibling(nome.replace("_it.properties", "_en.properties"));
			if (!Files.exists(inglese)) {
				mancanti.add(nome + ": manca il file _en");
				continue;
			}
			Set<String> chiavi = new TreeSet<>(leggi(file).stringPropertyNames());
			chiavi.removeAll(leggi(inglese).stringPropertyNames());
			if (!chiavi.isEmpty()) {
				mancanti.add(inglese.getFileName() + ": mancano " + chiavi);
			}
		}
		assertTrue(mancanti.isEmpty(), String.join("\n", mancanti));
	}

	@Test
	void leStringheItalianeNelCodiceNonAumentano() throws IOException {
		Properties baseline = new Properties();
		try (InputStream in = getClass().getResourceAsStream("localizzazione_baseline.properties")) {
			assertNotNull(in, "manca localizzazione_baseline.properties");
			baseline.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		Map<String, Integer> conteggio = contaLeStringheItaliane();
		List<String> saliti = new ArrayList<>();
		for (Map.Entry<String, Integer> e : conteggio.entrySet()) {
			int ammesse = Integer.parseInt(baseline.getProperty(e.getKey(), "0"));
			if (e.getValue() > ammesse) {
				saliti.add(e.getKey() + ": " + e.getValue() + " invece di " + ammesse);
			}
		}
		assertTrue(saliti.isEmpty(), "Stringhe italiane nel codice in più (i testi che arrivano sulla UI vanno nei file "
				+ "_it.properties). Conteggio attuale:\n" + conteggio.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("\n")) + "\nPiù di baseline: " + saliti);
	}

	@Test
	void ilRiconoscitoreDiStringheItalianeDistingueLeFrasiDagliIdentificativi() {
		assertTrue(italiana("Il goblin non ha più forza"));
		assertTrue(italiana("perché"));
		assertFalse(italiana("FORZA"));
		assertFalse(italiana("com.threeamigos.foresta"));
		assertFalse(italiana("%d"));
		assertFalse(italiana("Goblin"));
	}

	// ---- il contatore ----

	// Le sole parole che bastano a dire "è una frase italiana"; due di queste, oppure una lettera accentata
	private static final Set<String> PAROLE = new HashSet<>(Arrays.asList("il", "lo", "la", "gli", "le", "un", "una", "uno", "di", "del",
			"della", "dei", "delle", "che", "non", "per", "con", "nel", "nella", "sono", "hai", "ha", "ho", "è", "e", "ma", "se", "ti",
			"si", "mi", "al", "alla", "dal", "dalla", "tuo", "tua", "suo", "sua", "questo", "questa", "come", "più", "già", "anche",
			"ora", "qui", "dove", "quando", "tutti", "tutto", "sei", "siete", "vuoi", "puoi", "devi", "può", "da", "in", "su", "a"));
	private static final Pattern LETTERA_ACCENTATA = Pattern.compile("[àèéìòù]");
	private static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");
	// Messaggi per lo sviluppatore: non si traducono
	private static final Pattern RIGA_INTERNA = Pattern.compile("Logger\\.log|new \\w*Exception\\(|assert|@SuppressWarnings|@Deprecated");
	private static final String[] PERCORSI_INTERNI = {"/ui/sviluppo/", "StrumentiDiSviluppo", "DotazioneDiProva", "TrucchiDiSviluppo"};

	static boolean italiana(String testo) {
		if (LETTERA_ACCENTATA.matcher(testo.toLowerCase(java.util.Locale.ROOT)).find()) {
			return true;
		}
		int parole = 0;
		for (String parola : testo.toLowerCase(java.util.Locale.ROOT).split("[^\\p{L}]+")) {
			if (PAROLE.contains(parola) && ++parole >= 2) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, Integer> contaLeStringheItaliane() throws IOException {
		Map<String, Integer> perModulo = new TreeMap<>();
		for (Path file : SorgentiDelProgetto.sorgenti(false)) {
			String nome = SorgentiDelProgetto.radice().relativize(file).toString().replace('\\', '/');
			if (!nome.contains("/src/main/") || Arrays.stream(PERCORSI_INTERNI).anyMatch(nome::contains)) {
				continue;
			}
			String modulo = nome.substring(0, nome.indexOf('/'));
			String codice = new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
					.replaceAll("(?s)/\\*.*?\\*/", "");
			int trovate = 0;
			for (String riga : codice.split("\n")) {
				String senzaCommento = riga.replaceAll("^\\s*//.*", "");
				if (RIGA_INTERNA.matcher(senzaCommento).find()) {
					continue;
				}
				Matcher m = LITERAL.matcher(senzaCommento);
				while (m.find()) {
					if (italiana(m.group(1))) {
						trovate++;
					}
				}
			}
			if (trovate > 0) {
				perModulo.merge(modulo, trovate, Integer::sum);
			}
		}
		return perModulo;
	}

	// ---- i file ----

	private static Set<String> segnaposto(String testo) {
		Set<String> trovati = new TreeSet<>();
		Matcher m = SEGNAPOSTO.matcher(testo);
		while (m.find()) {
			trovati.add(m.group());
		}
		return trovati;
	}

	private static Properties leggi(Path file) throws IOException {
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(file)) {
			p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		return p;
	}

	private static List<Path> filePropertiesDelleRisorse() throws IOException {
		List<Path> file = new ArrayList<>();
		try (DirectoryStream<Path> moduli = Files.newDirectoryStream(SorgentiDelProgetto.radice(), "[0-9][0-9]-foresta-*")) {
			for (Path modulo : moduli) {
				Path risorse = modulo.resolve("src/main/resources");
				if (Files.isDirectory(risorse)) {
					try (Stream<Path> percorsi = Files.walk(risorse)) {
						file.addAll(percorsi.filter(p -> p.toString().endsWith(".properties")).collect(Collectors.toList()));
					}
				}
			}
		}
		return file;
	}
}
