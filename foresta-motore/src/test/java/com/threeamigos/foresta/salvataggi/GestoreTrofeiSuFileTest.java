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

	@Test
	void unTrofeoChePiuNonEsisteNonImpedisceDiLeggereGliAltri() throws IOException {
		Path cartella = home.resolve("trofei");
		Files.createDirectories(cartella);
		Files.write(cartella.resolve("trofei"), java.util.Arrays.asList("TROFEO_RINOMINATO|true|5", "SBEVAZZONE|true|3"), StandardCharsets.UTF_8);

		TrofeiMD trofei = new TrofeiMD();
		assertTrue(new GestoreTrofeiSuFile(cartella.toFile()).carica(trofei));

		assertTrue(trofei.isVinto(TipoTrofeo.SBEVAZZONE));
		assertEquals(3, trofei.getProgresso(TipoTrofeo.SBEVAZZONE));
	}

	@Test
	void unFileRovinatoSiMetteDaParteENonSiSovrascrive() throws IOException {
		Path cartella = home.resolve("trofei");
		Files.createDirectories(cartella);
		Files.write(cartella.resolve("trofei"), java.util.Arrays.asList("SBEVAZZONE|true|3", "RIGATTIERE|true|non un numero"), StandardCharsets.UTF_8);
		GestoreTrofeiSuFile gestore = new GestoreTrofeiSuFile(cartella.toFile());

		assertFalse(gestore.carica(new TrofeiMD()));

		// Il file rovinato resta in un'altra posizione, e la scrittura che segue non lo tocca
		assertFalse(Files.exists(cartella.resolve("trofei")));
		assertTrue(Files.readAllLines(cartella.resolve("trofei.rovinato"), StandardCharsets.UTF_8).contains("SBEVAZZONE|true|3"));
		assertTrue(gestore.salva(new TrofeiMD()));
		assertTrue(Files.readAllLines(cartella.resolve("trofei.rovinato"), StandardCharsets.UTF_8).contains("SBEVAZZONE|true|3"));
	}

	@Test
	void unaScritturaCheFallisceAMetaLasciaIlFileDiPrima() throws IOException {
		Path file = home.resolve("trofei");
		Files.write(file, java.util.Collections.singletonList("vecchio"), StandardCharsets.UTF_8);

		assertThrows(IOException.class, () -> GestoreSuFile.scriviSostituendo(file.toFile(), writer -> {
			writer.println("nuovo, ma incompleto");
			throw new IOException("disco pieno");
		}));

		assertEquals(java.util.Collections.singletonList("vecchio"), Files.readAllLines(file, StandardCharsets.UTF_8));
		assertFalse(Files.exists(home.resolve("trofei.tmp")), "il file temporaneo non resta");
	}

	@Test
	void unaScritturaRiuscitaSostituisceIlFileESenzaTemporanei() throws IOException {
		Path file = home.resolve("trofei");
		Files.write(file, java.util.Collections.singletonList("vecchio"), StandardCharsets.UTF_8);

		GestoreSuFile.scriviSostituendo(file.toFile(), writer -> writer.println("nuovo"));

		assertEquals(java.util.Collections.singletonList("nuovo"), Files.readAllLines(file, StandardCharsets.UTF_8));
		assertFalse(Files.exists(home.resolve("trofei.tmp")));
	}

	@Test
	void seLaSostituzioneFallisceIlFileDiPrimaTornaAlSuoPostoESenzaBak() throws IOException {
		Path file = home.resolve("trofei");
		Files.write(file, java.util.Collections.singletonList("vecchio"), StandardCharsets.UTF_8);
		// Il temporaneo non esiste: la rinomina fallisce dopo che il vecchio file è stato messo da parte
		assertThrows(IOException.class, () -> GestoreSuFile.sostituisci(home.resolve("trofei.tmp").toFile(), file.toFile()));

		assertEquals(java.util.Collections.singletonList("vecchio"), Files.readAllLines(file, StandardCharsets.UTF_8));
		assertFalse(Files.exists(home.resolve("trofei.bak")), "il vecchio file non resta da parte");
	}
}
