package com.threeamigos.foresta.statistiche;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Le statistiche tengono il conto di quanto succede in gioco iscrivendosi agli eventi.
 */
class StatisticheTest {

	@AfterEach
	void ripristina() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(null);
		Statistiche.setFornitoreDiDati(null);
	}

	@Test
	void contanoGliAvversariSconfittiPerClasse() {
		StatisticheMD dati = new StatisticheMD();
		Statistiche.setFornitoreDiDati(() -> dati);
		// Senza il thread principale del gioco, gli eventi si consegnano subito
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.azzera();
		Statistiche.registrati();
		assertEquals(0, Statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));

		BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
		BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
		BusEventi.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));

		assertEquals(2, Statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));
		assertEquals(1, Statistiche.getMostriUccisi(TipoPersonaggio.TROLL));
	}
}
