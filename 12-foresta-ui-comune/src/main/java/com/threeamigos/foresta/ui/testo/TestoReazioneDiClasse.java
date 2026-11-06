package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * Il testo che sale sopra un personaggio quando, per la sua classe, è debole, resiste o è immune a un tipo di danno: dice al
 * giocatore anche a quale ("Debole a Sacro", "Resiste a Fuoco", "Immune a Aria"), e il colore dice quanto: dal grigio verso il
 * verde man mano che è più debole (fino al doppio del danno), dal grigio verso il rosso man mano che resiste di più, rosso
 * acceso se è immune. Il colore ha {@link #SFUMATURE} gradi per parte.
 * <p>
 * I colori sono interi ARGB (opachi), senza il toolkit grafico: Swing e libGDX li convertono nel loro tipo.
 */
public final class TestoReazioneDiClasse {

	/** Quante sfumature ha il passaggio dal grigio al verde (e dal grigio al rosso) */
	static final int SFUMATURE = 10;
	/** Il moltiplicatore a cui la debolezza arriva al verde pieno */
	static final double DEBOLEZZA_MASSIMA = 2.0d;

	private static final int NEUTRO = argb(0xAD, 0xAD, 0xAD);
	private static final int VERDE = argb(0, 255, 0);
	private static final int ROSSO = argb(255, 0, 0);

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
	 * Il colore del testo, in ARGB: tanto più verde quanto più debole è il personaggio (verde pieno da {@link #DEBOLEZZA_MASSIMA}
	 * in su), tanto più rosso quanto più resiste. L'immunità è il rosso pieno, e nessuna resistenza parziale ci arriva (la più
	 * forte è l'ultima sfumatura prima).
	 */
	public static int colore(double moltiplicatore) {
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

	/** Il rosso di un colore ARGB, da 0 a 255 */
	public static int rosso(int argb) {
		return (argb >> 16) & 0xFF;
	}

	/** Il verde di un colore ARGB, da 0 a 255 */
	public static int verde(int argb) {
		return (argb >> 8) & 0xFF;
	}

	/** Il blu di un colore ARGB, da 0 a 255 */
	public static int blu(int argb) {
		return argb & 0xFF;
	}

	private static int argb(int rosso, int verde, int blu) {
		return 0xFF000000 | (rosso << 16) | (verde << 8) | blu;
	}

	/** La sfumatura <code>passo</code> su {@link #SFUMATURE} fra due colori */
	private static int mescola(int da, int a, int passo) {
		double t = (double) passo / SFUMATURE;
		return argb(
				(int) Math.round(rosso(da) + (rosso(a) - rosso(da)) * t),
				(int) Math.round(verde(da) + (verde(a) - verde(da)) * t),
				(int) Math.round(blu(da) + (blu(a) - blu(da)) * t));
	}
}
