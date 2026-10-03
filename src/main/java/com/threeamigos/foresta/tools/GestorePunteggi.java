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

	public static boolean isPunteggioInClassifica(int punteggio, String idPartita) {
		return interfacciaGestorePunteggi.isPunteggioInClassifica(punteggio, idPartita);
	}

	public static void addPunteggio(String nome, int punteggio, String idPartita) {
		interfacciaGestorePunteggi.addPunteggio(nome, punteggio, idPartita);
	}

	public static boolean salva() {
		return interfacciaGestorePunteggi.salva();
	}

}
