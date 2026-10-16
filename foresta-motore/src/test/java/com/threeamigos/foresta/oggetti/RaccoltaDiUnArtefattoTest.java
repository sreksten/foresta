package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quando nessuno può prendere un oggetto raccolto, il messaggio dice perché: troppo pesante per chi lo potrebbe usare, o
 * semplicemente non lo usa nessuno.
 */
class RaccoltaDiUnArtefattoTest {

	private final List<String> paragrafi = new ArrayList<>();
	private final List<String> frasi = new ArrayList<>();
	private GruppoGiocatore gruppo;

	@BeforeEach
	void prepara() {
		ModelloDati.setIstanza(new ModelloDati());
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> paragrafi.add(e.getMessaggio()));
		BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio()));
		gruppo = new GruppoGiocatore();
	}

	@AfterEach
	void ripristina() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(null);
	}

	private static Artefatto spada(double peso) {
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TipoArtefatto.SPADA);
		md.setNome("la spada di prova");
		md.setDescrizione("che serve ai test");
		md.setLivello(1);
		md.setPeso(peso);
		return Artefatto.di(md);
	}

	@Test
	void sePerLaClasseUnoLoUsaMaNonLoRegge() {
		gruppo.aggiungiPersonaggio(new Guerriero("Pippo", 5));

		Artefatto.raccogli(gruppo, null, spada(100_000));

		assertEquals(1, paragrafi.size(), "frasi: " + frasi);
		assertTrue(paragrafi.get(0).contains("è troppo pesante da portare"), paragrafi.get(0));
	}

	@Test
	void seNessunoLoUsaNonEPerIlPeso() {
		gruppo.aggiungiPersonaggio(new Mago("Merlino", 5));

		Artefatto.raccogli(gruppo, null, spada(100_000));

		assertTrue(paragrafi.isEmpty(), "paragrafi: " + paragrafi);
		assertTrue(frasi.stream().anyMatch(f -> f.contains("nessuno può utilizzarlo in questo momento")), "frasi: " + frasi);
	}
}
