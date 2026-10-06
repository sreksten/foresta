package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;

import com.threeamigos.foresta.eventi.interni.InternoRichiestaChiusuraFinestraCombattimento;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Missioni fallite, citta' distrutte, locazioni delle missioni, annullamenti che non fanno passare il turno.
 */
class ScenarioMissioniELocazioniTest {

	@Test
	void laMissioneCheFallisceFinisceTraLeFalliteAncheDopoUnSalvataggio() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(21)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			assertTrue(RegistroMissioni.getMissioniAttive().stream().anyMatch(m -> m instanceof MissioneCheFallisce),
					"al primo turno la missione e' attiva");

			partita.comando(Comando.ESCI_DA_CITTA);
			muoviDiUnPasso(partita);

			assertTrue(RegistroMissioni.getMissioniFallite().stream().anyMatch(m -> m instanceof MissioneCheFallisce),
					"al secondo turno e' fallita");
			assertFalse(RegistroMissioni.getMissioniAttive().stream().anyMatch(m -> m instanceof MissioneCheFallisce));
			assertFalse(RegistroMissioni.getMissioniCompletate().stream().anyMatch(m -> m instanceof MissioneCheFallisce));

			partita.salva(Comando.NUMERO_3);
			assertTrue(partita.leggi(Comando.NUMERO_3));
			assertTrue(RegistroMissioni.getMissioniFallite().stream().anyMatch(m -> m instanceof MissioneCheFallisce),
					"dopo il caricamento non e' piu' tra le fallite");
		}
	}

	@Test
	void unaSottoMissioneCompletataStaSoloTraLeCompletateEPoiSottoLaSuaMissione() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(21)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			// Le missioni dei boss sono sotto-missioni del Drago, non missioni di primo livello
			assertFalse(RegistroMissioni.getMissioniNonCompletate().stream().anyMatch(m -> m instanceof SconfiggiIlMinotauroGigante),
					"il Minotauro Gigante non e' una missione di primo livello");
			trovaMinotauroGigante().completaMissione();

			assertEquals(1, RegistroMissioni.getMissioniCompletate().stream()
					.filter(m -> m instanceof SconfiggiIlMinotauroGigante).count(), "compare una volta tra le completate");
			partita.salva(Comando.NUMERO_3);
			assertTrue(partita.leggi(Comando.NUMERO_3));
			assertEquals(1, RegistroMissioni.getMissioniCompletate().stream()
					.filter(m -> m instanceof SconfiggiIlMinotauroGigante).count(), "anche dopo un caricamento");

			SconfiggiIlDrago drago = RegistroMissioni.getMissionePrincipale();
			drago.completaMissione();

			assertTrue(RegistroMissioni.getMissioniCompletate().contains(drago));
			assertFalse(RegistroMissioni.getMissioniCompletate().stream().anyMatch(m -> m instanceof SconfiggiIlMinotauroGigante),
					"completato il Drago, il Minotauro Gigante compare solo sotto di lui");
			assertTrue(drago.getMissioniSecondarie().stream().anyMatch(m -> m instanceof SconfiggiIlMinotauroGigante));
		}
	}

	private static Missione trovaMinotauroGigante() {
		return RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().stream()
				.filter(m -> m instanceof SconfiggiIlMinotauroGigante).findFirst()
				.orElseThrow(() -> new AssertionError("il Minotauro Gigante deve essere una sotto-missione del Drago"));
	}

	@Test
	void unaCittaDistruttaDiventaRovineEFaFallireLeMissioniDiConsegna() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
			Missione medaglione = trova(RecuperaIlMedaglione.class);
			assertTrue(medaglione.isAttiva(), "a Fleena la missione del medaglione si attiva");
			Missione cronache = trova(CronacheDiUnFegatoEroico.class);
			CoordinateMD fleena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA);

			while (LineaTemporale.getGiorno() < 30) {
				LineaTemporale.aggiungiOre(24);
			}
			LineaTemporale.eventi(partita.gruppo());

			assertTrue(LineaTemporale.isCittaDistrutta(TipoLocazione.CITTA_FLEENA));
			assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(fleena));
			assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA));

			partita.comando(Comando.ESCI_DA_CITTA);
			muoviDiUnPasso(partita);

			assertTrue(medaglione.isFallita(), "il medaglione non si puo' piu' consegnare");
			assertTrue(RegistroMissioni.getMissioniFallite().contains(medaglione));
			assertTrue(cronache.isFallita(), "una tappa delle cronache e' andata persa");
			// Le descrizioni delle tappe non cercano piu' la locanda sulla mappa
			cronache.getMissioniSecondarie().forEach(m -> assertNotNull(m.getNome()));
		}
	}

	@Test
	void ilTerzoLadroDellaGrottaEAlmenoDiPrimoLivelloELaGrottaCompletaDiventaUnaGrotta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(23)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, true);
			partita.gruppo().setCoordinate(coordinate);
			Locazione grotta = Foresta.costruisciIstanza(coordinate);
			GruppoAvversario avversari = GruppoAvversario.getIstanza();
			avversari.reimposta();
			grotta.crea(partita.gruppo(), avversari);
			assertEquals(4, avversari.getNumeroPersonaggi());
			avversari.getPersonaggi().forEach(p -> assertTrue(p.getLivello() >= 1, p.getNome() + " livello " + p.getLivello()));

			Foresta.getLocazioneMD(coordinate).aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
			grotta.azzeraLocazione(partita.gruppo());
			assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(coordinate));
			assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE));
		}
	}

	@Test
	void leRovineDelleDerrateCompleteDiventanoRovine() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(24)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, true);
			partita.gruppo().setCoordinate(coordinate);
			Foresta.getLocazioneMD(coordinate).aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
			Locazione rovine = Foresta.costruisciIstanza(coordinate);
			rovine.azzeraLocazione(partita.gruppo());
			assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(coordinate));
			assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI));
		}
	}

	@Test
	void annullareUnaSceltaEIlNoAllaFugaNonFannoPassareIlTurno() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			// Con due personaggi vivi le scelte non sono automatiche e si possono annullare
			partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO, 5, 0);

			partita.comando(Comando.CORRUZIONE).comando(Comando.ANNULLA);
			partita.comando(Comando.AMICIZIA).comando(Comando.ANNULLA);
			partita.comando(Comando.COMBATTIMENTO).comando(Comando.ANNULLA);
			partita.comando(Comando.FUGA).comando(Comando.NO);

			partita.assertStato(Stato.IN_LOCAZIONE);
			assertEquals(5, durata(capo, TipoEffettoDiStato.RALLENTATO), "un annullamento ha fatto passare il turno");
		}
	}

	@Test
	void bereUnaPozioneInCombattimentoConPiuViviChiudeLaFinestraDelCombattimento() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(26)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			partita.eventi().ascolta(InternoRichiestaChiusuraFinestraCombattimento.class);
			partita.comando(Comando.COMBATTIMENTO).comando(Comando.PERSONAGGIO_1);
			partita.assertStato(Stato.IN_COMBATTIMENTO);

			partita.comando(Comando.POZIONE_SALUTE);

			assertTrue(partita.eventi().haRicevuto(InternoRichiestaChiusuraFinestraCombattimento.class));
		}
	}

	@Test
	void toltoUnArtefattoDiSaluteLaSaluteTornaEntroIlMassimo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(27)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			Guerriero guerriero = new Guerriero("G", 1);
			int massimoSenza = guerriero.getSaluteMassima();
			Artefatto amuleto = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.NINNOLO).setNome("amuleto di prova").setDescrizione("che dà salute")
					.setLivello(1).setDanniBase(0).setCostoAcquisto(1).setPeso(0)
					.setModificatore(TipoAttributo.SALUTE, TipoModificatore.AUMENTO_FISSO, 100)
					.costruisci();
			guerriero.addArtefatto(amuleto);
			guerriero.addSalute(guerriero.getSaluteMassima());
			assertTrue(guerriero.getSalute() > massimoSenza);

			guerriero.removeArtefatto(amuleto);

			assertEquals(massimoSenza, guerriero.getSalute());
		}
	}

	private static void muoviDiUnPasso(PartitaDiTest partita) {
		partita.assertStato(Stato.SCELTA_DIREZIONE);
		for (Comando direzione : new Comando[]{Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST}) {
			if (partita.comandiDisponibili().contains(direzione)) {
				partita.comando(direzione).comando(Comando.NUMERO_1);
				return;
			}
		}
		throw new AssertionError("nessuna direzione disponibile");
	}

	private static Missione trova(Class<? extends Missione> tipo) {
		return RegistroMissioni.getMissioniNonCompletate().stream().filter(tipo::isInstance).findFirst()
				.orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
	}

	private static int durata(Personaggio personaggio, TipoEffettoDiStato tipo) {
		return personaggio.getEffettiDiStato().stream()
				.filter(e -> e.getTipoEffettoDiStato() == tipo)
				.mapToInt(EffettoDiStatoMD::getDurata)
				.findFirst().orElse(0);
	}
}
