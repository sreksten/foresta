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

	public static int getPunti() {
		return statisticheMD.getPunti();
	}

	public static void addMostroUcciso(ClassePersonaggio classe) {
		statisticheMD.addMostroUcciso(classe);
	}

	public static int getMostriUccisi(ClassePersonaggio classe) {
		return statisticheMD.getMostriUccisi(classe);
	}
}
