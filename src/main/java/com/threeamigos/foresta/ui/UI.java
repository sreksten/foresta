package com.threeamigos.foresta.ui;

public class UI {

	private UI() {
	}

	private static InterfacciaUtente interfacciaUtente;

	public static void impostaInterfacciaUtente(InterfacciaUtente userInterface) {
		interfacciaUtente = userInterface;
	}

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali scegliere per
	 * poter continuare il gioco. L'elenco delle possibili azioni viene riempito con valori
	 * provenienti dalla classe Comando
	 */
	public static void impostaAzioni() {
		interfacciaUtente.impostaAzioni();
	}

}
