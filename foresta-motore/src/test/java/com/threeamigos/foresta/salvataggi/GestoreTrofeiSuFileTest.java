package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.modellodati.TrofeiMD;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il file dei trofei, in una home temporanea al posto di quella vera.
 */
class GestoreTrofeiSuFileTest {

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
	void senzaFileNessunTrofeoEVinto() {
		TrofeiMD trofei = new TrofeiMD();
		assertTrue(new GestoreTrofeiSuFile().carica(trofei), "un file assente non e' un errore");
		assertFalse(trofei.isVinto(TipoTrofeo.SBEVAZZONE));
	}

	@Test
	void salvaNelFileTrofeiDellaCartellaForestaERilegge() throws IOException {
		// Given
		TrofeiMD trofei = new TrofeiMD();
		trofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);

		// When
		assertTrue(new GestoreTrofeiSuFile().salva(trofei));

		// Then
		Path file = home.resolve(".foresta").resolve("trofei");
		List<String> righe = Files.readAllLines(file, StandardCharsets.UTF_8);
		assertEquals(TipoTrofeo.values().length, righe.size(), "una riga per trofeo");
		assertTrue(righe.contains("SBEVAZZONE|false|1"), righe.toString());
		TrofeiMD riletti = new TrofeiMD();
		assertTrue(new GestoreTrofeiSuFile().carica(riletti));
		assertEquals(1, riletti.getProgresso(TipoTrofeo.SBEVAZZONE));
	}

	@Test
	void conUnaCartellaIndicataScriveLiESenzaToccareLaHome() throws IOException {
		// Given
		Path cartella = home.resolve("spazio-privato-dell-app");
		TrofeiMD trofei = new TrofeiMD();
		trofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 2);

		// When
		assertTrue(new GestoreTrofeiSuFile(cartella.toFile()).salva(trofei));

		// Then
		assertTrue(Files.isRegularFile(cartella.resolve("trofei")), "la cartella indicata viene creata e usata");
		assertFalse(Files.exists(home.resolve(".foresta")), "la cartella dell'utente non c'entra");
		TrofeiMD riletti = new TrofeiMD();
		assertTrue(new GestoreTrofeiSuFile(cartella.toFile()).carica(riletti));
		assertEquals(2, riletti.getProgresso(TipoTrofeo.SBEVAZZONE));
	}
}
