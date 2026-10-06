package com.threeamigos.foresta.eventi;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gli eventi che la UI riceve (notifiche, richieste, comandi e interni con una vista) portano solo viste in sola
 * lettura, identificativi e dati: se portassero Personaggio o Artefatto, la UI potrebbe chiamarne i metodi che
 * scrivono (vedi motore_di_gioco.md, "La separazione fra motore, modello dati e UI"). Qui si controlla, leggendo i sorgenti, che nessun
 * evento importi classi del motore o del dominio; le sole eccezioni sono quelle di {@link #ECCEZIONI}.
 */
class DipendenzeEventiTest {

	private static final Path EVENTI = Paths.get("src/main/java/com/threeamigos/foresta/eventi");

	private static final Pattern DOMINIO = Pattern.compile(
			"\\bcom\\.threeamigos\\.foresta\\.(personaggi|oggetti|missioni|motore|locazioni|trofei|offerte|incantesimi)\\.");

	/**
	 * Gli eventi interni al motore, che la UI non riceve (chi li crea e chi li consuma sta nel motore e nel dominio),
	 * più lo sniffer, che li scrive nel log. Quando uno smette di importare il dominio, va tolto da qui (il test lo
	 * pretende).
	 */
	private static final Set<String> ECCEZIONI = new TreeSet<>(Arrays.asList(
			"SnifferBusEventi.java",
			"interni/InternoAcquistoArtefatto.java",
			"interni/InternoCreazionePersonaggio.java",
			"interni/InternoIncantatura.java",
			"interni/InternoMissioneCompletata.java",
			"interni/InternoOggettoRaccolto.java",
			"interni/InternoPersonaggioArreso.java",
			"interni/InternoPrelievoArtefatto.java",
			"interni/InternoRisultatoValutazionePersonaggioAttaccante.java",
			"interni/InternoSpostamentoArtefatto.java",
			"interni/InternoStoccaggioArtefatto.java",
			"interni/InternoVenditaArtefatto.java"));

	@Test
	void gliEventiNonPortanoClassiDelDominio() throws IOException {
		Set<String> trovati = new TreeSet<>();
		Set<String> vietati = new TreeSet<>();
		for (Path file : sorgenti()) {
			String nome = EVENTI.relativize(file).toString().replace('\\', '/');
			if (!DOMINIO.matcher(codice(file)).find()) {
				continue;
			}
			trovati.add(nome);
			if (!ECCEZIONI.contains(nome)) {
				vietati.add(nome);
			}
		}
		assertTrue(vietati.isEmpty(), "Questi eventi importano classi del dominio o del motore:\n" + String.join("\n", vietati));
		assertEquals(ECCEZIONI, trovati, "Un'eccezione non serve più: toglierla da ECCEZIONI");
	}

	/**
	 * Gli eventi interni al motore non sono nelle cartelle che la UI riceve: un evento che importa il dominio e sta in
	 * notifiche, richieste o comandigiocatore è un errore anche se qualcuno lo mette fra le eccezioni.
	 */
	@Test
	void leEccezioniSonoTutteInterne() {
		for (String nome : ECCEZIONI) {
			assertTrue(nome.startsWith("interni/") || nome.equals("SnifferBusEventi.java"), nome);
		}
	}

	/**
	 * I comandi che la UI manda al motore portano solo dati e identificativi (uuid, id, tipi): il motore ritrova da sé
	 * gli oggetti veri e ignora il comando se non corrispondono a niente. Con una vista in mano, invece, dovrebbe
	 * fidarsi di quel che la UI gli passa.
	 */
	@Test
	void iComandiDellaUINonPortanoViste() throws IOException {
		Set<String> conViste = new TreeSet<>();
		for (Path file : sorgenti()) {
			String nome = EVENTI.relativize(file).toString().replace('\\', '/');
			if (nome.startsWith("comandigiocatore/") && codice(file).contains("com.threeamigos.foresta.interfacce.Vista")) {
				conViste.add(nome);
			}
		}
		assertTrue(conViste.isEmpty(), "Questi comandi portano viste invece di identificativi:\n" + String.join("\n", conViste));
	}

	private static String codice(Path file) throws IOException {
		return new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("//[^\\n]*", "");
	}

	private static List<Path> sorgenti() throws IOException {
		try (Stream<Path> percorsi = Files.walk(EVENTI)) {
			return percorsi.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}
}
