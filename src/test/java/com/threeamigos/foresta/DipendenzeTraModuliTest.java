package com.threeamigos.foresta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I tre moduli in cui il progetto si divide (oggi le classi stanno ancora in un solo modulo Maven, ma i confini sono
 * già questi):
 * <ul>
 * <li><b>modello</b>: tipi, modellodati, interfacce, eventi e le poche classi di strumenti e intermezzi che la UI
 * usa. Non conosce né il motore né la UI.</li>
 * <li><b>motore</b>: tutto il resto, escluse la UI e Main. Conosce solo il modello.</li>
 * <li><b>swing</b>: la UI e gli eventi che portano sprite, che crea e riceve solo lei. Conosce solo il modello.</li>
 * <li><b>applicazione</b>: Main, che assembla le parti, e lo sniffer del bus, che scrive nel log anche gli eventi
 * della UI e quelli del motore. Conoscono tutti gli altri moduli.</li>
 * </ul>
 * Il test legge i sorgenti e confronta ogni dipendenza (import, nome completo, nome semplice nello stesso pacchetto)
 * con questi confini.
 */
class DipendenzeTraModuliTest {

	private static final Path SORGENTI = Paths.get("src/main/java/com/threeamigos/foresta");
	private static final String BASE = "com.threeamigos.foresta.";

	private enum Modulo {MODELLO, MOTORE, SWING, APPLICAZIONE}

	private static final Set<String> PACCHETTI_DEL_MODELLO = new HashSet<>(Arrays.asList(
			"tipi", "modellodati", "interfacce", "eventi"));

	/**
	 * Classi di strumenti e di intermezzi che la UI usa e che quindi stanno nel modello.
	 */
	private static final Set<String> CLASSI_NEL_MODELLO = new HashSet<>(Arrays.asList(
			"strumenti.Logger", "strumenti.Misc", "strumenti.Punteggio", "strumenti.Temporizzabile",
			"strumenti.Temporizzatore", "strumenti.TestataSalvataggio", "strumenti.ThreadPrincipale",
			"intermezzi.Animazione", "intermezzi.BattutaIntermezzo", "intermezzi.BattutaProgrammata",
			"intermezzi.ElementoIntermezzo", "intermezzi.ImmagineIntermezzo", "intermezzi.PaginaIntermezzo",
			"intermezzi.Ripetizione", "intermezzi.StatoElemento", "intermezzi.Tappa", "intermezzi.TipoStiramento",
			"intermezzi.Verso"));

	/**
	 * Eventi che portano sprite e fumetti: li crea e li riceve solo la UI, e stanno con lei.
	 */
	private static final Set<String> EVENTI_DELLA_UI = new HashSet<>(Arrays.asList(
			"eventi.interni.InternoCreazioneSpriteATempo", "eventi.interni.InternoCreazioneSpriteAnnuncioGlobale",
			"eventi.interni.InternoCreazioneSpriteEffettoDiStato", "eventi.interni.InternoCreazioneSpriteFumettoATempo",
			"eventi.interni.InternoCreazioneSpriteInDissolvenza", "eventi.interni.InternoNotificaViaFumettoATempo"));

	/**
	 * Eventi interni al motore, che portano classi del dominio (vedi DipendenzeEventiTest): stanno nel motore anche se
	 * il loro pacchetto è quello degli eventi.
	 */
	private static final Set<String> EVENTI_DEL_MOTORE = new HashSet<>(Arrays.asList(
			"eventi.interni.InternoAcquistoArtefatto",
			"eventi.interni.InternoCreazionePersonaggio", "eventi.interni.InternoIncantatura",
			"eventi.interni.InternoMissioneCompletata", "eventi.interni.InternoOggettoRaccolto",
			"eventi.interni.InternoPersonaggioArreso", "eventi.interni.InternoPrelievoArtefatto",
			"eventi.interni.InternoSpostamentoArtefatto", "eventi.interni.InternoStoccaggioArtefatto",
			"eventi.interni.InternoVenditaArtefatto"));

	private static Modulo modulo(String classe) {
		String pacchetto = classe.substring(0, classe.indexOf('.') < 0 ? classe.length() : classe.indexOf('.'));
		if (classe.equals("Main") || classe.equals("SnifferBusEventi")) {
			return Modulo.APPLICAZIONE;
		}
		if (pacchetto.equals("ui") || EVENTI_DELLA_UI.contains(classe)) {
			return Modulo.SWING;
		}
		if (EVENTI_DEL_MOTORE.contains(classe)) {
			return Modulo.MOTORE;
		}
		if (PACCHETTI_DEL_MODELLO.contains(pacchetto) || CLASSI_NEL_MODELLO.contains(classe)) {
			return Modulo.MODELLO;
		}
		return Modulo.MOTORE;
	}

