package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.locazioni.Radura;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.PilotaggioDelTutorial;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il tutorial pilota i primi cinque passi: un mostro solo per passo (arpia, chimera drago, scheletro, goblin, folletto), qualunque sia il caso. Il
 * passo dipende dai suggerimenti che il giocatore ha già visto, e vale per tutta la locazione; finita la sequenza, a tutorial spento o con
 * un'interfaccia che non li mostra, i dadi tornano al caso.
 */
class PilotaggioDelTutorialTest {

	@AfterEach
	void ripulisce() {
		Dado.impostaTruccatore(null);
		TrucchiDiSviluppo.istanza().spegniTutto();
		RegistroSuggerimenti.ripristina();
		RegistroSuggerimenti.setMostrabile(false);
	}

	/** Il giocatore è al passo n: ha visto i primi n suggerimenti che sbloccano, e comincia una locazione. */
	private static void alPasso(int passo) {
		RegistroSuggerimenti.ripristina();
		PilotaggioDelTutorial.ricomincia();
		for (int i = 0; i < passo; i++) {
			RegistroSuggerimenti.visto(PilotaggioDelTutorial.SUGGERIMENTI_CHE_SBLOCCANO[i]);
		}
		// Si avanza di un passo per locazione: tante locazioni quanti i passi (una in più non cambia niente)
		for (int locazione = 0; locazione <= passo; locazione++) {
			PilotaggioDelTutorial.iniziaLocazione();
		}
	}

	private static PartitaDiTest partita(long seme) {
		PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		// PartitaDiTest spegne il tutorial: qui lo si prova, con un'interfaccia che mostra i suggerimenti
		RegistroSuggerimenti.ripristina();
		RegistroSuggerimenti.setMostrabile(true);
		PilotaggioDelTutorial.iniziaLocazione();
		return partita;
	}

	/** Gli avversari che un bosco crea nel turno in cui siamo. */
	private static List<Personaggio> incontro() {
		GruppoAvversario avversari = GruppoAvversario.getIstanza();
		avversari.reimposta();
		new Bosco().crea(GruppoGiocatore.getIstanza(), avversari);
		return avversari.getPersonaggiVivi();
	}

	@Test
	void iPrimiCinquePassiHannoIlMostroDelTutorialUnoSolo() {
		try (PartitaDiTest partita = partita(31)) {
			Dado.impostaTruccatore(new PilotaggioDelTutorial(null));
			TipoPersonaggio[] attesi = {TipoPersonaggio.ARPIA, TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GOBLIN,
					TipoPersonaggio.FOLLETTO};
			for (int passo = 0; passo < attesi.length; passo++) {
				alPasso(passo);
				// Qualunque sia il caso: la chimera drago non è fra quelli ammessi all'inizio, e l'incontro c'è sempre
				for (int prova = 0; prova < 30; prova++) {
					List<Personaggio> mostri = incontro();
					assertEquals(1, mostri.size(), "passo " + passo);
					assertEquals(attesi[passo], mostri.get(0).getClasse(), "passo " + passo);
				}
			}
		}
	}

	@Test
	void ilPassoDipendeDaiSuggerimentiVistiNonDalNumeroDelTurno() {
		try (PartitaDiTest partita = partita(32)) {
			// Passano i turni ma il giocatore non ha letto niente: si ripete il primo passo
			Statistiche.incrementaTurniGiocati();
			Statistiche.incrementaTurniGiocati();
			assertEquals(TipoPersonaggio.ARPIA, PilotaggioDelTutorial.mostroDelTurno().get());
			// Un suggerimento che non sblocca niente di per sé non fa avanzare
			RegistroSuggerimenti.visto(TipoSuggerimento.MISSIONI);
			PilotaggioDelTutorial.ricomincia();
			PilotaggioDelTutorial.iniziaLocazione();
			assertEquals(TipoPersonaggio.ARPIA, PilotaggioDelTutorial.mostroDelTurno().get());
			// Conta l'ordine: il secondo senza il primo non basta
			RegistroSuggerimenti.visto(TipoSuggerimento.SCELTE_IN_COMBATTIMENTO);
			PilotaggioDelTutorial.iniziaLocazione();
			assertEquals(TipoPersonaggio.ARPIA, PilotaggioDelTutorial.mostroDelTurno().get());
			RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
			PilotaggioDelTutorial.iniziaLocazione();
			assertEquals(TipoPersonaggio.CHIMERA_DRAGO, PilotaggioDelTutorial.mostroDelTurno().get());
			PilotaggioDelTutorial.iniziaLocazione();
			assertEquals(TipoPersonaggio.SCHELETRO, PilotaggioDelTutorial.mostroDelTurno().get());
		}
	}

