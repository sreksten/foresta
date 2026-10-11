package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * Il testo che sale sopra un personaggio quando, per la sua classe, è debole, resiste o è immune a un tipo di danno: dice al
 * giocatore anche a quale ("Debole a Sacro", "Resiste a Fuoco", "Immune a Aria").
 */
final class TestoReazioneDiClasse {

	private TestoReazioneDiClasse() {
	}

	/**
	 * @param moltiplicatore per quanto si moltiplica quel danno contro il personaggio: 0 immune, tra 0 e 1 resiste, più di 1
	 *                       debole
	 */
	static String di(TipoDanno tipoDanno, double moltiplicatore) {
		String prefisso = moltiplicatore == 0.0d ? "Immune a " : moltiplicatore < 1.0d ? "Resiste a " : "Debole a ";
		return prefisso + tipoDanno.getNome();
	}

	/**
	 * Verde per una debolezza, rosso per una resistenza o un'immunità
	 */
	static DoomdarkColorModel.Color colore(double moltiplicatore) {
		return moltiplicatore < 1.0d ? DoomdarkColorModel.Color.RED : DoomdarkColorModel.Color.GREEN;
	}
}
