package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.StatisticheMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Statistiche {

	private Statistiche() {
	}

	private static final StatisticheMD statisticheMD = ModelloDati.getIstanza().getStatisticheMD();

	public static void addPunti(int quantita) {
		statisticheMD.addPunti(quantita);
		UI.variaPunti(quantita);
	}

	// Chiamato ogni volta che il personaggio completa una missione o uccide un mostro
	public static void addPuntiEsperienza(int ammontareXp) {
		statisticheMD.addPuntiEsperienza(ammontareXp);

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = statisticheMD.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(statisticheMD.getPuntiEsperienza());

		if (nuovoLivello > livelloAttuale) {
			statisticheMD.setLivello(nuovoLivello);
			UI.notifica("LEVELED UP! Ora il mondo è al livello " + nuovoLivello + "!");
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
}
