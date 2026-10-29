package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.tipi.TipoDanno;

import java.awt.*;

/**
 * Il testo che sale sopra un personaggio quando, per la sua classe, è debole, resiste o è immune a un tipo di danno: dice al
 * giocatore anche a quale ("Debole a Sacro", "Resiste a Fuoco", "Immune a Aria"), e il colore dice quanto: dal grigio verso il
 * verde man mano che è più debole (fino al doppio del danno), dal grigio verso il rosso man mano che resiste di più, rosso
 * acceso se è immune. Il colore ha {@link #SFUMATURE} gradi per parte.
 */
public final class TestoReazioneDiClasse {

	/** Quante sfumature ha il passaggio dal grigio al verde (e dal grigio al rosso) */
	static final int SFUMATURE = 10;
	/** Il moltiplicatore a cui la debolezza arriva al verde pieno */
	static final double DEBOLEZZA_MASSIMA = 2.0d;

	private static final Color NEUTRO = new Color(0xAD, 0xAD, 0xAD);
	private static final Color VERDE = new Color(0, 255, 0);
	private static final Color ROSSO = new Color(255, 0, 0);

	private TestoReazioneDiClasse() {
	}

	/**
	 * @param moltiplicatore per quanto si moltiplica quel danno contro il personaggio: 0 immune, tra 0 e 1 resiste, più di 1
	 *                       debole
	 */
	public static String di(TipoDanno tipoDanno, double moltiplicatore) {
		String chiave = moltiplicatore == 0.0d ? "REAZIONE_IMMUNE" : moltiplicatore < 1.0d ? "REAZIONE_RESISTE" : "REAZIONE_DEBOLE";
		return TestiDellaUI.get(chiave, tipoDanno.getNome());
	}

	/**
	 * Il colore del testo: tanto più verde quanto più debole è il personaggio (verde pieno da {@link #DEBOLEZZA_MASSIMA} in su),
	 * tanto più rosso quanto più resiste. L'immunità è il rosso pieno, e nessuna resistenza parziale ci arriva (la più forte è
	 * l'ultima sfumatura prima).
	 */
	public static Color colore(double moltiplicatore) {
		if (moltiplicatore == 0.0d) {
			return ROSSO;
		}
		if (moltiplicatore > 1.0d) {
			double quota = Math.min(1.0d, (moltiplicatore - 1.0d) / (DEBOLEZZA_MASSIMA - 1.0d));
			return mescola(NEUTRO, VERDE, (int) Math.ceil(quota * SFUMATURE));
		}
		if (moltiplicatore < 1.0d) {
			double quota = 1.0d - moltiplicatore;
			return mescola(NEUTRO, ROSSO, Math.min(SFUMATURE - 1, (int) Math.ceil(quota * SFUMATURE)));
		}
		return NEUTRO;
	}

	/** La sfumatura <code>passo</code> su {@link #SFUMATURE} fra due colori */
	private static Color mescola(Color da, Color a, int passo) {
		double t = (double) passo / SFUMATURE;
		return new Color(
				(int) Math.round(da.getRed() + (a.getRed() - da.getRed()) * t),
				(int) Math.round(da.getGreen() + (a.getGreen() - da.getGreen()) * t),
				(int) Math.round(da.getBlue() + (a.getBlue() - da.getBlue()) * t));
	}
}
