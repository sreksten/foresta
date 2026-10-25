package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.combattimento.AttaccoConLeArmi;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.effettidistato.EffettoDiStatoMD;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.mappa.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressioni dei bug corretti dopo l'indagine sul codice.
 */
class ScenarioCorrezioniTest {

	@Test
	void sconfittoIlDragoLaMissionePrincipaleECompletaENonSparisce() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			RegistroMissioni.getMissionePrincipale().completaMissione();
			assertNotNull(RegistroMissioni.getMissionePrincipale(), "la missione principale sparisce appena completata");
			assertTrue(RegistroMissioni.getMissionePrincipale().isCompleta());
		}
	}

	@Test
	void gliAttacchiFisiciFuoriDallaMischiaFannoDanno() {
		ModelloDati.setIstanza(new ModelloDati());
		Dado.impostaSeme(21);
		Troll troll = new Troll(5);
		Guerriero guerriero = new Guerriero("Bersaglio", 1);
		int salutePrima = guerriero.getSalute();
		for (int i = 0; i < 20 && guerriero.getSalute() == salutePrima; i++) {
			Dado.trucca(1); // il tiro per colpire (d100): con 1 si colpisce sempre
			AttaccoConLeArmi.esegui(troll, guerriero);
			Dado.ripristina();
		}
		assertTrue(guerriero.getSalute() < salutePrima, "il troll non ha mai fatto danno");
	}

	@Test
	void inCombattimentoGliEffettiDiStatoScendonoAOgniRound() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(5)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO, 5, 0);

			partita.comando(Comando.COMBATTIMENTO);

			partita.assertStato(Stato.IN_COMBATTIMENTO);
			// Scegliere chi combatte non è un'azione: non passa nessun turno di effetti
			assertEquals(5, durata(capo, TipoEffettoDiStato.RALLENTATO), "iniziare il combattimento non e' un turno");
			// Ogni scatto è una sola azione: gli effetti scendono quando finisce il round, cioè quando hanno agito tutti
			int round = StatoDelCombattimento.getIstanza().getRound();
			for (int i = 0; i < 20 && partita.stato() == Stato.IN_COMBATTIMENTO && StatoDelCombattimento.getIstanza().getRound() == round; i++) {
				assertEquals(5, durata(capo, TipoEffettoDiStato.RALLENTATO), "l'effetto e' sceso prima della fine del round");
				partita.scatta();
			}
			if (partita.stato() == Stato.IN_COMBATTIMENTO) {
				assertEquals(round + 1, StatoDelCombattimento.getIstanza().getRound());
				assertEquals(4, durata(capo, TipoEffettoDiStato.RALLENTATO));
			}
		}
	}

	@Test
	void accamparsiDopoAverCaricatoUnaPartitaNonVaInErrore() {
		GestoreSalvataggiInMemoria salvataggi;
		try (PartitaDiTest prima = PartitaDiTest.nuova(7)) {
			prima.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> prima.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			prima.comando(Comando.ESCI_DA_CITTA);
			// Ci si accampa nel bosco, non in citta': il gruppo va su una casella di bosco prima del salvataggio
			prima.gruppo().setCoordinate(unaCasellaDiBosco());
			// Ci si accampa solo di notte e con qualcuno da mettere di guardia
			prima.gruppo().aggiungiPersonaggio(new Guerriero("Sentinella", 1));
			while (LineaTemporale.getOra() <= 20) {
				LineaTemporale.aggiungiOre(1);
			}
			prima.comando(Comando.FLOPPY_SALVA).comando(Comando.NUMERO_1);
			salvataggi = prima.salvataggi();
		}
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(8, salvataggi)) {
			dopo.comando(Comando.FLOPPY_CARICA).comando(Comando.NUMERO_1);
			dopo.assertStato(Stato.SCELTA_DIREZIONE);
			dopo.assertComandoDisponibile(Comando.ACCAMPAMENTO);
			dopo.comando(Comando.ACCAMPAMENTO);
			dopo.assertStato(Stato.SCELTA_DIREZIONE);
		}
	}

	@Test
	void superatoLUltimoGiornoAccampandosiLaPartitaEPersaSubito() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.comando(Comando.ESCI_DA_CITTA);
			partita.gruppo().setCoordinate(unaCasellaDiBosco());
			partita.gruppo().aggiungiPersonaggio(new Guerriero("Sentinella", 1));
			while (LineaTemporale.getOra() <= 20) {
				LineaTemporale.aggiungiOre(1);
			}
			ModelloDati.getIstanza().getLineaTemporaleMD().setGiorno(40);

			// Il mattino dopo è il giorno 41: non si torna a scegliere la direzione (e a poter salvare)
			partita.comando(Comando.ACCAMPAMENTO);
			partita.assertStato(Stato.GIOCO_PERSO);
			assertTrue(partita.testi().stream().anyMatch(t -> t.contains("tutto e' perduto")), String.valueOf(partita.testi()));
		}
	}

	@Test
	void daSoliNonCiSiAccampa() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.comando(Comando.ESCI_DA_CITTA);
			partita.gruppo().setCoordinate(unaCasellaDiBosco());
			while (LineaTemporale.getOra() <= 20) {
				LineaTemporale.aggiungiOre(1);
			}
			partita.assertStato(Stato.SCELTA_DIREZIONE);

			// Nessuno da mettere di guardia: il comando, anche se arrivasse, va rifiutato
			AssertionError errore = assertThrows(AssertionError.class, () -> partita.comando(Comando.ACCAMPAMENTO));
			assertTrue(errore.getMessage().contains("Comando non valido: " + Comando.ACCAMPAMENTO), errore.getMessage());
			partita.assertStato(Stato.SCELTA_DIREZIONE);
			assertEquals(0, ModelloDati.getIstanza().getIntermezziMD().getNumeroAccampamenti(),
					"un accampamento rifiutato non va contato");
		}
	}

	@Test
	void chiLasciaIlGruppoNonTornaDopoIlSalvataggio() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(9)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			Guerriero mercenario = new Guerriero("Mercenario", 1);
			partita.gruppo().aggiungiPersonaggio(mercenario);
			assertEquals(2, partita.gruppo().getNumeroPersonaggi());
			partita.gruppo().rimuoviPersonaggio(mercenario);

			partita.salva(Comando.NUMERO_2);
			assertTrue(partita.leggi(Comando.NUMERO_2));
			assertEquals(1, partita.gruppo().getNumeroPersonaggi(), "il mercenario e' tornato nel gruppo");
		}
	}

	@Test
	void resurrezioneEMorteConsumanoMagia() {
		ModelloDati.setIstanza(new ModelloDati());
		Mago mago = new Mago("Merlino", 5);
		mago.addMagia(mago.getMagiaMassima());
		Guerriero compagno = new Guerriero("Compagno", 1);

		Incantesimo resurrezione = FabbricaIncantesimi.crea(TipoIncantesimo.RESURREZIONE, 5);
		int prima = mago.getMagia();
		resurrezione.formula(mago, compagno, null);
		assertEquals(prima - resurrezione.getCostoLancio(), mago.getMagia());

		Incantesimo morte = FabbricaIncantesimi.crea(TipoIncantesimo.MORTE, 5);
		prima = mago.getMagia();
		morte.formula(mago, new Troll(1), null);
		assertEquals(prima - morte.getCostoLancio(), mago.getMagia());
	}

	@Test
	void dopoMezzanotteSiRiposaFinoAlleOtto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(12)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			while (LineaTemporale.getOra() != 2) {
				LineaTemporale.aggiungiOre(1);
			}
			assertEquals(6, LineaTemporale.oreFinoAlMattino());
			while (LineaTemporale.getOra() != 21) {
				LineaTemporale.aggiungiOre(1);
			}
			assertEquals(11, LineaTemporale.oreFinoAlMattino());
		}
	}

	@Test
	void unaMissioneSecondariaDaIlVentiPerCentoDegliXpDelLivello() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(13)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			Personaggio capo = partita.gruppo().getCapo();
			int prima = capo.getEsperienza();
			int attesi = GestoreProgressione.getXpRichiestiPerProssimoLivello(Statistiche.getLivello()) * 20 / 100;
			assertTrue(attesi > 0);
			EsperienzaDelGruppo.completaMissioneSecondaria();
			assertEquals(prima + attesi, capo.getEsperienza());
		}
	}

	@Test
	void nellaFugaSiPerdonoAlPiuMetaDellePergamene() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(14)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			int prima = partita.gruppo().getIncantesimi(TipoIncantesimo.FUOCO);
			assertTrue(prima >= 2);
			partita.gruppo().fugge();
			int dopo = partita.gruppo().getIncantesimi(TipoIncantesimo.FUOCO);
			assertTrue(dopo >= prima - prima / 2 && dopo <= prima, "da " + prima + " a " + dopo);
		}
	}

	@Test
	void ogniPersonaggioHaAlmenoUnBersaglio() {
		ModelloDati.setIstanza(new ModelloDati());
		for (com.threeamigos.foresta.tipi.TipoPersonaggio classe : com.threeamigos.foresta.tipi.TipoPersonaggio.values()) {
			assertTrue(FabbricaPersonaggi.crea(classe, 1).getBersagli() >= 1, classe.name());
		}
	}

	@Test
	void magoEMagaTiranoLeStatisticheComeGliAltriEroi() {
		ModelloDati.setIstanza(new ModelloDati());
		Dado.impostaSeme(3);
		double mago = 0, guerriero = 0;
		for (int i = 0; i < 200; i++) {
			mago += sommaPrimari(new Mago("M", 1));
			guerriero += sommaPrimari(new Guerriero("G", 1));
		}
		// Prima il Mago aveva 20 punti in meno da distribuire (il budget dei mostri)
		assertTrue(Math.abs(mago - guerriero) / 200 < 5, "mago " + mago / 200 + ", guerriero " + guerriero / 200);
	}

	@Test
	void equipaggiareUnArtefattoAggiornaISecondari() {
		ModelloDati.setIstanza(new ModelloDati());
		com.threeamigos.foresta.personaggi.Ladro ladro = new com.threeamigos.foresta.personaggi.Ladro("L", 1);
		int prima = ladro.getPrecisione();
		com.threeamigos.foresta.artefatti.Artefatto anello = com.threeamigos.foresta.artefatti.CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ANELLO)
				.setNome("anello di prova").setDescrizione("che fa mirare meglio").setLivello(1).setDanniBase(0)
				.setCostoAcquisto(1).setPeso(0)
				.setModificatore(TipoAttributo.DESTREZZA,
						TipoModificatore.VARIAZIONE_FISSA, 200)
				.costruisci();
		ladro.addArtefatto(anello);
		assertTrue(ladro.getPrecisione() > prima, "precisione " + prima + " -> " + ladro.getPrecisione());
		ladro.removeArtefatto(anello);
		assertEquals(prima, ladro.getPrecisione());
	}

	@Test
	void spendereIPuntiAbilitaAggiornaISecondari() {
		ModelloDati.setIstanza(new ModelloDati());
		com.threeamigos.foresta.personaggi.Ladro ladro = new com.threeamigos.foresta.personaggi.Ladro("L", 1);
		int prima = ladro.getPrecisione();
		ladro.getModelloDati().setPuntiAbilitaDisponibili(200);
		for (int i = 0; i < 200; i++) {
			ladro.spendiPuntoAbilita(TipoAttributo.DESTREZZA);
		}
		assertTrue(ladro.getPrecisione() > prima, "precisione " + prima + " -> " + ladro.getPrecisione());
	}

	@Test
	void aGruppoPienoNonSiOffreAiuto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(16)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			// Pieno vuol dire anche gli slot dei personaggi temporanei, quelli che un aiuto occuperebbe
			while (partita.gruppo().getNumeroPersonaggi() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
				partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			}
			com.threeamigos.foresta.personaggi.Centauro centauro = new com.threeamigos.foresta.personaggi.Centauro(1);
			for (int i = 0; i < 100; i++) {
				com.threeamigos.foresta.offerte.Offerta offerta = com.threeamigos.foresta.offerte.FabbricaOfferte.crea(centauro.getOfferta(TipoApproccio.CORRUZIONE));
				assertTrue(!(offerta instanceof com.threeamigos.foresta.offerte.AiutoGratuito)
						&& !(offerta instanceof com.threeamigos.foresta.offerte.AiutoMercenario), "offerto aiuto a gruppo pieno");
			}
		}
	}

	private static int sommaPrimari(Personaggio p) {
		return p.getForza() + p.getDestrezza() + p.getCostituzione() + p.getIntelligenza() + p.getSaggezza()
				+ p.getCarisma() + p.getFortuna();
	}

	private static CoordinateMD unaCasellaDiBosco() {
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
					return new CoordinateMD(x, y);
				}
			}
		}
		throw new AssertionError("nessuna casella di bosco");
	}

	private static int durata(Personaggio personaggio, TipoEffettoDiStato tipo) {
		return personaggio.getEffettiDiStato().stream()
				.filter(e -> e.getTipoEffettoDiStato() == tipo)
				.mapToInt(EffettoDiStatoMD::getDurata)
				.findFirst().orElse(0);
	}
}
