package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.dado.Dado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un anello non magico non ha effetto, ma si vende come una gemma: preso, vale un prezioso.
 */
class AnelloNonMagicoTest {

	private final List<String> frasi = new ArrayList<>();
	private GruppoGiocatore gruppo;

	@BeforeEach
	void prepara() {
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio()));
		gruppo = new GruppoGiocatore();
		gruppo.aggiungiPersonaggio(new Guerriero("Pippo", 5));
		Cassa.reimposta();
	}

	@AfterEach
	void ripristina() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(null);
		Dado.ripristina();
	}

	private static Anello anello(boolean magico) {
		for (int seme = 0; seme < 1000; seme++) {
			Dado.impostaSeme(seme);
			Anello anello = new Anello();
			if (anello.isMagico() == magico) {
				return anello;
			}
		}
		throw new IllegalStateException("nessun anello " + (magico ? "magico" : "non magico") + " trovato");
	}

	@Test
	void unAnelloNonMagicoVaiComeUnaGemma() {
		Anello anello = anello(false);
		int preziosi = Cassa.getPreziosi();

		assertTrue(RaccoltaBottino.raccogli(anello, gruppo, null));

		assertEquals(preziosi + anello.getQuantita(), Cassa.getPreziosi());
		assertTrue(frasi.stream().anyMatch(f -> f.contains("si può vendere come una gemma")), frasi.toString());
	}

	@Test
	void unAnelloMagicoNonDaPreziosi() {
		Anello anello = anello(true);
		int preziosi = Cassa.getPreziosi();

		RaccoltaBottino.raccogli(anello, gruppo, null);

		assertEquals(preziosi, Cassa.getPreziosi());
		assertFalse(frasi.stream().anyMatch(f -> f.contains("come una gemma")), frasi.toString());
	}
}
