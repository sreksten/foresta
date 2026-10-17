package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tutte le righe dei salvataggi si leggono con {@link LettoreCampi}: un salvataggio troncato o rovinato dà sempre un
 * {@link IOException} che dice cosa manca, mai una NullPointerException o un indice fuori dai limiti.
 */
class LetturaDeiSalvataggiTest {

	@Test
	void isottocampiSiDividonoEUltimoTieneIlResto() throws IOException {
		LettoreCampi campi = new LettoreCampi("FORZA§12|A§B§C§D§E§F§nota con § dentro|solo");

		LettoreCampi attributo = campi.sottocampi(2, 2);
		assertEquals("FORZA", attributo.testo());
		assertEquals(12, attributo.intero());
		assertFalse(attributo.haAltriCampi());
		LettoreCampi modificatore = campi.sottocampi(6, 7);
		for (String atteso : new String[]{"A", "B", "C", "D", "E", "F", "nota con § dentro"}) {
			assertEquals(atteso, modificatore.testo());
		}
		assertFalse(modificatore.haAltriCampi());
		LettoreCampi solo = campi.sottocampi(1, 2);
		assertEquals("solo", solo.testo());
		assertFalse(solo.haAltriCampi());
	}

	@Test
	void unNumeroRovinatoDaUnIOExceptionCheDiceDove() throws IOException {
		LettoreCampi campi = new LettoreCampi("12|dodici");
		assertEquals(12, campi.intero());
		IOException errore = assertThrows(IOException.class, campi::intero);
		assertTrue(errore.getMessage().contains("dodici"), errore.getMessage());
	}

	@Test
	void unaRigaConUnSoloNumeroMancanteDaUnIOException() {
		BufferedReader vuoto = new BufferedReader(new StringReader(""));
		assertThrows(IOException.class, () -> LettoreCampi.leggiIntero(vuoto));
	}

	@Test
	void modificatoriEffettiEIncantamentiSiRileggonoDalLoroCampo() throws IOException {
		ModificatoreAttributoMD conNota = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.values()[0], 2.5d,
				"nota con § dentro", UnitaDurataModificatore.PERMANENTE, 0, BersaglioModificatore.PORTATORE);
		ModificatoreAttributoMD senzaNota = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.values()[0], -1.0d);
		EffettoDiStatoMD effetto = new EffettoDiStatoMD(TipoEffettoDiStato.BRUCIATO, 3, 4, 0.5d);
		IncantamentoMD incantamento = new IncantamentoMD("fiamma § viva", TipoDanno.FUOCO, 4, 0.2d);

		LettoreCampi campi = new LettoreCampi(String.join(Serializzabile.PIPE, conNota.comeCampo(), senzaNota.comeCampo(),
				effetto.comeCampo(), incantamento.comeCampo()));

		assertEquals(conNota, ModificatoreAttributoMD.daCampo(campi));
		assertEquals(senzaNota, ModificatoreAttributoMD.daCampo(campi));
		EffettoDiStatoMD effettoRiletto = EffettoDiStatoMD.daCampo(campi);
		assertEquals(TipoEffettoDiStato.BRUCIATO, effettoRiletto.getTipoEffettoDiStato());
		assertEquals(3, effettoRiletto.getDurata());
		assertEquals(4, effettoRiletto.getDanniNelTempo());
		assertEquals(0.5d, effettoRiletto.getIntensita());
		IncantamentoMD incantamentoRiletto = IncantamentoMD.daCampo(campi);
		assertEquals("fiamma § viva", incantamentoRiletto.getNomeIncantamento());
		assertEquals(TipoDanno.FUOCO, incantamentoRiletto.getTipoDannoElementale());
		assertEquals(4, incantamentoRiletto.getDannoBonusFisso());
		assertEquals(0.2d, incantamentoRiletto.getCoefficienteScala());
		assertFalse(campi.haAltriCampi());
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
		principale.setClasse(MissionePredefinita.values()[0]);
		principale.setNome("La prova");
		principale.setDescrizione("Una|descrizione");
		principale.setDescrizioneVisibile(true);
		principale.aggiungiProprieta("PASSO", "3");
		MissioneMD secondaria = new MissioneMD();
		secondaria.setId("m2");
		secondaria.setClasse(MissionePredefinita.values()[0]);
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
		assertThrows(IOException.class, () -> new MissioneMD().leggi(lettore("m1|" + MissionePredefinita.values()[0].name())));
		assertThrows(IOException.class, () -> new MissioneMD().leggi(lettore("")));
		// La riga delle proprieta' manca del tutto
		assertThrows(IOException.class, () -> new MissioneMD().leggi(
				lettore("m1|" + MissionePredefinita.values()[0].name() + "|n|d|true|0\n")));
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

	@Test
	void unPersonaggioTroncatoInQualunqueRigaDaUnErroreChiaro() throws IOException {
		PersonaggioMD personaggio = new PersonaggioMD();
		personaggio.setClasse(TipoPersonaggio.GUERRIERO);
		personaggio.setVivo(true);
		personaggio.setForza(12);
		personaggio.aggiungiArtefatto(ArtefattoMDTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia"));
		String[] righe = scrivi(personaggio).split("\n");
		// Senza tattica la sua riga è vuota: troncato proprio lì, non deve diventare una NullPointerException
		for (int quante = 0; quante < righe.length; quante++) {
			StringBuilder troncato = new StringBuilder();
			for (int i = 0; i < quante; i++) {
				troncato.append(righe[i]).append('\n');
			}
			String testo = troncato.toString();
			assertThrows(IOException.class, () -> new PersonaggioMD().leggi(lettore(testo)), "troncato dopo " + quante + " righe");
		}
	}

	private static BufferedReader lettore(String testo) {
		return new BufferedReader(new StringReader(testo));
	}
}
