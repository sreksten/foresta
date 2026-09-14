package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMessaggio;
import com.threeamigos.foresta.eventi.EventoVariazioneDisponibilitaConsumabile;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.StatisticheMD;
import com.threeamigos.foresta.motore.modellodati.TipoConsumabile;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

public class Statistiche {

	private Statistiche() {
	}

	private static final StatisticheMD statisticheMD = ModelloDati.getIstanza().getStatisticheMD();

	public static void addPunti(int quantita) {
		statisticheMD.addPunti(quantita);
		BusEventi.pubblica(new EventoVariazioneDisponibilitaConsumabile(TipoConsumabile.PUNTI_ESPERIENZA, quantita));
	}

	// Chiamato ogni volta che il personaggio completa una missione o uccide un mostro
	public static void addPuntiEsperienza(int ammontareXp) {
		statisticheMD.addPuntiEsperienza(ammontareXp);

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = statisticheMD.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(statisticheMD.getPuntiEsperienza());

		if (nuovoLivello > livelloAttuale) {
			statisticheMD.setLivello(nuovoLivello);
			BusEventi.pubblica(new EventoMessaggio("LEVELED UP! Ora il mondo è al livello " + nuovoLivello + "!"));
		}
	}

	public static int getPunti() {
		return statisticheMD.getPunti();
	}

	public static int getPuntiEsperienza() {
		return statisticheMD.getPuntiEsperienza();
	}

	public static int getLivello() {
		return statisticheMD.getLivello();
	}

	public static int getPuntiEsperienzaPerProssimoLivello() {
		return GestoreProgressione.getXpPerProssimoLivello(getLivello());
	}

	public static void addMostroUcciso(ClassePersonaggio classe) {
		statisticheMD.addMostroUcciso(classe);
	}

	public static int getMostriUccisi(ClassePersonaggio classe) {
		return statisticheMD.getMostriUccisi(classe);
	}

	public static int getTurniGiocati() {
		return statisticheMD.getTurniGiocati();
	}

	public static int incrementaTurniGiocati() {
		statisticheMD.setTurniGiocati(statisticheMD.getTurniGiocati() + 1);
		return statisticheMD.getTurniGiocati();
	}
}
