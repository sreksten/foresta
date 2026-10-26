package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le descrizioni dei comandi, lette da comandi_it.properties.
 */
class ComandoTest {

	// I comandi che non hanno una descrizione: gli slot numerici, le frecce di scorrimento, il timer e i comandi segreti
	private static final Set<Comando> SENZA_DESCRIZIONE = new HashSet<>(Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2,
			Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5, Comando.SALVATAGGIO_AUTOMATICO, Comando.SU, Comando.GIU,
			Comando.DESTRA, Comando.SINISTRA, Comando.TIMER, Comando.RUTTOLOMEO, Comando.STORPSGORBLIN));

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ogniComandoHaUnaDescrizioneTranneQuelliCheNonLaVogliono() {
		for (Comando comando : Comando.values()) {
			if (SENZA_DESCRIZIONE.contains(comando)) {
				assertNull(comando.getDescrizione(), comando + " non ha una descrizione");
			} else {
				String descrizione = comando.getDescrizione();
				assertTrue(descrizione != null && !descrizione.trim().isEmpty(), "manca la descrizione di " + comando);
			}
		}
	}

	@Test
	void laDescrizioneSiLeggeComeScrittaNelFile() {
		assertEquals("Salva la partita", Comando.FLOPPY_SALVA.getDescrizione());
		assertEquals("Consuma una pozione dell'Aumento di Livello", Comando.POZIONE_AUMENTO_LIVELLO.getDescrizione());
		assertEquals("Fugge a gambe levate", Comando.FUGA.getDescrizione());
	}

	@Test
	void unaLinguaSenzaFileRipiegaSuItaliano() {
		Testi.impostaLingua("xx");

		assertEquals("Salva la partita", Comando.FLOPPY_SALVA.getDescrizione());
		assertNull(Comando.TIMER.getDescrizione());
	}

	@Test
	void ilFileNonHaVociPerComandiCheNonEsistonoONulle() throws IOException {
		Properties descrizioni = new Properties();
		try (InputStream flusso = Testi.class.getResourceAsStream("comandi_it.properties")) {
			descrizioni.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
		}
		for (String chiave : descrizioni.stringPropertyNames()) {
			Comando comando = Comando.valueOf(chiave);
			assertFalse(SENZA_DESCRIZIONE.contains(comando), "il file ha una voce per " + chiave + ", che dovrebbe esserne senza");
			assertFalse(descrizioni.getProperty(chiave).trim().isEmpty(), "descrizione vuota: " + chiave);
		}
	}

	@Test
	void unComandoSiRitrovaDallaSuaPosizione() {
		for (Comando comando : Comando.values()) {
			assertSame(comando, Comando.of(comando.ordinal()));
		}
		assertThrows(IllegalArgumentException.class, () -> Comando.of(-1));
		assertThrows(IllegalArgumentException.class, () -> Comando.of(Comando.values().length));
	}

	@Test
	void iComandiDeiPersonaggiSonoOttoInFila() {
		Comando[] personaggi = {Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.PERSONAGGIO_3, Comando.PERSONAGGIO_4,
				Comando.PERSONAGGIO_5, Comando.PERSONAGGIO_6, Comando.PERSONAGGIO_7, Comando.PERSONAGGIO_8};
		for (int i = 0; i < personaggi.length; i++) {
			assertSame(personaggi[i], Comando.ofPersonaggio(i));
			assertEquals(i, personaggi[i].getIndicePersonaggio(), personaggi[i].name());
		}
		assertThrows(IllegalArgumentException.class, () -> Comando.ofPersonaggio(-1));
		assertThrows(IllegalArgumentException.class, () -> Comando.ofPersonaggio(8));

		Set<Comando> deiPersonaggi = new HashSet<>(Arrays.asList(personaggi));
		for (Comando comando : Comando.values()) {
			assertEquals(deiPersonaggi.contains(comando), comando.isPersonaggio(), comando.name());
		}
	}
}
