package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.ui.AccessoMappaGeneralePerTest;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Riproduzione del bug in todo.md passando per la cache statica di DisegnatoreMappa
 * (mappaGenerale/versioneMappaGenerata), riusando le stesse istanze statiche che la
 * UI reale userebbe fra una partita persa e la successiva (niente ModelloDati.setIstanza
 * di mezzo, esattamente come fa Automa.gestisciTestoInStatoPreGameAttesaNomePersonaggio).
 */
class BugCacheMappaGeneraleTest {

	@Test
	void laMappaGeneraleSiRicostruisceDopoUnaNuovaPartita() {
		BusEventi.azzera();
		Dado.ripristina();
		Dado.impostaSeme(1);
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		GruppoAvversario.azzeraIstanza();
		Notizie.registrati();
		Statistiche.registrati();
		RegistroArtefatti.registrati();
		RegistroMissioni.registrati();

		// Partita 1: si rivela tutta la mappa e si disegna la mappa generale
		CostruttoreDellaForesta.reimposta();
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				Foresta.setLocazioneConosciuta(new CoordinateMD(x, y));
			}
		}
		AccessoMappaGeneralePerTest.mappaGenerale();

		// Partita 2, come dopo aver perso e ricominciato: alcune caselle (non tutte, qualunque sia il seme) non sono ancora rivelate
		CostruttoreDellaForesta.reimposta();
		BufferedImage mappaPartita2 = AccessoMappaGeneralePerTest.mappaGenerale();
		boolean esisteUnaCasellaSconosciuta = false;
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				int pixel = mappaPartita2.getRGB(
						x * AccessoMappaGeneralePerTest.larghezzaIcona(),
						y * AccessoMappaGeneralePerTest.altezzaIcona());
				// Trasparente (alpha 0): la casella non è ancora conosciuta nella partita 2
				if ((pixel >>> 24) == 0) {
					esisteUnaCasellaSconosciuta = true;
				}
			}
		}
		assertTrue(esisteUnaCasellaSconosciuta, "la mappa generale dovrebbe avere caselle sconosciute nella partita 2");
	}
}
