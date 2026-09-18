package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.ComandiPossibili;
import com.threeamigos.foresta.motore.Comando;

public class UI {

	private UI() {
	}

	private static InterfacciaUtente interfacciaUtente;

	public static void impostaInterfacciaUtente(InterfacciaUtente userInterface) {
		interfacciaUtente = userInterface;
	}

	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
	public static void centraMappa() {
		interfacciaUtente.centraMappa();
	}

	/**
	 * Muove la visuale della mappa di gioco conosciuta su ricezione di
	 * Comando.(NORD|SUD|EST|OVEST)
	 */
	public static void muoviMappa(Comando direzione) {
		interfacciaUtente.muoviMappa(direzione);
	}

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali scegliere per
	 * poter continuare il gioco. L'elenco delle possibili azioni viene riempito con valori
	 * provenienti dalla classe Comando
	 */
	public static void impostaAzioni() {
		interfacciaUtente.impostaAzioni();
	}

	/**
	 * Scorciatoia per impostare i comandi e modificare subito l'interfaccia utente
	 */
	public static void impostaAzioni(Comando... comandi) {
		ComandiPossibili.set(comandi);
		impostaAzioni();
	}

}
