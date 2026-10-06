package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Riproduzione mirata del bug in todo.md: la conoscenza della mappa non dovrebbe
 * sopravvivere a un Foresta.reimposta() fra una partita e la successiva.
 */
class BugMappaReimpostataTest {

	@Test
	void laConoscenzaDellaMappaNonSopravviveAReimposta() {
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

		// Partita 1
		Foresta.reimposta();
		CoordinateMD lontana = new CoordinateMD(Foresta.getDimensioneX() - 1, Foresta.getDimensioneY() - 1);
		Foresta.setLocazioneConosciuta(lontana);
		assertTrue(Foresta.isLocazioneConosciuta(lontana), "la casella dovrebbe essere conosciuta nella partita 1");

		// Partita 2, come dopo aver perso e ricominciato (stesso processo, stesse istanze statiche)
		Foresta.reimposta();
		assertFalse(Foresta.isLocazioneConosciuta(lontana), "la casella non dovrebbe essere ancora conosciuta nella partita 2");
	}
}
