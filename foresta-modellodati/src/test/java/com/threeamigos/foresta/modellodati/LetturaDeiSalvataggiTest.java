package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tutte le righe dei salvataggi si leggono con {@link LettoreCampi}: un salvataggio troncato o rovinato dà sempre un
 * {@link IOException} che dice cosa manca, mai una NullPointerException o un indice fuori dai limiti.
 */
class LetturaDeiSalvataggiTest {

	@Test
	void isottocampiSiDividonoEUltimoTieneIlResto() throws IOException {
		LettoreCampi campi = new LettoreCampi("FORZA§12|A§B§C§D§E§F§nota con § dentro|solo");

		assertArrayEquals(new String[]{"FORZA", "12"}, campi.sottocampi(2, 2));
		assertArrayEquals(new String[]{"A", "B", "C", "D", "E", "F", "nota con § dentro"}, campi.sottocampi(6, 7));
		assertArrayEquals(new String[]{"solo"}, campi.sottocampi(1, 2));
	}

	@Test
	void unCampoCompostoConTroppiPochiSottocampiVieneRifiutato() throws IOException {
		LettoreCampi campi = new LettoreCampi("FORZA");

		IOException errore = assertThrows(IOException.class, () -> campi.sottocampi(2, 2));
		assertTrue(errore.getMessage().contains("FORZA"), errore.getMessage());
	}

	@Test
	void restoRestituisceTuttiICampiCheRimangonoAncheConIlSeparatore() throws IOException {
		LettoreCampi campi = new LettoreCampi("uno|due|tre|quattro");
		campi.testo();
		campi.testo();

		assertEquals("tre|quattro", campi.resto());
		assertFalse(campi.haAltriCampi());
		assertEquals("", campi.resto());
	}

	@Test
	void unaLocazioneSiRileggeConLeSueProprieta() throws IOException {
		// Given
		LocazioneMD locazione = new LocazioneMD(TipoLocazione.values()[0]);
		locazione.setNome("Radura del|corvo");
		locazione.aggiungiProprieta("NOTA", "una: frase\ncon due righe");

		// When
		LocazioneMD riletta = new LocazioneMD();
		riletta.leggi(lettore(scrivi(locazione)));

		// Then
		assertEquals(locazione.getUuid(), riletta.getUuid());
		assertEquals(locazione.getTipo(), riletta.getTipo());
		assertEquals("Radura delcorvo", riletta.getNome());
		assertEquals("una: frase con due righe", riletta.ottieniProprieta("NOTA"));
	}

	@Test
	void unaLocazioneTroncataDaUnErroreChiaro() {
		assertThrows(IOException.class, () -> new LocazioneMD().leggi(lettore("")));
		assertThrows(IOException.class, () -> new LocazioneMD().leggi(lettore("solo-l-uuid")));
		assertThrows(IOException.class, () -> new LocazioneMD().leggi(new BufferedReader(new StringReader(""))));
	}

	@Test
	void unaMissioneSiRileggeConLeSecondarieELeProprieta() throws IOException {
		// Given
		MissioneMD principale = new MissioneMD();
		principale.setId("m1");
		principale.setClasse(ClasseMissione.values()[0]);
		principale.setNome("La prova");
		principale.setDescrizione("Una|descrizione");
		principale.setDescrizioneVisibile(true);
		principale.aggiungiProprieta("PASSO", "3");
		MissioneMD secondaria = new MissioneMD();
		secondaria.setId("m2");
		secondaria.setClasse(ClasseMissione.values()[0]);
		secondaria.setNome("");
		principale.aggiungiMissioneMD(secondaria);

		// When
		MissioneMD riletta = new MissioneMD();
		riletta.leggi(lettore(scrivi(principale)));

		// Then
		assertEquals("m1", riletta.getId());
		assertEquals("La prova", riletta.getNome());
		assertEquals("Unadescrizione", riletta.getDescrizione());
		assertTrue(riletta.isDescrizioneVisibile());
		assertEquals("3", riletta.ottieniProprieta("PASSO"));
		assertEquals(1, riletta.getMissioniMD().size());
		assertEquals("m2", riletta.getMissioniMD().iterator().next().getId());
	}

	@Test
	void unaMissioneTroncataDaUnErroreChiaro() {
		assertThrows(IOException.class, () -> new MissioneMD().leggi(lettore("m1|" + ClasseMissione.values()[0].name())));
		assertThrows(IOException.class, () -> new MissioneMD().leggi(lettore("")));
		// La riga delle proprieta' manca del tutto
		assertThrows(IOException.class, () -> new MissioneMD().leggi(
				lettore("m1|" + ClasseMissione.values()[0].name() + "|n|d|true|0\n")));
	}

	@Test
	void unMessaggioConTipoSconosciutoOTroncatoDaUnErroreChiaro() {
		assertThrows(IOException.class, () -> new MessaggioMD().leggi(lettore("uuid|X|testo")));
		assertThrows(IOException.class, () -> new MessaggioMD().leggi(lettore("uuid")));
		assertThrows(IOException.class, () -> new MessaggioMD().leggi(lettore("")));
	}

	@Test
	void unMessaggioSenzaTestoSiRilegge() throws IOException {
		MessaggioMD messaggio = new MessaggioMD();
		messaggio.leggi(lettore("uuid|F|"));

		assertEquals("", messaggio.getTesto());
		assertFalse(messaggio.isParagrafo());
	}

	private static String scrivi(Serializzabile oggetto) throws IOException {
		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);
		oggetto.salva(stream);
		stream.flush();
		return scritto.toString();
	}

	private static BufferedReader lettore(String testo) {
		return new BufferedReader(new StringReader(testo));
	}
}
