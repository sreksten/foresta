package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'ordine in cui la UI mostra i personaggi del combattimento (StatoDelCombattimento.getOrdineDelRound): lo stesso del piano del
 * round, un mio e un avversario, e chi non può agire in fondo.
 */
class OrdineDelRoundTest {

	@Test
	void sonoAlternatiESeguonoIlPianoPoiVengonoICaduti() {
		try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(77)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			Personaggio capo = partita.gruppo().getCapo();
			Personaggio compagno = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
			partita.gruppo().aggiungiPersonaggio(compagno);
			GruppoAvversario avversari = GruppoAvversario.getIstanza();
			avversari.reimposta();
			List<Personaggio> goblin = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				Personaggio g = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
				goblin.add(g);
				avversari.aggiungiPersonaggio(g);
			}
			StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
			stato.reimposta();

			// Piano: G A G A A
			assertEquals(java.util.Arrays.asList(capo, goblin.get(0), compagno, goblin.get(1), goblin.get(2)), stato.getOrdineDelRound());

			// Un avversario caduto resta nella lista, in fondo, quando il piano si rifà
			goblin.get(0).setInPanchina(true);
			stato.nuovaOndata();
			List<Personaggio> ordine = stato.getOrdineDelRound();
			assertEquals(5, ordine.size());
			assertEquals(goblin.get(0), ordine.get(4), "chi non può agire sta in fondo");
			assertEquals(java.util.Arrays.asList(capo, goblin.get(1), compagno, goblin.get(2)), ordine.subList(0, 4));
		}
	}
}
