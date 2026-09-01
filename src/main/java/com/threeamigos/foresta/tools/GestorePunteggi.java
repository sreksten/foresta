package com.threeamigos.foresta.tools;

public class GestorePunteggi {
	
	private GestorePunteggi() {
	}

	private static InterfacciaGestorePunteggi interfacciaGestorePunteggi;

	public static void impostaGestorePunteggi(InterfacciaGestorePunteggi gestorePunteggi) {
		interfacciaGestorePunteggi = gestorePunteggi;
	}

	public static int getCardinalita() {
		return interfacciaGestorePunteggi.getConteggio();
	}

	public static Punteggio getPunteggio(int posizione) {
		return interfacciaGestorePunteggi.getPunteggio(posizione);
	}

	public static boolean isPunteggioInClassifica(int punteggio) {
		return interfacciaGestorePunteggi.isPunteggioInClassifica(punteggio);
	}

	public static void addPunteggio(String nome, int punteggio) {
		interfacciaGestorePunteggi.addPunteggio(nome, punteggio);
	}

	public static boolean salva() {
		return interfacciaGestorePunteggi.salva();
	}

}
