package com.threeamigos.foresta.statistiche;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoPuntiEsperienzaAssegnati;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePunteggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.personaggi.GestoreProgressione;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

	private StatisticheMD dati;
	private final List<Object> notifiche = new ArrayList<>();

	private void conUnaPartita() {
		dati = new StatisticheMD();
		Statistiche.setFornitoreDiDati(() -> dati);
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.azzera();
		notifiche.clear();
		BusEventi.iscriviti(NotificaVariazionePunteggio.class, notifiche::add);
		BusEventi.iscriviti(NotificaVariazionePuntiEsperienzaPersonaggio.class, notifiche::add);
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, notifiche::add);
	}

	@Test
	void senzaUnFornitoreLeStatisticheNonSannoDoveLeggere() {
		Statistiche.setFornitoreDiDati(null);
		assertThrows(IllegalStateException.class, Statistiche::getPunti);
	}

	@Test
	void iPuntiSiSommanoESiAnnunciano() {
		conUnaPartita();

		Statistiche.addPunti(30);
		Statistiche.addPunti(20);

		assertEquals(50, Statistiche.getPunti());
		assertEquals(2, notifiche.size());
		NotificaVariazionePunteggio seconda = (NotificaVariazionePunteggio) notifiche.get(1);
		assertEquals(30, seconda.getValorePrecedente());
		assertEquals(50, seconda.getNuovoValore());
	}

	@Test
	void l_esperienzaSiSommaEQuandoBastaIlLivelloDelMondoSale() {
		conUnaPartita();
		int perIlSecondo = GestoreProgressione.getXpNecessariPerLivello(2);

		Statistiche.addPuntiEsperienza(perIlSecondo - 1);
		assertEquals(1, Statistiche.getLivello());
		assertEquals(1, notifiche.size(), "solo la variazione dell'esperienza");

		Statistiche.addPuntiEsperienza(1);
		assertEquals(perIlSecondo, Statistiche.getPuntiEsperienza());
		assertEquals(2, Statistiche.getLivello());
		assertTrue(notifiche.get(notifiche.size() - 1) instanceof NotificaAumentoLivelloMondo);
		assertEquals(2, ((NotificaAumentoLivelloMondo) notifiche.get(notifiche.size() - 1)).getLivello());
		assertEquals(GestoreProgressione.getXpPerProssimoLivello(2), Statistiche.getPuntiEsperienzaPerProssimoLivello());
	}

	@Test
	void l_esperienzaDelleUccisioniArrivaDalBus() {
		conUnaPartita();
		Statistiche.registrati();

		BusEventi.pubblica(new InternoPuntiEsperienzaAssegnati(7));

		assertEquals(7, Statistiche.getPuntiEsperienza());
	}

	@Test
	void ilLivelloDelMondoSiPortaAQuelloVolutoPerLaModalitaDiProva() {
		conUnaPartita();

		Statistiche.impostaLivelloDelMondo(5);
		assertEquals(5, Statistiche.getLivello());
		assertEquals(GestoreProgressione.getXpNecessariPerLivello(5), Statistiche.getPuntiEsperienza());
		assertTrue(notifiche.get(notifiche.size() - 1) instanceof NotificaAumentoLivelloMondo, "salendo si annuncia come un aumento");

		notifiche.clear();
		Statistiche.impostaLivelloDelMondo(2);
		assertEquals(2, Statistiche.getLivello());
		assertEquals(1, notifiche.size(), "scendendo cambia l'esperienza ma non c'è nessun aumento da annunciare");
		assertTrue(notifiche.get(0) instanceof NotificaVariazionePuntiEsperienzaPersonaggio);
	}

	@Test
	void iTurniSiContanoEUnaPartitaNuovaAzzeraTutto() {
		conUnaPartita();
		String partita = Statistiche.getIdPartita();
		assertNotNull(partita);

		Statistiche.incrementaTurniGiocati();
		Statistiche.incrementaTurniGiocati();
		Statistiche.addPunti(10);
		assertEquals(2, Statistiche.getTurniGiocati());

		Statistiche.reimposta();

		assertEquals(0, Statistiche.getTurniGiocati());
		assertEquals(0, Statistiche.getPunti());
	}

	@Test
	void leNotificheDelleStatisticheDichiaranoIlLoroTipoEIlLoroValore() {
		NotificaVariazionePunteggio punteggio = new NotificaVariazionePunteggio(1, 2);
		assertEquals(TipoEvento.NOTIFICA_VARIAZIONE_PUNTEGGIO, punteggio.getTipoEvento());
		NotificaVariazionePuntiEsperienzaPersonaggio esperienza = new NotificaVariazionePuntiEsperienzaPersonaggio(3, 4);
		assertEquals(3, esperienza.getValorePrecedente());
		assertEquals(4, esperienza.getNuovoValore());
		assertEquals(5, new NotificaAumentoLivelloMondo(5).getLivello());
	}
}
