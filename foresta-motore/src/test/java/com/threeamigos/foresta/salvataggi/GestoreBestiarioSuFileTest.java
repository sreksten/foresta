package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
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
 * Il file del bestiario, in una home temporanea al posto di quella vera. Come per i trofei (vedi GestoreTrofeiSuFileTest, che prova anche la
 * scrittura sostituendo, uguale per tutti i gestori su file).
 */
class GestoreBestiarioSuFileTest {

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
	void senzaFileNessunaVoceESbloccata() {
		BestiarioMD bestiario = new BestiarioMD();

		assertTrue(new GestoreBestiarioSuFile().carica(bestiario), "un file assente non è un errore");

		assertTrue(bestiario.getSbloccate().isEmpty());
	}

	@Test
	void salvaNelFileBestiarioDellaCartellaForestaERilegge() throws IOException {
		BestiarioMD bestiario = new BestiarioMD();
		bestiario.sblocca(TipoPersonaggio.MAGA);
		bestiario.scopri(TipoPersonaggio.SCHELETRO, "INCANTESIMO:MORTE");

		assertTrue(new GestoreBestiarioSuFile().salva(bestiario));

		List<String> righe = Files.readAllLines(home.resolve(".foresta").resolve("bestiario"), StandardCharsets.UTF_8);
		assertTrue(righe.contains("MAGO|true|"), righe.toString());
		assertTrue(righe.contains("SCHELETRO|false|INCANTESIMO:MORTE"), righe.toString());
		BestiarioMD riletto = new BestiarioMD();
		assertTrue(new GestoreBestiarioSuFile().carica(riletto));
		assertTrue(riletto.isSbloccata(TipoPersonaggio.MAGO));
		assertTrue(riletto.isScoperta(TipoPersonaggio.SCHELETRO, "INCANTESIMO:MORTE"));
	}

	@Test
	void unaClasseChePiuNonEsisteNonImpedisceDiLeggereLeAltre() throws IOException {
		Path cartella = home.resolve("bestiario");
		Files.createDirectories(cartella);
		Files.write(cartella.resolve("bestiario"), Arrays.asList("MOSTRO_RINOMINATO|true|DANNO:FUOCO", "GOBLIN|true|"), StandardCharsets.UTF_8);

		BestiarioMD bestiario = new BestiarioMD();
		assertTrue(new GestoreBestiarioSuFile(cartella.toFile()).carica(bestiario));

		assertTrue(bestiario.isSbloccata(TipoPersonaggio.GOBLIN));
	}

	@Test
	void unFileRovinatoSiMetteDaParteENonSiSovrascrive() throws IOException {
		Path cartella = home.resolve("bestiario");
		Files.createDirectories(cartella);
		// La seconda riga non ha il campo che dice se la voce è sbloccata
		Files.write(cartella.resolve("bestiario"), Arrays.asList("GOBLIN|true|", "TROLL"), StandardCharsets.UTF_8);
		GestoreBestiarioSuFile gestore = new GestoreBestiarioSuFile(cartella.toFile());

		assertFalse(gestore.carica(new BestiarioMD()));

		assertFalse(Files.exists(cartella.resolve("bestiario")));
		assertTrue(Files.readAllLines(cartella.resolve("bestiario.rovinato"), StandardCharsets.UTF_8).contains("GOBLIN|true|"));
		assertTrue(gestore.salva(new BestiarioMD()));
		assertTrue(Files.readAllLines(cartella.resolve("bestiario.rovinato"), StandardCharsets.UTF_8).contains("GOBLIN|true|"));
	}
}
