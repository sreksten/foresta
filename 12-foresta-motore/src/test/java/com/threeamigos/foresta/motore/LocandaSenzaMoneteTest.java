package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La locanda quando le monete non bastano per tutto il gruppo, o non bastano proprio, e quando si dorme o non si dorme: si passa da tutti i
 * ramificazioni con semi diversi (la locanda a volte fa incontrare qualcuno o dà informazioni).
 */
class LocandaSenzaMoneteTest {

	private static void inLocanda(PartitaDiTest partita, int monete, boolean pernotta) {
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		while (partita.gruppo().getNumeroPersonaggiPermanenti() < 3) {
			Personaggio compagno = new Guerriero("Compagno" + partita.gruppo().getNumeroPersonaggi(), 1);
			compagno.getModelloDati().setPng(false);
			partita.gruppo().aggiungiPersonaggio(compagno);
		}
		Cassa.subMonete(Cassa.getMonete());
		Cassa.addMonete(monete);
		partita.comando(Comando.LOCANDA);
		for (int passo = 0; passo < 12; passo++) {
			Collection<Comando> comandi = partita.comandiDisponibili();
			if (comandi.contains(Comando.LOCANDA) || comandi.isEmpty()) {
				break;
			}
			if (comandi.contains(Comando.SI) && comandi.contains(Comando.NO)) {
				partita.comando(pernotta ? Comando.SI : Comando.NO);
			} else if (comandi.contains(Comando.PERGAMENA)) {
				partita.comando(Comando.PERGAMENA);
			} else {
				partita.comando(comandi.iterator().next());
			}
		}
	}

	@Test
	void senzaAbbastanzaMonetePerTuttiMangiaUnoSolo() {
		for (long seme = 1; seme <= 12; seme++) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
				inLocanda(partita, CostantiEconomia.COSTO_PASTO, seme % 2 == 0);
				assertTrue(Cassa.getMonete() >= 0);
			}
		}
	}

	@Test
	void senzaNemmenoUnPastoLOsteNonFaCredito() {
		for (long seme = 1; seme <= 4; seme++) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
				inLocanda(partita, 0, false);
				assertEquals(0, Cassa.getMonete());
			}
		}
	}

	@Test
	void conAbbastanzaMonetePerIlPastoMaNonPerDormire() {
		for (long seme = 1; seme <= 12; seme++) {
			try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
				int pasto = CostantiEconomia.COSTO_PASTO * 3;
				inLocanda(partita, pasto + 1, true);
				assertTrue(GruppoGiocatore.getIstanza().getNumeroPersonaggi() >= 3);
			}
		}
	}
}
