package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le statistiche tengono il conto di quanto succede in gioco iscrivendosi agli eventi.
 */
class StatisticheTest {

	@Test
	void contanoGliAvversariSconfittiPerClasse() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			assertEquals(0, Statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));

			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
			partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.TROLL));

			assertEquals(2, Statistiche.getMostriUccisi(TipoPersonaggio.GOBLIN));
			assertEquals(1, Statistiche.getMostriUccisi(TipoPersonaggio.TROLL));
		}
	}
}
