package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePunteggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.StatisticheMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

public class Statistiche {

	private static StatisticheMD getStatisticheMD() {
		return ModelloDati.getIstanza().getStatisticheMD();
	}

	private Statistiche() {
	}

	public static void addPunti(int quantita) {
		StatisticheMD md = getStatisticheMD();
		int valorePrecedente = md.getPunti();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPunti(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazionePunteggio(valorePrecedente, valoreAttuale));
	}

	// Chiamato ogni volta che il personaggio completa una missione o uccide un mostro
	public static void addPuntiEsperienza(int quantita) {
		StatisticheMD md = getStatisticheMD();
		int valorePrecedente = md.getPuntiEsperienza();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPuntiEsperienza(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazionePuntiEsperienzaPersonaggio(valorePrecedente, valoreAttuale));

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = md.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(md.getPuntiEsperienza());

		if (nuovoLivello > livelloAttuale) {
			md.setLivello(nuovoLivello);
			BusEventi.pubblica(new NotificaAumentoLivelloMondo(nuovoLivello));
		}
	}

	public static int getPunti() {
		return getStatisticheMD().getPunti();
	}

	public static int getPuntiEsperienza() {
		return getStatisticheMD().getPuntiEsperienza();
	}

	public static int getLivello() {
		return getStatisticheMD().getLivello();
	}

	public static int getPuntiEsperienzaPerProssimoLivello() {
		return GestoreProgressione.getXpPerProssimoLivello(getLivello());
	}

	public static void addMostroUcciso(ClassePersonaggio classe) {
		getStatisticheMD().addMostroUcciso(classe);
	}

	public static int getMostriUccisi(ClassePersonaggio classe) {
		return getStatisticheMD().getMostriUccisi(classe);
	}

	public static void incrementaTurniGiocati() {
		StatisticheMD md = getStatisticheMD();
		md.setTurniGiocati(md.getTurniGiocati() + 1);
	}

	public static int getTurniGiocati() {
		return getStatisticheMD().getTurniGiocati();
	}

	public static void reimposta() {
		getStatisticheMD().reimposta();
	}
}
