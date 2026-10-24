package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.salvataggi.GestoreSalvataggiSuFile;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I salvataggi su file, in una cartella temporanea indicata al gestore: un salvataggio si può scrivere sopra un altro
 * dello stesso slot e non lascia il file temporaneo. Non rilegge il salvataggio (lo farebbe con il modello dati
 * statico, lasciando la partita a metà per i test che seguono): la rilettura la prova IdPartitaTest.
 */
class GestoreSalvataggiSuFileTest {

	@TempDir
	File cartella;

	@Test
	void salvareDueVolteNelloStessoSlotSostituisceIlFileESenzaResidui() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(131)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella);

			assertTrue(gestore.salva(Comando.NUMERO_3), "il primo salvataggio crea il file");
			assertTrue(gestore.salva(Comando.NUMERO_3), "il secondo sostituisce quello che c'e' gia'");

			assertEquals(Collections.singletonList("NUMERO_3.TXT"), Arrays.asList(cartella.list()), "nessun file temporaneo rimasto");
			List<TestataSalvataggio> salvataggi = gestore.getSalvataggiDisponibili();
			assertEquals(1, salvataggi.size());
		}
	}

	@Test
	void ilSalvataggioAutomaticoNonCompareNellElencoESiPuoCancellare() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(132)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			GestoreSalvataggiSuFile gestore = new GestoreSalvataggiSuFile(cartella);

			assertTrue(gestore.salva(Comando.SALVATAGGIO_AUTOMATICO));
			assertTrue(new File(cartella, "SALVATAGGIO_AUTOMATICO.TXT").isFile());
			assertTrue(gestore.getSalvataggiDisponibili().isEmpty(), "non e' uno degli slot del giocatore");

			gestore.elimina(Comando.SALVATAGGIO_AUTOMATICO);
			assertFalse(new File(cartella, "SALVATAGGIO_AUTOMATICO.TXT").exists());
			gestore.elimina(Comando.SALVATAGGIO_AUTOMATICO);
		}
	}
}
