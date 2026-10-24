package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.threeamigos.foresta.missioni.RegistroMissioni;

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

		// Partita 1: si rivela tutta la mappa
		Foresta.reimposta();
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				Foresta.setLocazioneConosciuta(new CoordinateMD(x, y));
			}
		}
		assertTrue(Foresta.isLocazioneConosciuta(new CoordinateMD(0, 0)), "la mappa dovrebbe essere conosciuta nella partita 1");

		// Partita 2, come dopo aver perso e ricominciato (stesso processo, stesse istanze statiche): all'inizio ci sono
		// caselle già conosciute (le locazioni uniche, la zona di partenza) ma non tutte, qualunque sia il seme
		Foresta.reimposta();
		boolean esisteUnaCasellaSconosciuta = false;
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				if (!Foresta.isLocazioneConosciuta(new CoordinateMD(x, y))) {
					esisteUnaCasellaSconosciuta = true;
				}
			}
		}
		assertTrue(esisteUnaCasellaSconosciuta, "la conoscenza della mappa non dovrebbe sopravvivere alla partita 2");
	}
}
