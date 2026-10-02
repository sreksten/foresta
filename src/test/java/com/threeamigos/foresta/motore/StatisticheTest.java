package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le statistiche tengono il conto di quanto succede in gioco iscrivendosi agli eventi.
 */
class StatisticheTest {

	@Test
	void contanoGliAvversariSconfittiPerClasse() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			assertEquals(0, Statistiche.getMostriUccisi(ClassePersonaggio.GOBLIN));

			partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
			partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.GOBLIN));
			partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.TROLL));

			assertEquals(2, Statistiche.getMostriUccisi(ClassePersonaggio.GOBLIN));
			assertEquals(1, Statistiche.getMostriUccisi(ClassePersonaggio.TROLL));
		}
	}
}