	@Test
	void ilPassoSiFissaAllInizioDellaLocazioneEUnSuggerimentoLettoInMezzoNonCambiaNiente() {
		try (PartitaDiTest partita = partita(38)) {
			Dado.impostaTruccatore(new PilotaggioDelTutorial(null));
			alPasso(0);
			assertEquals(0, colpiDelMostroASegno());
			// Durante lo scontro con l'arpia il giocatore legge la spiegazione del combattimento: la protezione non cade
			RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
			assertEquals(TipoPersonaggio.ARPIA, PilotaggioDelTutorial.mostroDelTurno().get());
			assertTrue(PilotaggioDelTutorial.isPrimoScontro());
			assertEquals(100, new PilotaggioDelTutorial(null).lancio(ContestoLancio.COLPO_DEI_MOSTRI, 1, 100).getAsInt());
			// Alla locazione dopo, il passo è avanzato
			PilotaggioDelTutorial.iniziaLocazione();
			assertEquals(TipoPersonaggio.CHIMERA_DRAGO, PilotaggioDelTutorial.mostroDelTurno().get());
			assertFalse(PilotaggioDelTutorial.isPrimoScontro());
		}
	}

	@Test
	void siAvanzaDiUnPassoPerLocazioneAncheSeIlGiocatoreHaGiaVistoDiPiu() {
		try (PartitaDiTest partita = partita(42)) {
			// Per caso, già nel primo scontro il giocatore ha letto tre dei suggerimenti che sbloccano (un oggetto trovato, per esempio)
			PilotaggioDelTutorial.ricomincia();
			PilotaggioDelTutorial.iniziaLocazione();
			RegistroSuggerimenti.visto(TipoSuggerimento.COMBATTIMENTO);
			RegistroSuggerimenti.visto(TipoSuggerimento.SCELTE_IN_COMBATTIMENTO);
			RegistroSuggerimenti.visto(TipoSuggerimento.OGGETTO_TROVATO);
			// ...ma nessun passo si salta: arpia, chimera drago, scheletro, goblin
			TipoPersonaggio[] attesi = {TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GOBLIN};
			for (TipoPersonaggio mostro : attesi) {
				PilotaggioDelTutorial.iniziaLocazione();
				assertEquals(mostro, PilotaggioDelTutorial.mostroDelTurno().get());
			}
		}
	}

	@Test
	void finitaLaSequenzaIlTutorialNonDecideNiente() {
		try (PartitaDiTest partita = partita(39)) {
			alPasso(5);
			assertFalse(PilotaggioDelTutorial.pilotaQuestoTurno());
			assertFalse(new PilotaggioDelTutorial(null).lancio(ContestoLancio.NUMERO_MOSTRI, 1, 3).isPresent());
		}
	}

	@Test
	void dopoTroppiTurniNonPilotaPiuAnchePassiNonSbloccati() {
		try (PartitaDiTest partita = partita(40)) {
			for (int i = 0; i < PilotaggioDelTutorial.TURNI_MASSIMI; i++) {
				assertTrue(PilotaggioDelTutorial.pilotaQuestoTurno(), "turno " + i);
				Statistiche.incrementaTurniGiocati();
			}
			assertFalse(PilotaggioDelTutorial.pilotaQuestoTurno());
		}
	}

	@Test
	void conUnInterfacciaCheNonMostraISuggerimentiNonPilota() {
		try (PartitaDiTest partita = partita(41)) {
			RegistroSuggerimenti.setMostrabile(false);
			assertFalse(PilotaggioDelTutorial.pilotaQuestoTurno());
			assertFalse(PilotaggioDelTutorial.isPrimoScontro());
			assertFalse(new PilotaggioDelTutorial(null).lancio(ContestoLancio.NUMERO_MOSTRI, 1, 3).isPresent());
		}
	}

	@Test
	void aTutorialSpentoNonDecideNiente() {
		try (PartitaDiTest partita = partita(33)) {
			RegistroSuggerimenti.setAttivo(false);
			assertFalse(PilotaggioDelTutorial.pilotaQuestoTurno());
			assertFalse(new PilotaggioDelTutorial(null).lancio(ContestoLancio.NUMERO_MOSTRI, 1, 3).isPresent());
			assertFalse(new PilotaggioDelTutorial(null).scelta(ContestoLancio.TIPO_MOSTRO, Arrays.asList(TipoPersonaggio.ARPIA)).isPresent());
		}
	}

	@Test
	void iTrucchiDellaFinestraDiProvaHannoLaPrecedenza() {
		try (PartitaDiTest partita = partita(34)) {
			TrucchiDiSviluppo.istanza().accendiElemento(ContestoLancio.TIPO_MOSTRO, TipoPersonaggio.HOBGOBLIN);
			TrucchiDiSviluppo.istanza().accendiNumero(ContestoLancio.NUMERO_MOSTRI, 2);
			Dado.impostaTruccatore(new PilotaggioDelTutorial(TrucchiDiSviluppo.istanza()));
			List<Personaggio> mostri = incontro();
			assertEquals(TipoPersonaggio.HOBGOBLIN, mostri.get(0).getClasse());
			assertTrue(mostri.size() <= 2);
		}
	}

