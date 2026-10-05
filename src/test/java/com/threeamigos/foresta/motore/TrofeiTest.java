package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoMissioneCompletata;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I trofei: quanto si guadagna in una locazione conta solo se il gruppo ne esce vivo, il
 * progresso si accumula da una partita all'altra, i trofei si controllano a fine locazione,
 * si salvano appena vinti e non si perdono più.
 */
class TrofeiTest {

	@Test
	void mangiareInUnaLocandaInCittaFaAvanzareLoSbevazzoneAFineLocazione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));

			partita.comando(Comando.LOCANDA);
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE), "superata la porta non si e' ancora mangiato");
			partita.comando(Comando.PERGAMENA);
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE), "il pasto resta in sospeso fino alla fine della locazione");

			partita.pubblica(new InternoFineLocazione());

			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
			assertTrue(partita.trofei().contieneRiga("SBEVAZZONE|false|1"), "a fine locazione il progresso va salvato");
		}
	}

	@Test
	void chiVieneRespintoDallOsteNonHaVisitatoLaLocanda() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.gruppo().subMonete(partita.gruppo().getMonete());

			partita.comando(Comando.LOCANDA);

			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE),
					"senza monete l'oste non fa entrare e non si mangia: la visita non conta");
		}
	}

	@Test
	void leLocandeVisitateSiSommanoDaUnaPartitaAllAltra() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.comando(Comando.LOCANDA);
			partita.comando(Comando.PERGAMENA);
			partita.pubblica(new InternoFineLocazione());
			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));

			// Una nuova partita azzera il modello dati, non il progresso verso i trofei
			ModelloDati.setIstanza(new ModelloDati());
			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));

			// Un riavvio del gioco lo rilegge da dove era stato salvato
			RegistroTrofei.impostaGestoreTrofei(partita.trofei());
			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
		}
	}

	@Test
	void loSbevazzoneSiVinceAFineLocazioneEUnaVoltaSola() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoTrofeoAcquisito.class);
			for (int i = 0; i < 100; i++) {
				RegistroTrofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);
			}

			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE), "i trofei si controllano solo a fine locazione");

			partita.comando(Comando.ESCI_DA_CITTA);

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE));
			assertEquals(1, partita.eventi().tutti(InternoTrofeoAcquisito.class).size());
			assertEquals(TipoTrofeo.SBEVAZZONE, partita.eventi().ultimo(InternoTrofeoAcquisito.class).getTrofeo());
			assertTrue(partita.trofei().contieneRiga("SBEVAZZONE|true|100"), "appena vinto, il trofeo va salvato");

			// Un trofeo gia' vinto non si annuncia, non avanza e non si salva di nuovo
			int salvataggi = partita.trofei().getSalvataggi();
			RegistroTrofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);
			partita.pubblica(new InternoFineLocazione());
			assertEquals(1, partita.eventi().tutti(InternoTrofeoAcquisito.class).size());
			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
			assertEquals(salvataggi, partita.trofei().getSalvataggi());
		}
	}

	@Test
	void unTrofeoVintoValeAncheNellePartiteSuccessive() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			for (int i = 0; i < 100; i++) {
				RegistroTrofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);
			}
			partita.comando(Comando.ESCI_DA_CITTA);
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE));

			ModelloDati.setIstanza(new ModelloDati());
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE));

			RegistroTrofei.impostaGestoreTrofei(partita.trofei());
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE));
		}
	}

	@Test
	void lAmmazzagoblinContaSoloIGoblinSconfittiESiVinceAFineLocazione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.eventi().ascolta(InternoTrofeoAcquisito.class);

			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));
			partita.pubblica(new InternoFineLocazione());
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.AMMAZZAGOBLIN), "un troll non e' un goblin");

			for (int i = 0; i < 100; i++) {
				partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
			}
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.AMMAZZAGOBLIN), "i trofei si controllano solo a fine locazione");

			partita.pubblica(new InternoFineLocazione());

			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.AMMAZZAGOBLIN));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.AMMAZZAGOBLIN));
			assertTrue(partita.trofei().contieneRiga("AMMAZZAGOBLIN|true|100"));
			assertEquals(TipoTrofeo.AMMAZZAGOBLIN, partita.eventi().ultimo(InternoTrofeoAcquisito.class).getTrofeo());
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE), "gli altri trofei non c'entrano");
		}
	}

	@Test
	void lAmicoDiTuttiSiVinceDopoCentoAmicizie() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 99; i++) {
				partita.pubblica(new InternoAmiciziaStretta(Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.GOBLIN)));
			}
			partita.pubblica(new InternoFineLocazione());
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.AMICO_DI_TUTTI), "contano le amicizie, non i personaggi");

			partita.pubblica(new InternoAmiciziaStretta(Collections.singletonList(TipoPersonaggio.TROLL)));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.AMICO_DI_TUTTI));
		}
	}

	@Test
	void ilCorruttoreContaLeCorruzioniRiuscite() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			List<TipoPersonaggio> treGoblin = Arrays.asList(TipoPersonaggio.GOBLIN, TipoPersonaggio.GOBLIN, TipoPersonaggio.GOBLIN);
			for (int i = 0; i < 99; i++) {
				partita.pubblica(new InternoCorruzioneRiuscita(treGoblin));
			}
			partita.pubblica(new InternoFineLocazione());
			assertEquals(99, RegistroTrofei.getProgresso(TipoTrofeo.CORRUTTORE), "conta le corruzioni, non i personaggi corrotti");
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.CORRUTTORE));

			partita.pubblica(new InternoCorruzioneRiuscita(treGoblin));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.CORRUTTORE));
		}
	}

	@Test
	void unaCorruzioneRiuscitaInGiocoFaAvanzareIlCorruttore() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoCorruzioneRiuscita.class);

			// Qui interessa l'esito, non l'offerta del comando: le monete bastano e il dado decide che
			// la corruzione riesce
			Dado.trucca(10);
			partita.comando(Comando.CORRUZIONE);

			assertEquals(1, partita.eventi().tutti(InternoCorruzioneRiuscita.class).size());
			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.CORRUTTORE));
		}
	}

	@Test
	void unAmiciziaStrettaInGiocoFaAvanzareLAmicoDiTutti() {
		// Senza la modalita' di prova: i suoi scarponi di RomyJona azzerano il carisma
		try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			partita.eventi().ascolta(InternoAmiciziaStretta.class);
			assertTrue(partita.gruppo().getCapo().getCarisma() > 1, "precondizione: il capo ha carisma");

			// Qui interessa l'esito, non l'offerta del comando: il dado decide che l'amicizia riesce
			Dado.trucca(1);
			partita.comando(Comando.AMICIZIA);

			assertEquals(1, partita.eventi().tutti(InternoAmiciziaStretta.class).size());
			assertEquals(1, RegistroTrofei.getProgresso(TipoTrofeo.AMICO_DI_TUTTI));
		}
	}

	@Test
	void iTrofeiDeiBossSiVinconoSconfiggendoliUnaVolta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.CHIMERA_DRAGO));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.IDRA));
			partita.pubblica(new InternoFineLocazione());

			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_IL_DRAGO), "una chimera drago non e' il Drago");
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_L_IDRA));
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_LA_STREGA));

			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.DRAGO));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.STREGA));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.LICH));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.MINOTAURO_GIGANTE));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_IL_DRAGO));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_LA_STREGA));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_IL_LICH));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_IL_MINOTAURO_GIGANTE));
		}
	}

	@Test
	void iTesoriContanoSoloSeCustoditiDagliAvversari() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			// 99 monete, pietre preziose e corone tolte agli avversari, e altrettante trovate incustodite
			for (int i = 0; i < 33; i++) {
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.MONETA, 3, null, true));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.PIETRA_PREZIOSA, 3, null, true));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.CORONA, 3, null, true));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.MONETA, 3, null, false));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.PIETRA_PREZIOSA, 3, null, false));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.CORONA, 3, null, false));
			}
			partita.pubblica(new InternoFineLocazione());

			assertEquals(99, RegistroTrofei.getProgresso(TipoTrofeo.RAPINATORE), "le monete incustodite non contano");
			assertEquals(99, RegistroTrofei.getProgresso(TipoTrofeo.LADRO_DI_PREZIOSI));
			assertEquals(99, RegistroTrofei.getProgresso(TipoTrofeo.ARSENIO_LUPIN));
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.RAPINATORE));

			partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.MONETA, 1, null, true));
			partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.PIETRA_PREZIOSA, 1, null, true));
			partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.CORONA, 1, null, true));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.RAPINATORE));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.LADRO_DI_PREZIOSI));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.ARSENIO_LUPIN));
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.ESPERTO_SCASSINATORE), "monete, pietre preziose e corone non sono cofani");
		}
	}

	@Test
	void lEspertoScassinatoreContaAncheICofaniIncustoditi() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 50; i++) {
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.COFANO, 1, null, true));
				partita.pubblica(new InternoOggettoRaccolto(TipoOggetto.COFANO, 1, null, false));
			}
			partita.pubblica(new InternoFineLocazione());

			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.ESPERTO_SCASSINATORE));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.ESPERTO_SCASSINATORE));
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.RAPINATORE));
		}
	}

	@Test
	void unBossUccisoInUnoScontroInCuiSiMuoreNonConta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.DRAGO));
			// Il gruppo muore: la fine della locazione non arriva, e la partita successiva
			// prepara una nuova locazione
			partita.pubblica(new InternoPreparazioneLocazione());
			partita.pubblica(new InternoFineLocazione());

			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.UCCIDI_IL_DRAGO));
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.UCCIDI_IL_DRAGO));
		}
	}

	@Test
	void ilCacciatoreDiTaglieContaLeMissioniCompletate() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.eventi().ascolta(InternoMissioneCompletata.class);

			RegistroMissioni.getMissionePrincipale().completaMissione();
			assertEquals(1, partita.eventi().tutti(InternoMissioneCompletata.class).size(), "completare una missione pubblica l'evento");
			for (int i = 0; i < 48; i++) {
				partita.pubblica(new InternoMissioneCompletata(RegistroMissioni.getMissionePrincipale()));
			}
			partita.pubblica(new InternoFineLocazione());
			assertEquals(49, RegistroTrofei.getProgresso(TipoTrofeo.CACCIATORE_DI_TAGLIE));
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.CACCIATORE_DI_TAGLIE));

			partita.pubblica(new InternoMissioneCompletata(RegistroMissioni.getMissionePrincipale()));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.CACCIATORE_DI_TAGLIE));
		}
	}

	@Test
	void ilPerdigiornoSiVinceInsiemeAllUltimoDegliAltriTrofei() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			// Vinti in partite precedenti tutti tranne il Perdigiorno e l'Ammazzagoblin
			partita.trofei().conVinti(Arrays.stream(TipoTrofeo.values())
					.filter(t -> t != TipoTrofeo.PERDIGIORNO && t != TipoTrofeo.AMMAZZAGOBLIN)
					.toArray(TipoTrofeo[]::new));
			RegistroTrofei.impostaGestoreTrofei(partita.trofei());
			partita.eventi().ascolta(InternoTrofeoAcquisito.class);
			partita.pubblica(new InternoFineLocazione());
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.PERDIGIORNO), "manca ancora l'Ammazzagoblin");

			for (int i = 0; i < 100; i++) {
				partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
			}
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.AMMAZZAGOBLIN));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.PERDIGIORNO), "nella stessa fine locazione dell'ultimo trofeo");
			assertEquals(Arrays.asList(TipoTrofeo.AMMAZZAGOBLIN, TipoTrofeo.PERDIGIORNO),
					partita.eventi().tutti(InternoTrofeoAcquisito.class).stream().map(InternoTrofeoAcquisito::getTrofeo).collect(Collectors.toList()));
		}
	}

	@Test
	void ogniPartitaDiTestPartePerdendoITrofeiDelleAltre() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.SBEVAZZONE));
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.SBEVAZZONE));
		}
	}
}
