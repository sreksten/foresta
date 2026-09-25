package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La partita come un tutto: stesso seme, stesso mondo (mappa e magazzini); salvataggio e ricaricamento.
 */
class ScenarioPartitaTest {

	@Test
	void conLoStessoSemeLaForestaEUguale() {
		String prima = forestaCostruita(99);
		String seconda = forestaCostruita(99);
		assertEquals(prima, seconda);
	}

	@Test
	void conLoStessoSemeIMagazziniSonoUguali() {
		for (long seme = 1; seme <= 5; seme++) {
			assertEquals(magazzini(seme), magazzini(seme), "seme " + seme);
		}
	}

	@Test
	void unaPartitaSalvataSiRilegge() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			partita.comando(Comando.ESCI_DA_CITTA);
			partita.assertStato(Stato.SCELTA_DIREZIONE);
			int monete = partita.gruppo().getMonete();
			CoordinateMD dove = partita.gruppo().getCoordinate();

			partita.comando(Comando.FLOPPY).comando(Comando.NUMERO_1);
			assertTrue(partita.salvataggi().contiene(Comando.NUMERO_1));

			// Si spende e ci si sposta, poi si ricarica: tutto torna com'era al salvataggio
			partita.gruppo().subMonete(50);
			partita.gruppo().setCoordinate(new CoordinateMD(0, 0));
			assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_1));

			assertEquals(monete, partita.gruppo().getMonete());
			assertEquals(dove.getX(), partita.gruppo().getX());
			assertEquals(dove.getY(), partita.gruppo().getY());
			assertEquals("Arsenio", partita.gruppo().getCapo().getNomeProprio().orElse(null));
		}
	}

	@Test
	void gliIntermezziSiFermanoSeNonSiSaltano() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(3)) {
			partita.nonSaltareIntermezzi();
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_RUUNA));
			partita.assertStato(Stato.INTERMEZZO);
			partita.assertComandoDisponibile(Comando.PERGAMENA);

			partita.saltaIntermezzi();
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.assertComandoDisponibile(Comando.ARMAIOLO);
		}
	}

	/**
	 * Nome e costo degli artefatti di ogni negozio di ogni città appena costruiti con quel seme, più il lancio
	 * successivo: se il generatore facesse estrazioni che non seguono il seme, anche quello cambierebbe.
	 */
	private static String magazzini(long seme) {
		try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
			partita.comando(Comando.PERGAMENA).testo("Arsenio");
			StringBuilder magazzini = new StringBuilder();
			for (ClassiLocazione citta : ClassiLocazione.values()) {
				if (citta.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
					for (TipoNegozio negozio : TipoNegozio.values()) {
						RegistroArtefatti.getScambiatorePerNegozio(Foresta.getCoordinateLocazioneUnica(citta), negozio)
								.getInventario()
								.forEach(a -> magazzini.append(a.getModelloDati().getNome()).append('/')
										.append(a.getCostoAcquisto()).append(';'));
						magazzini.append('\n');
					}
				}
			}
			return magazzini.append(Dado.tira(1, 1_000_000)).toString();
		}
	}

	/**
	 * Le locazioni di ogni casella della Foresta appena costruita con quel seme.
	 */
	private static String forestaCostruita(long seme) {
		try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
			StringWriter mappa = new StringWriter();
			PrintWriter scrittore = new PrintWriter(mappa);
			partita.comando(Comando.PERGAMENA).testo("Arsenio");
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				for (int x = 0; x < Foresta.getDimensioneX(); x++) {
					scrittore.print(Foresta.getLocazione(x, y).ordinal());
					scrittore.print(',');
				}
				scrittore.println();
			}
			scrittore.print(partita.gruppo().getX() + "/" + partita.gruppo().getY());
			scrittore.flush();
			return mappa.toString();
		}
	}
}
