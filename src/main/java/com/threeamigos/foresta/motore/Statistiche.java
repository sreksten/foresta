package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.StatisticheMD;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Statistiche {

	private Statistiche() {
	}

	private static StatisticheMD statisticheMD = ModelloDati.getIstanza().getStatisticheMD();

	public static final void addPunti(int quantita) {
		statisticheMD.addPunti(quantita);
		UI.variaPunti(quantita);
	}

	public static final int getPunti() {
		return statisticheMD.getPunti();
	}

	public static final void addMostroUcciso(ClassiPersonaggio classe) {
		statisticheMD.addMostroUcciso(classe);
	}

	public static final int getMostriUccisi(ClassiPersonaggio classe) {
		return statisticheMD.getMostriUccisi(classe);
	}
}
