package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tutorial.PilotaggioDelTutorial;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il tutorial pilota i primi cinque turni: un mostro solo per turno (arpia, chimera drago, scheletro, goblin, folletto), qualunque sia il
 * caso; poi, o a tutorial spento, i dadi tornano al caso.
 */
class PilotaggioDelTutorialTest {

	@AfterEach
	void ripulisce() {
		Dado.impostaTruccatore(null);
		TrucchiDiSviluppo.istanza().spegniTutto();
		RegistroSuggerimenti.setAttivo(true);
	}

	private static PartitaDiTest partita(long seme) {
		PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		// PartitaDiTest spegne il tutorial: qui lo si prova
		RegistroSuggerimenti.setAttivo(true);
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
	void iPrimiCinqueTurniHannoIlMostroDelTutorialUnoSolo() {
		try (PartitaDiTest partita = partita(31)) {
			Dado.impostaTruccatore(new PilotaggioDelTutorial(null));
			TipoPersonaggio[] attesi = {TipoPersonaggio.ARPIA, TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GOBLIN,
					TipoPersonaggio.FOLLETTO};
			for (int turno = 0; turno < attesi.length; turno++) {
				assertEquals(turno, Statistiche.getTurniGiocati());
				// Qualunque sia il caso: la chimera drago non è fra quelli ammessi all'inizio, e l'incontro c'è sempre
				for (int prova = 0; prova < 30; prova++) {
					List<Personaggio> mostri = incontro();
					assertEquals(1, mostri.size(), "turno " + turno);
					assertEquals(attesi[turno], mostri.get(0).getClasse(), "turno " + turno);
				}
				Statistiche.incrementaTurniGiocati();
			}
		}
	}

	@Test
	void finitaLaSequenzaIlTutorialNonDecideNiente() {
		try (PartitaDiTest partita = partita(32)) {
			for (int i = 0; i < 5; i++) {
				Statistiche.incrementaTurniGiocati();
			}
			assertFalse(PilotaggioDelTutorial.pilotaQuestoTurno());
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
}
