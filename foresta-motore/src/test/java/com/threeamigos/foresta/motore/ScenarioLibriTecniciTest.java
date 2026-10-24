package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.oggetti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I libri tecnici nella partita vera, con l'Automa: il capo li legge alla fine della locazione in cui li ha trovati, e poi
 * l'alchimista e l'incantatore ne parlano la prima volta che il gruppo entra da loro.
 */
class ScenarioLibriTecniciTest {

	private static final String UNIX = "MANUALE_UNIX_SYSTEM_V";
	private static final String K_AND_R = "THE_C_PROGRAMMING_LANGUAGE";

	private static void trovato(String chiave) {
		IntermezzoLibroTecnico.trovato(LibroTecnico.getLibro(chiave).orElseThrow(AssertionError::new));
	}

	private static String battute(PartitaDiTest partita) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.map(b -> b.getBattuta().getTesto())
				.collect(Collectors.joining(" | "));
	}

	private static PartitaDiTest inCitta(PartitaDiTest partita) {
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		RegistroIntermezzi.segnaScattato(FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE));
		partita.nonSaltareIntermezzi();
		return partita;
	}

	/**
	 * Alla fine della locazione in cui l'ha trovato, il capo legge il libro.
	 */
	@Test
	void ilCapoLeggeIlLibroAFineLocazione() {
		for (String chiave : new String[] {K_AND_R, UNIX}) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
				inCitta(partita);
				trovato(chiave);
				partita.comando(Comando.ESCI_DA_CITTA);
				partita.assertStato(Stato.INTERMEZZO);
				assertTrue(battute(partita).contains(chiave.equals(K_AND_R) ? "The C Programming Language" : "Manuale di Unix System V"),
						battute(partita));
				partita.saltaIntermezzi();
				partita.assertStato(Stato.SCELTA_DIREZIONE);
			}
		}
	}

	/**
	 * Letto il libro (anche in una locazione precedente), la prima volta dal negoziante giusto parte la sua scena.
	 */
	@Test
	void l_incantatoreSpiegaIlLibroSulC() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			inCitta(partita);
			letto(K_AND_R);

			partita.comando(Comando.NEGOZIO_INCANTATORE);
			partita.assertStato(Stato.INTERMEZZO);
			assertTrue(battute(partita).contains("Core Dump"), battute(partita));
		}
	}

	@Test
	void l_alchimistaRivelaLaSpadaDelManuale() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			inCitta(partita);
			letto(UNIX);

			partita.comando(Comando.NEGOZIO_ALCHIMISTA);
			partita.assertStato(Stato.INTERMEZZO);
			assertTrue(battute(partita).contains("Permette di vedere le cose nascoste."), battute(partita));
		}
	}

	/**
	 * Entrambi i libri, trovati e letti in locazioni diverse: ognuno ha la sua scena, nel suo negozio.
	 */
	@Test
	void ognunoHaLaSuaScenaNelSuoNegozio() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			inCitta(partita);
			letto(UNIX);
			letto(K_AND_R);

			partita.comando(Comando.NEGOZIO_ALCHIMISTA);
			partita.assertStato(Stato.INTERMEZZO);
			assertTrue(battute(partita).contains("Permette di vedere le cose nascoste."), battute(partita));
			partita.saltaIntermezzi();

			partita.comando(Comando.NEGOZIO_INCANTATORE);
			partita.assertStato(Stato.INTERMEZZO);
			assertTrue(battute(partita).contains("Core Dump"), battute(partita));
		}
	}

	/**
	 * Come dopo la lettura del capo a fine locazione: il libro è trovato e letto.
	 */
	private static void letto(String chiave) {
		trovato(chiave);
		Intermezzo lettura =
				FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO);
		RegistroIntermezzi.segnaScattato(lettura);
		lettura.getPagine();
	}
}
