package com.threeamigos.foresta.tools;

public class GestorePunteggi {
	
	private GestorePunteggi() {
	}

	private static InterfacciaGestorePunteggi interfacciaGestorePunteggi;

	public static final void impostaGestorePunteggi(InterfacciaGestorePunteggi gestorePunteggi) {
		interfacciaGestorePunteggi = gestorePunteggi;
	}

	public static final boolean carica() {
		return interfacciaGestorePunteggi.carica();
	}

	public static final int getCardinalita() {
		return interfacciaGestorePunteggi.getCardinalita();
	}

	public static final InterfacciaGestorePunteggi.Record getRecord(int posizione) {
		return interfacciaGestorePunteggi.getRecord(posizione);
	}

	public static final boolean isPunteggioInClassifica(int punteggio) {
		return interfacciaGestorePunteggi.isPunteggioInClassifica(punteggio);
	}

	public static final void addRecord(String nome, int punteggio) {
		interfacciaGestorePunteggi.addRecord(nome, punteggio);
	}

	public static final boolean salva() {
		return interfacciaGestorePunteggi.salva();
	}

}
