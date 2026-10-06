package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.ui.AccessoMappaGeneralePerTest;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

		// Partita 1: si rivela una casella lontana e si disegna la mappa generale
		Foresta.reimposta();
		CoordinateMD lontana = new CoordinateMD(Foresta.getDimensioneX() - 1, Foresta.getDimensioneY() - 1);
		Foresta.setLocazioneConosciuta(lontana);
		AccessoMappaGeneralePerTest.mappaGenerale();

		// Partita 2, come dopo aver perso e ricominciato: stessa casella non ancora rivelata
		Foresta.reimposta();
		BufferedImage mappaPartita2 = AccessoMappaGeneralePerTest.mappaGenerale();
		int pixelDopoReset = mappaPartita2.getRGB(
				lontana.getX() * AccessoMappaGeneralePerTest.larghezzaIcona(),
				lontana.getY() * AccessoMappaGeneralePerTest.altezzaIcona());

		// Trasparente (alpha 0): la casella non è ancora conosciuta nella partita 2
		assertEquals(0, pixelDopoReset >>> 24, "la casella dovrebbe apparire sconosciuta nella partita 2, pixel=" + Integer.toHexString(pixelDopoReset));
	}
}