	/** Quante volte, su 300 colpi di un mostro contro il capo, vanno a segno. */
	private static int colpiDelMostroASegno() {
		Personaggio mostro = incontro().get(0);
		Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
		int asegno = 0;
		for (int i = 0; i < 300; i++) {
			if (CalcolatoreCombattimento.colpisce(mostro, capo, CategoriaDanno.FISICO)) {
				asegno++;
			}
		}
		return asegno;
	}

	@Test
	void ilPrimoScontroEQuelloConLoScheletroNonSiPossonoPerdere() {
		try (PartitaDiTest partita = partita(35)) {
			Dado.impostaTruccatore(new PilotaggioDelTutorial(null));
			// Passo 0, l'arpia: nessun colpo a segno
			alPasso(0);
			assertEquals(0, colpiDelMostroASegno());
			// Passo 1, la chimera drago: non è protetta
			alPasso(1);
			assertTrue(colpiDelMostroASegno() > 0);
			// Passo 2, lo scheletro: protetto
			alPasso(2);
			assertEquals(0, colpiDelMostroASegno());
			// Passo 3, il goblin: non è protetto
			alPasso(3);
			assertTrue(colpiDelMostroASegno() > 0);
		}
	}

	@Test
	void ilTutorialDecideLeAltreCoseDeiPrimiPassiSoloNelLoroPasso() {
		try (PartitaDiTest partita = partita(36)) {
			PilotaggioDelTutorial pilota = new PilotaggioDelTutorial(null);
			// Passo 0, l'arpia: niente incantesimi (una voce con una probabilità sotto il 100 non vale), niente di altro
			alPasso(0);
			assertEquals(100, pilota.lancio(ContestoLancio.TATTICA_DEI_MOSTRI, 1, 100).getAsInt());
			assertFalse(pilota.lancio(ContestoLancio.PASSAGGIO_INOSSERVATO, 1, 100).isPresent());
			assertFalse(pilota.lancio(ContestoLancio.CORRUZIONE, 1, 10).isPresent());
			// Passo 1, la chimera drago: passare inosservati riesce sempre (un tiro di 1 è sotto qualunque probabilità)
			alPasso(1);
			assertEquals(1, pilota.lancio(ContestoLancio.PASSAGGIO_INOSSERVATO, 1, 100).getAsInt());
			assertFalse(pilota.lancio(ContestoLancio.TATTICA_DEI_MOSTRI, 1, 100).isPresent());
			// Passo 3, il goblin: la corruzione riesce (il tiro più alto supera la soglia)
			alPasso(3);
			assertEquals(10, pilota.lancio(ContestoLancio.CORRUZIONE, 1, 10).getAsInt());
			assertFalse(pilota.lancio(ContestoLancio.AMICIZIA, 1, 20).isPresent());
			// Passo 4, il folletto: l'amicizia riesce (il tiro più basso è sotto il carisma) e non c'è la sfida a carta, forbici e sasso
			alPasso(4);
			assertEquals(1, pilota.lancio(ContestoLancio.AMICIZIA, 1, 20).getAsInt());
			assertEquals(100, pilota.lancio(ContestoLancio.SFIDA_CARTA_FORBICI_SASSO, 1, 100).getAsInt());
			assertFalse(pilota.lancio(ContestoLancio.CORRUZIONE, 1, 10).isPresent());
		}
	}

	@Test
	void ilPrimoOggettoTrovatoEUnArmaChePuoUsareIlCapoFincheNonSiVedeIlSuggerimento() {
		try (PartitaDiTest partita = partita(37)) {
			Dado.impostaTruccatore(new PilotaggioDelTutorial(null));
			alPasso(0);
			// Il capo è un guerriero: la mazza, anche in una radura, che non lascia oggetti, e non una gemma a caso
			assertEquals(TipoBottino.MAZZA, PilotaggioDelTutorial.oggettoDelTurno().get());
			for (int prova = 0; prova < 30; prova++) {
				Radura radura = new Radura();
				GruppoAvversario.getIstanza().reimposta();
				radura.crea(GruppoGiocatore.getIstanza(), GruppoAvversario.getIstanza());
				assertNotNull(radura.getOggetto(), "prova " + prova);
				assertEquals(TipoBottino.MAZZA, radura.getOggetto().getClasse());
			}
			// Vale a qualunque passo, finché il giocatore non ha letto il suggerimento dell'oggetto trovato
			alPasso(2);
			assertEquals(TipoBottino.MAZZA, PilotaggioDelTutorial.oggettoDelTurno().get());
			// Letto, gli oggetti tornano al caso; e a tutorial spento non si decide niente
			RegistroSuggerimenti.visto(TipoSuggerimento.OGGETTO_TROVATO);
			assertFalse(PilotaggioDelTutorial.oggettoDelTurno().isPresent());
			RegistroSuggerimenti.ripristina();
			RegistroSuggerimenti.setAttivo(false);
			assertFalse(PilotaggioDelTutorial.oggettoDelTurno().isPresent());
		}
	}
}
