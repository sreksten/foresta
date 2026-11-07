package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.modellodati.SuggerimentiMD;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il file del tutorial, in una home temporanea al posto di quella vera. Come per il bestiario (vedi GestoreBestiarioSuFileTest).
 */
class GestoreSuggerimentiSuFileTest {

	@TempDir
	Path home;

	private String homePrecedente;

	@BeforeEach
	void spostaLaHome() {
		homePrecedente = System.getProperty("user.home");
		System.setProperty("user.home", home.toString());
	}

	@AfterEach
	void ripristinaLaHome() {
		System.setProperty("user.home", homePrecedente);
	}

	@Test
	void senzaFileIlTutorialEAccesoESenzaSuggerimentiVisti() {
		SuggerimentiMD tutorial = new SuggerimentiMD();

		assertTrue(new GestoreSuggerimentiSuFile().carica(tutorial), "un file assente non è un errore");

		assertTrue(tutorial.isAttivo());
		assertTrue(tutorial.getVisti().isEmpty());
	}

	@Test
	void salvaNelFileTutorialDellaCartellaForestaERilegge() throws IOException {
		SuggerimentiMD tutorial = new SuggerimentiMD();
		tutorial.setAttivo(false);
		tutorial.segnaVisto(TipoSuggerimento.COMBATTIMENTO);
		tutorial.segnaVisto(TipoSuggerimento.PRIMI_PASSI);

		assertTrue(new GestoreSuggerimentiSuFile().salva(tutorial));

		List<String> righe = Files.readAllLines(home.resolve(".foresta").resolve("tutorial"), StandardCharsets.UTF_8);
		assertTrue(righe.contains("false"), righe.toString());
		assertTrue(righe.contains("PRIMI_PASSI") && righe.contains("COMBATTIMENTO"), righe.toString());
		SuggerimentiMD riletto = new SuggerimentiMD();
		assertTrue(new GestoreSuggerimentiSuFile().carica(riletto));
		assertFalse(riletto.isAttivo());
		assertTrue(riletto.isVisto(TipoSuggerimento.COMBATTIMENTO));
		assertTrue(riletto.isVisto(TipoSuggerimento.PRIMI_PASSI));
		assertFalse(riletto.isVisto(TipoSuggerimento.POZIONI));
	}

	@Test
	void unSuggerimentoChePiuNonEsisteNonImpedisceDiLeggereGliAltri() throws IOException {
		Path cartella = home.resolve("tutorial");
		Files.createDirectories(cartella);
		Files.write(cartella.resolve("tutorial"), Arrays.asList("true", "SUGGERIMENTO_RINOMINATO", "MISSIONI"), StandardCharsets.UTF_8);

		SuggerimentiMD tutorial = new SuggerimentiMD();
		assertTrue(new GestoreSuggerimentiSuFile(cartella.toFile()).carica(tutorial));

		assertTrue(tutorial.isVisto(TipoSuggerimento.MISSIONI));
	}

	@Test
	void unFileRovinatoSiMetteDaParteENonSiSovrascrive() throws IOException {
		Path cartella = home.resolve("tutorial");
		Files.createDirectories(cartella);
		// La prima riga non dice se il tutorial è acceso
		Files.write(cartella.resolve("tutorial"), Arrays.asList("forse", "MISSIONI"), StandardCharsets.UTF_8);
		GestoreSuggerimentiSuFile gestore = new GestoreSuggerimentiSuFile(cartella.toFile());

		assertFalse(gestore.carica(new SuggerimentiMD()));

		assertFalse(Files.exists(cartella.resolve("tutorial")));
		assertTrue(Files.readAllLines(cartella.resolve("tutorial.rovinato"), StandardCharsets.UTF_8).contains("MISSIONI"));
		assertTrue(gestore.salva(new SuggerimentiMD()));
		assertTrue(Files.readAllLines(cartella.resolve("tutorial.rovinato"), StandardCharsets.UTF_8).contains("MISSIONI"));
	}
}
