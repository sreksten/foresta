package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * L'ordine in cui la UI mostra i personaggi del combattimento (StatoDelCombattimento.getOrdineDelRound): lo stesso del piano del
 * round, nell'ordine che i gruppi avevano all'inizio: un mio e un avversario a turno, con i caduti e chi è in panchina al loro posto.
 */
class OrdineDelRoundTest {

	@Test
	void sonoAlternatiEChiCadeORestaInPanchinaRestaAlSuoPosto() {
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

			// 2 miei contro 3 avversari: parte dal mio primo, poi l'avversario che lo segue nel giro (non il primo degli avversari)
			List<Personaggio> atteso = java.util.Arrays.asList(capo, goblin.get(1), compagno, goblin.get(2), goblin.get(0));
			assertEquals(atteso, stato.getOrdineDelRound());

			// Chi va in panchina resta al suo posto, mescolato agli altri
			goblin.get(0).setInPanchina(true);
			assertEquals(atteso, stato.getOrdineDelRound());

			// L'ordine non cambia nemmeno a giro avviato, né se qualcuno muore
			stato.registraAzione(capo);
			goblin.get(1).subSalute(goblin.get(1).getSalute() + 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
			assertFalse(goblin.get(1).isVivo());
			assertEquals(atteso, stato.getOrdineDelRound());

			// Un'ondata rifà la lista da capo, con gli avversari nuovi
			avversari.reimposta();
			Personaggio primo = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
			Personaggio secondo = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
			avversari.aggiungiPersonaggio(primo);
			avversari.aggiungiPersonaggio(secondo);
			stato.nuovaOndata();
			assertEquals(java.util.Arrays.asList(capo, primo, compagno, secondo), stato.getOrdineDelRound());
		}
	}

	@Test
	void ilMotoreFaAgireNellOrdineMostratoAncheConUnMorto() {
		try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(78)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			Personaggio capo = partita.gruppo().getCapo();
			Personaggio morto = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 1);
			Personaggio terzo = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
			partita.gruppo().aggiungiPersonaggio(morto);
			partita.gruppo().aggiungiPersonaggio(terzo);
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
			stato.getOrdineDelRound();
			morto.subSalute(morto.getSalute() + 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);

			// A video: capo, goblin 0, morto, goblin 1, terzo, goblin 2. Chi è morto non agisce: gli altri nello stesso ordine
			List<Personaggio> attesi = java.util.Arrays.asList(capo, goblin.get(0), goblin.get(1), terzo, goblin.get(2));
			List<Personaggio> agiti = new ArrayList<>();
			for (int i = 0; i < attesi.size(); i++) {
				StatoDelCombattimento.Lato lato = stato.getProssimoLato();
				Personaggio chi = stato.getProssimoAttaccante(lato == StatoDelCombattimento.Lato.AVVERSARI ? avversari : partita.gruppo());
				agiti.add(chi);
				stato.registraAzione(chi);
			}
			assertEquals(attesi, agiti);
			assertEquals(2, stato.getRound(), "con l'ultimo il round è finito");
		}
	}
}