	@Test
	void ogniClasseConoscePerNomeSoloIModuliChePuo() throws IOException {
		Map<String, Path> classi = new HashMap<>();
		Map<String, List<String>> perNomeSemplice = new HashMap<>();
		try (Stream<Path> percorsi = Files.walk(SORGENTI)) {
			for (Path file : percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList())) {
				String classe = SORGENTI.relativize(file).toString().replace('\\', '/').replace('/', '.');
				classe = classe.substring(0, classe.length() - ".java".length());
				classi.put(classe, file);
				perNomeSemplice.computeIfAbsent(classe.substring(classe.lastIndexOf('.') + 1), k -> new java.util.ArrayList<>())
						.add(classe);
			}
		}
		Set<String> vietate = new TreeSet<>();
		for (Map.Entry<String, Path> entry : classi.entrySet()) {
			String classe = entry.getKey();
			for (String usata : dipendenze(classe, entry.getValue(), classi, perNomeSemplice)) {
				if (!puoUsare(modulo(classe), modulo(usata))) {
					vietate.add(modulo(classe) + " " + classe + " -> " + modulo(usata) + " " + usata);
				}
			}
		}
		assertTrue(vietate.isEmpty(), "Dipendenze che violano i confini fra i moduli:\n" + String.join("\n", vietate));
	}

	private static boolean puoUsare(Modulo chi, Modulo cosa) {
		return chi == cosa || cosa == Modulo.MODELLO || chi == Modulo.APPLICAZIONE;
	}

	private static Set<String> dipendenze(String classe, Path file, Map<String, Path> classi,
										  Map<String, List<String>> perNomeSemplice) throws IOException {
		String codice = new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("//[^\\n]*", "")
				.replaceAll("\"(\\\\.|[^\"\\\\])*\"", "\"\"");
		String pacchetto = classe.contains(".") ? classe.substring(0, classe.lastIndexOf('.')) : "";
		Map<String, String> importate = new HashMap<>();
		Set<String> pacchettiImportati = new HashSet<>();
		Matcher importazioni = Pattern.compile("^import\\s+(?:static\\s+)?" + Pattern.quote(BASE) + "([\\w.]+?)(\\.\\*)?;",
				Pattern.MULTILINE).matcher(codice);
		while (importazioni.find()) {
			String nome = importazioni.group(1);
			if (importazioni.group(2) != null) {
				pacchettiImportati.add(nome);
				continue;
			}
			String corrente = risolvi(nome, classi);
			if (corrente != null) {
				importate.put(corrente.substring(corrente.lastIndexOf('.') + 1), corrente);
			}
		}
		Set<String> usate = new HashSet<>(importate.values());
		Matcher completi = Pattern.compile(Pattern.quote(BASE) + "([\\w.]+)").matcher(codice);
		while (completi.find()) {
			String corrente = risolvi(completi.group(1), classi);
			if (corrente != null) {
				usate.add(corrente);
			}
		}
		Matcher identificatori = Pattern.compile("\\b[A-Z]\\w*\\b").matcher(codice);
		Set<String> nomi = new HashSet<>();
		while (identificatori.find()) {
			nomi.add(identificatori.group());
		}
		for (String nome : nomi) {
			if (importate.containsKey(nome)) {
				continue;
			}
			for (String candidata : perNomeSemplice.getOrDefault(nome, java.util.Collections.emptyList())) {
				String pacchettoCandidata = candidata.contains(".") ? candidata.substring(0, candidata.lastIndexOf('.')) : "";
				if (pacchettoCandidata.equals(pacchetto) || pacchettiImportati.contains(pacchettoCandidata)) {
					usate.add(candidata);
				}
			}
		}
		usate.remove(classe);
		return usate;
	}

	/**
	 * La classe di primo livello a cui punta un nome completo, anche se finisce con una classe annidata o un membro.
	 */
	private static String risolvi(String nome, Map<String, Path> classi) {
		String[] parti = nome.split("\\.");
		for (int i = parti.length; i > 0; i--) {
			String candidata = String.join(".", Arrays.copyOf(parti, i));
			if (classi.containsKey(candidata)) {
				return candidata;
			}
		}
		return null;
	}
}
