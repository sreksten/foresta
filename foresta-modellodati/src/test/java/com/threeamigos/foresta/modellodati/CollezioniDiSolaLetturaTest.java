package com.threeamigos.foresta.modellodati;


import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le collezioni che il modello dati mostra sono di sola lettura: chi vuole cambiarle passa dai metodi che lo dicono,
 * così nessuno modifica per sbaglio l'elenco di un personaggio, del gruppo o il magazzino di un negozio.
 */
class CollezioniDiSolaLetturaTest {

	private static ArtefattoMD artefatto(String nome) {
		return ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, nome, "");
	}

	@Test
	void ilPersonaggioNonLasciaModificareLeSueCollezioni() {
		PersonaggioMD personaggio = new PersonaggioMD();

		assertThrows(UnsupportedOperationException.class, () -> personaggio.getArtefatti().add(artefatto("a")));
		assertThrows(UnsupportedOperationException.class, () -> personaggio.getModificatori()
				.add(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1)));
		assertThrows(UnsupportedOperationException.class, () -> personaggio.getEffettiDiStato().clear());
	}

	@Test
	void ilPersonaggioCambiaLeSueCollezioniConIMetodiEsplicitiELaVistaSiAggiorna() {
		PersonaggioMD personaggio = new PersonaggioMD();
		ArtefattoMD spada = artefatto("spada");
		ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1);
		EffettoDiStatoMD effetto = new EffettoDiStatoMD(TipoEffettoDiStato.values()[0], 3, 0, 1);
		java.util.Collection<ArtefattoMD> vista = personaggio.getArtefatti();

		personaggio.aggiungiArtefatto(spada);
		personaggio.aggiungiModificatore(modificatore);
		personaggio.aggiungiEffettoDiStato(effetto);

		assertTrue(vista.contains(spada), "la vista ottenuta prima riflette i cambiamenti");
		assertEquals(1, personaggio.getModificatori().size());
		assertEquals(1, personaggio.getEffettiDiStato().size());
		assertTrue(personaggio.togliArtefatto(spada));
		assertFalse(personaggio.togliArtefatto(spada), "non c'e' piu'");
		assertTrue(personaggio.togliModificatore(modificatore));
		assertTrue(personaggio.togliEffettoDiStato(effetto));
		assertTrue(vista.isEmpty());
	}

	@Test
	void ripristinaArtefattiRimetteIlContenutoELOrdine() {
		PersonaggioMD personaggio = new PersonaggioMD();
		ArtefattoMD a = artefatto("a");
		ArtefattoMD b = artefatto("b");
		ArtefattoMD c = artefatto("c");
		personaggio.aggiungiArtefatto(a);
		personaggio.aggiungiArtefatto(b);
		personaggio.aggiungiArtefatto(c);
		List<ArtefattoMD> prima = new ArrayList<>(personaggio.getArtefatti());

		personaggio.togliArtefatto(a);
		personaggio.togliArtefatto(b);
		personaggio.ripristinaArtefatti(prima);

		assertEquals(Arrays.asList(a, b, c), new ArrayList<>(personaggio.getArtefatti()));
	}

	@Test
	void ilGruppoDelGiocatoreNonLasciaModificareArtefattiEOspiti() {
		GruppoGiocatoreMD gruppo = new GruppoGiocatoreMD();

		assertThrows(UnsupportedOperationException.class, () -> gruppo.getArtefatti().add(artefatto("a")));
		assertThrows(UnsupportedOperationException.class, () -> gruppo.getOspitiMD().add(new PersonaggioMD()));
		assertThrows(UnsupportedOperationException.class, () -> gruppo.getOspitiVulnerabili().add("uuid"));

		ArtefattoMD spada = artefatto("spada");
		PersonaggioMD ospite = new PersonaggioMD();
		gruppo.aggiungiArtefatto(spada);
		gruppo.aggiungiOspite(ospite);
		gruppo.aggiungiOspiteVulnerabile(ospite.getUuid());
		assertEquals(1, gruppo.getArtefatti().size());
		assertEquals(1, gruppo.getOspitiMD().size());
		assertTrue(gruppo.getOspitiVulnerabili().contains(ospite.getUuid()));
		gruppo.togliArtefatto(spada);
		gruppo.togliOspite(ospite);
		gruppo.togliOspiteVulnerabile(ospite.getUuid());
		assertTrue(gruppo.getArtefatti().isEmpty() && gruppo.getOspitiMD().isEmpty() && gruppo.getOspitiVulnerabili().isEmpty());
	}

	@Test
	void ilMagazzinoDiUnNegozioSiLeggeSenzaPoterloModificareESenzaCrearlo() {
		RegistroArtefattiMD registro = new RegistroArtefattiMD();
		CoordinateMD citta = new CoordinateMD(7, 1);

		assertTrue(registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).isEmpty());
		assertThrows(UnsupportedOperationException.class,
				() -> registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).add(artefatto("a")));
		assertTrue(registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO).isEmpty(), "leggere un magazzino non lo crea");

		registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO, artefatto("a"));
		assertThrows(UnsupportedOperationException.class,
				() -> registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).clear());
		assertEquals(1, registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).size());
		assertTrue(registro.getCoordinateMagazzini(TipoNegozio.ARMAIOLO).contains(citta));
	}

	@Test
	void ilMagazzinoSiCambiaSoloConIMetodiDelRegistro() {
		RegistroArtefattiMD registro = new RegistroArtefattiMD();
		CoordinateMD citta = new CoordinateMD(7, 1);
		ArtefattoMD spada = artefatto("spada");
		ArtefattoMD ascia = artefatto("ascia");
		registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO, spada);
		registro.aggiungiAMagazzino(citta, TipoNegozio.ARMAIOLO, ascia);

		assertTrue(registro.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada));
		assertFalse(registro.rimuoviDaMagazzino(citta, TipoNegozio.ARMAIOLO, spada));
		assertFalse(registro.rimuoviDaMagazzino(new CoordinateMD(0, 0), TipoNegozio.ARMAIOLO, ascia), "un'altra citta'");
		registro.tieniInMagazzino(citta, TipoNegozio.ARMAIOLO, a -> a == spada);
		assertTrue(registro.getMagazzino(citta, TipoNegozio.ARMAIOLO).isEmpty(), "ascia non e' da tenere");
		registro.tieniInMagazzino(new CoordinateMD(0, 0), TipoNegozio.VENDITORE_DI_PERGAMENE, a -> true);
		assertTrue(registro.getCoordinateMagazzini(TipoNegozio.VENDITORE_DI_PERGAMENE).isEmpty(), "non crea magazzini");
	}

	@Test
	void ilGruppoCambiaIPersonaggiSoloConIMetodiEsplicitiEPuoRiordinarli() {
		GruppoMD gruppo = new GruppoMD();
		PersonaggioMD a = new PersonaggioMD();
		PersonaggioMD b = new PersonaggioMD();
		PersonaggioMD c = new PersonaggioMD();
		gruppo.addPersonaggioMD(a);
		gruppo.addPersonaggioMD(b);
		gruppo.addPersonaggioMD(c);

		assertThrows(UnsupportedOperationException.class, () -> gruppo.getPersonaggiMD().add(new PersonaggioMD()));
		assertThrows(UnsupportedOperationException.class, () -> gruppo.getPersonaggiMD().clear());

		gruppo.riordinaPersonaggiMD(Arrays.asList(c, a, b));
		assertEquals(Arrays.asList(c, a, b), gruppo.getPersonaggiMD());
		gruppo.rimuoviPersonaggioMD(a);
		assertEquals(Arrays.asList(c, b), gruppo.getPersonaggiMD());
		gruppo.setIndiceCapo(1);
		gruppo.svuotaPersonaggiMD();
		assertTrue(gruppo.getPersonaggiMD().isEmpty());
		assertEquals(1, gruppo.getIndiceCapo(), "svuotare i personaggi non tocca il capo");
	}

	@Test
	void riordinareConLaListaDelloStessoGruppoNonRompeNiente() {
		GruppoMD gruppo = new GruppoMD();
		PersonaggioMD a = new PersonaggioMD();
		PersonaggioMD b = new PersonaggioMD();
		gruppo.addPersonaggioMD(a);
		gruppo.addPersonaggioMD(b);

		// La vista del gruppo stesso come argomento: si copia prima di svuotare
		gruppo.riordinaPersonaggiMD(gruppo.getPersonaggiMD());

		assertEquals(Arrays.asList(a, b), gruppo.getPersonaggiMD());
	}

	@Test
	void unArtefattoNonLasciaModificareModificatoriEIncantamenti() {
		ArtefattoMD spada = artefatto("spada");
		spada.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 1));
		spada.addIncantamento("Fiamma", com.threeamigos.foresta.tipi.TipoDanno.FUOCO, 5, 0.1);

		assertThrows(UnsupportedOperationException.class, () -> spada.getModificatori().clear());
		assertThrows(UnsupportedOperationException.class, () -> spada.getIncantamenti().clear());
		assertEquals(1, spada.getModificatori().size());
		assertEquals(1, spada.getIncantamenti().size());

		spada.rimuoviTuttiGliIncantamenti();
		assertTrue(spada.getIncantamenti().isEmpty());
		assertEquals(1, spada.getModificatori().size(), "i modificatori restano");
	}

	@Test
	void ilRegistroDelleMissioniEUnaMissioneNonLasciannoModificareLeLoroListe() {
		RegistroMissioniMD registro = new RegistroMissioniMD();
		MissioneMD principale = new MissioneMD();
		principale.setId("m1");
		MissioneMD secondaria = new MissioneMD();
		secondaria.setId("m2");
		registro.aggiungiMissione("m1", principale);
		principale.aggiungiMissioneMD(secondaria);

		assertThrows(UnsupportedOperationException.class, () -> registro.getMissioni().clear());
		assertThrows(UnsupportedOperationException.class, () -> principale.getMissioniMD().clear());
		assertEquals(1, registro.getMissioni().size());
		assertEquals(1, principale.getMissioniMD().size());

		principale.rimuoviMissioneMD(secondaria);
		assertTrue(principale.getMissioniMD().isEmpty());
	}

	@Test
	void laForestaNonLasciaModificareLeCittaDistrutte() {
		ForestaMD foresta = new ForestaMD();

		assertThrows(UnsupportedOperationException.class, () -> foresta.getCittaDistrutte().add(com.threeamigos.foresta.tipi.TipoLocazione.CITTA_RUUNA));

		foresta.addCittaDistrutta(com.threeamigos.foresta.tipi.TipoLocazione.CITTA_RUUNA);
		assertEquals(Collections.singletonList(TipoLocazione.CITTA_RUUNA), foresta.getCittaDistrutte());
		assertTrue(foresta.isCittaDistrutta(com.threeamigos.foresta.tipi.TipoLocazione.CITTA_RUUNA));
	}

	@Test
	void leNotizieEIMessaggiNonSiModificanoDaFuori() {
		NotizieMD notizie = new NotizieMD();

		assertThrows(UnsupportedOperationException.class, () -> notizie.getUltimiMessaggi().add(new MessaggioMD("x", false)));
		assertThrows(UnsupportedOperationException.class, () -> notizie.getUltimeNotizie().add(new NotiziaMD("N", "x")));
	}
}
