package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il raffreddore (RAFFREDDATO): a ogni azione di chi lo ha, una volta su due, il personaggio starnutisce (si scrive) e perde il turno: l'azione non parte e
 * non costa niente. Il tiro è un d100 contro {@link CostantiPersonaggi#RAFFREDDATO_PROBABILITA_STARNUTO}.
 */
class RaffreddatoTest {

	private static final int STARNUTISCE = 1;
	private static final int NON_STARNUTISCE = 100;

	@AfterEach
	void ripristina() {
		Dado.ripristina();
	}

	private static PartitaDiTest partitaInCombattimento(long seme) {
		PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		return partita;
	}

	private static boolean haScrittoLoStarnuto(PartitaDiTest partita) {
		return partita.testi().stream().anyMatch(t -> t.contains("starnutisce!"));
	}

	private static int saluteDegliAvversari() {
		int salute = 0;
		for (Personaggio avversario : GruppoAvversario.getIstanza().getPersonaggi()) {
			salute += avversario.getSalute();
		}
		return salute;
	}

	@Test
	void unAttaccoSingoloDiUnRaffreddatoSiPerdeSeStarnutisce() {
		try (PartitaDiTest partita = partitaInCombattimento(701)) {
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);
			int salute = saluteDegliAvversari();

			Dado.trucca(STARNUTISCE);
			partita.comando(Comando.SINGOLO_ATTACCO);

			assertTrue(haScrittoLoStarnuto(partita), partita.testi().toString());
			assertEquals(salute, saluteDegliAvversari(), "l'attacco non è partito: nessun avversario ha perso salute per colpa sua");
		}
	}

	@Test
	void seNonStarnutisceAttaccaComeSempre() {
		try (PartitaDiTest partita = partitaInCombattimento(702)) {
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);

			Dado.trucca(NON_STARNUTISCE);
			partita.comando(Comando.SINGOLO_ATTACCO);

			assertFalse(haScrittoLoStarnuto(partita), partita.testi().toString());
		}
	}

	@Test
	void senzaRaffreddoreNonSiStarnutisceMai() {
		try (PartitaDiTest partita = partitaInCombattimento(703)) {
			Dado.trucca(STARNUTISCE);
			partita.comando(Comando.SINGOLO_ATTACCO);

			assertFalse(haScrittoLoStarnuto(partita), partita.testi().toString());
		}
	}

	@Test
	void unaPozioneNonSiBeveSeSiStarnutisce() {
		try (PartitaDiTest partita = partitaInCombattimento(704)) {
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);
			partita.gruppo().addPozioniSalute(2 - partita.gruppo().getPozioniSalute());
			capo.addSalute(-(capo.getSalute() / 2));

			Dado.trucca(STARNUTISCE);
			partita.comando(Comando.POZIONE_SALUTE);

			assertTrue(haScrittoLoStarnuto(partita), partita.testi().toString());
			assertEquals(2, partita.gruppo().getPozioniSalute(), "la pozione resta dov'è");
		}
	}

	@Test
	void unaPozioneSiBeveSeNonSiStarnutisce() {
		try (PartitaDiTest partita = partitaInCombattimento(705)) {
			Personaggio capo = partita.gruppo().getCapo();
			capo.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);
			partita.gruppo().addPozioniSalute(2 - partita.gruppo().getPozioniSalute());
			capo.addSalute(-(capo.getSalute() / 2));

			Dado.trucca(NON_STARNUTISCE);
			partita.comando(Comando.POZIONE_SALUTE);

			assertFalse(haScrittoLoStarnuto(partita), partita.testi().toString());
			assertEquals(1, partita.gruppo().getPozioniSalute());
		}
	}

	@Test
	void nelCombattimentoAutomaticoIRaffreddatiStarnutisconoEPerdonoIlTurno() {
		try (PartitaDiTest partita = partitaInCombattimento(706)) {
			partita.gruppo().getCapo().addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);
			for (Personaggio avversario : GruppoAvversario.getIstanza().getPersonaggi()) {
				avversario.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, CostantiPersonaggi.RAFFREDDATO_TURNI, 0);
			}
			int salute = saluteDegliAvversari();
			int saluteDelCapo = partita.gruppo().getCapo().getSalute();

			// Tutti starnutiscono, a ogni passo del battito: nessuno attacca (i tiri a caso non servono, perché non si arriva a tirarli)
			Dado.trucca(STARNUTISCE, STARNUTISCE, STARNUTISCE, STARNUTISCE, STARNUTISCE, STARNUTISCE);
			partita.comando(Comando.COMBATTIMENTO);
			for (int passo = 0; passo < 4; passo++) {
				partita.scatta();
			}

			assertTrue(haScrittoLoStarnuto(partita), partita.testi().toString());
			assertEquals(salute, saluteDegliAvversari(), "nessuno ha colpito nessuno");
			assertEquals(saluteDelCapo, partita.gruppo().getCapo().getSalute());
		}
	}
}
