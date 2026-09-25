package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.EffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Troll;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
			troll.attacca(guerriero);
			Dado.ripristina();
		}
		assertTrue(guerriero.getSalute() < salutePrima, "il troll non ha mai fatto danno");
	}

	@Test
	void inCombattimentoGliEffettiDiStatoScendonoAOgniRound() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(5)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CASTELLO_IDRA));
			partita.assertStato(Stato.IN_LOCAZIONE);
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO, 5, 0);

			partita.comando(Comando.COMBATTIMENTO);

			partita.assertStato(Stato.IN_COMBATTIMENTO);
			assertEquals(4, durata(capo, TipoEffettoDiStato.RALLENTATO), "l'effetto non e' sceso durante il round");
			partita.scatta();
			if (partita.stato() == Stato.IN_COMBATTIMENTO) {
				assertEquals(3, durata(capo, TipoEffettoDiStato.RALLENTATO));
			}
		}
	}

	@Test
	void accamparsiDopoAverCaricatoUnaPartitaNonVaInErrore() {
		GestoreSalvataggiInMemoria salvataggi;
		try (PartitaDiTest prima = PartitaDiTest.nuova(7)) {
			prima.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> prima.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			prima.comando(Comando.ESCI_DA_CITTA);
			// Ci si accampa nel bosco, non in citta': il gruppo va su una casella di bosco prima del salvataggio
			prima.gruppo().setCoordinate(unaCasellaDiBosco());
			prima.comando(Comando.FLOPPY).comando(Comando.NUMERO_1);
			salvataggi = prima.salvataggi();
		}
		try (PartitaDiTest dopo = PartitaDiTest.nuovaConSalvataggi(8, salvataggi)) {
			dopo.comando(Comando.FLOPPY).comando(Comando.NUMERO_1);
			dopo.assertStato(Stato.SCELTA_DIREZIONE);
			dopo.comando(Comando.ACCAMPAMENTO);
			dopo.assertStato(Stato.SCELTA_DIREZIONE);
		}
	}

	@Test
	void chiLasciaIlGruppoNonTornaDopoIlSalvataggio() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(9)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			Guerriero mercenario = new Guerriero("Mercenario", 1);
			partita.gruppo().aggiungiPersonaggio(mercenario);
			assertEquals(2, partita.gruppo().getNumeroPersonaggi());
			partita.gruppo().rimuoviPersonaggio(mercenario);

			GestoreSalvataggi.salva(Comando.NUMERO_2);
			assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
			assertEquals(1, partita.gruppo().getNumeroPersonaggi(), "il mercenario e' tornato nel gruppo");
		}
	}

	@Test
	void resurrezioneEMorteConsumanoMagia() {
		ModelloDati.setIstanza(new ModelloDati());
		Mago mago = new Mago("Merlino", 5);
		mago.addMagia(mago.getMagiaMassima());
		Guerriero compagno = new Guerriero("Compagno", 1);

		Incantesimo resurrezione = ClasseIncantesimo.RESURREZIONE.getIstanza(5);
		int prima = mago.getMagia();
		resurrezione.formula(mago, compagno, null);
		assertEquals(prima - resurrezione.getCostoLancio(), mago.getMagia());

		Incantesimo morte = ClasseIncantesimo.MORTE.getIstanza(5);
		prima = mago.getMagia();
		morte.formula(mago, new Troll(1), null);
		assertEquals(prima - morte.getCostoLancio(), mago.getMagia());
	}

	@Test
	void dopoMezzanotteSiRiposaFinoAlleOtto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(12)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
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
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			Personaggio capo = partita.gruppo().getCapo();
			int prima = capo.getEsperienza();
			int attesi = GestoreProgressione.getXpRichiestiPerProssimoLivello(Statistiche.getLivello()) * 20 / 100;
			assertTrue(attesi > 0);
			GestoreProgressione.completaMissioneSecondaria();
			assertEquals(prima + attesi, capo.getEsperienza());
		}
	}

	@Test
	void nellaFugaSiPerdonoAlPiuMetaDellePergamene() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(14)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			int prima = partita.gruppo().getIncantesimi(ClasseIncantesimo.FUOCO);
			assertTrue(prima >= 2);
			partita.gruppo().fugge();
			int dopo = partita.gruppo().getIncantesimi(ClasseIncantesimo.FUOCO);
			assertTrue(dopo >= prima - prima / 2 && dopo <= prima, "da " + prima + " a " + dopo);
		}
	}

	@Test
	void ogniPersonaggioHaAlmenoUnBersaglio() {
		ModelloDati.setIstanza(new ModelloDati());
		for (com.threeamigos.foresta.personaggi.ClassePersonaggio classe : com.threeamigos.foresta.personaggi.ClassePersonaggio.values()) {
			assertTrue(classe.getIstanza(1).getBersagli() >= 1, classe.name());
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
		com.threeamigos.foresta.oggetti.Artefatto anello = com.threeamigos.foresta.tools.CostruttoreArtefatto.istanza()
				.setTipo(com.threeamigos.foresta.motore.modellodati.TipoArtefatto.ANELLO)
				.setNome("anello di prova").setDescrizione("che fa mirare meglio").setLivello(1).setDanniBase(0)
				.setCostoAcquisto(1).setPeso(0)
				.setModificatore(com.threeamigos.foresta.motore.modellodati.TipoAttributo.DESTREZZA,
						com.threeamigos.foresta.motore.modellodati.TipoModificatore.AUMENTO_FISSO, 200)
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
			ladro.spendiPuntoAbilita(com.threeamigos.foresta.motore.modellodati.TipoAttributo.DESTREZZA);
		}
		assertTrue(ladro.getPrecisione() > prima, "precisione " + prima + " -> " + ladro.getPrecisione());
	}

	@Test
	void aGruppoPienoNonSiOffreAiuto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(16)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			while (partita.gruppo().getNumeroPersonaggi() < Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
				partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			}
			com.threeamigos.foresta.personaggi.Centauro centauro = new com.threeamigos.foresta.personaggi.Centauro(1);
			for (int i = 0; i < 100; i++) {
				com.threeamigos.foresta.offerte.Offerta offerta = centauro.getOfferta(Comando.CORRUZIONE);
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
				if (Foresta.getLocazione(x, y) == ClassiLocazione.BOSCO) {
					return new CoordinateMD(x, y);
				}
			}
		}
		throw new AssertionError("nessuna casella di bosco");
	}

	private static int durata(Personaggio personaggio, TipoEffettoDiStato tipo) {
		return personaggio.getEffettiDiStato().stream()
				.filter(e -> e.getTipoEffettoDiStato() == tipo)
				.mapToInt(EffettoDiStato::getDurata)
				.findFirst().orElse(0);
	}
}
