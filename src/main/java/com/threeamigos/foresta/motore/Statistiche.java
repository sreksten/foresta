package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoAumentoLivelloMondo;
import com.threeamigos.foresta.eventi.EventoVariazionePunti;
import com.threeamigos.foresta.eventi.EventoVariazionePuntiEsperienza;
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
		int valorePrecedente = getStatisticheMD().getPunti();
		int valoreAttuale = valorePrecedente + quantita;
		getStatisticheMD().setPunti(valoreAttuale);
		BusEventi.pubblica(new EventoVariazionePunti(valorePrecedente, valoreAttuale));
	}

	// Chiamato ogni volta che il personaggio completa una missione o uccide un mostro
	public static void addPuntiEsperienza(int quantita) {
		int valorePrecedente = getStatisticheMD().getPuntiEsperienza();
		int valoreAttuale = valorePrecedente + quantita;
		getStatisticheMD().setPuntiEsperienza(valoreAttuale);
		BusEventi.pubblica(new EventoVariazionePuntiEsperienza(valorePrecedente, valoreAttuale));

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = getStatisticheMD().getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(getStatisticheMD().getPuntiEsperienza());

		if (nuovoLivello > livelloAttuale) {
			getStatisticheMD().setLivello(nuovoLivello);
			BusEventi.pubblica(new EventoAumentoLivelloMondo(nuovoLivello));
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
		getStatisticheMD().setTurniGiocati(getStatisticheMD().getTurniGiocati() + 1);
	}

	public static int getTurniGiocati() {
		return getStatisticheMD().getTurniGiocati();
	}

	public static void reimposta() {
		getStatisticheMD().reimposta();
	}
}
