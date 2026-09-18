package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Comando;

public interface InterfacciaUtente {

	enum Finestra {
		INTRO_OUTRO,
		GRAFICA,
		STATO,
		INCANTESIMI,
		MAPPA,
		STATISTICHE,
		TESTO,
		MISSIONI,
		INFO_COMBATTIMENTO,
		MAPPA_A_TUTTO_SCHERMO,
		INVENTARIO
    }

	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
    void centraMappa();

	/** Muove la visuale della mappa di gioco conosciuta su
	 * ricezione di Comando.(NORD|SUD|EST|OVEST)
	 */
    void muoviMappa(Comando direzione);

	/**
	 * Richiede se si vuole uscire dal gioco
	 */
    void confermaUscita();

	/**
	 * Richiama la schermata o animazione che mostra le statistiche
	 * sui mostri uccisi eccetera.
	 */
    void statistiche();

	/**
	 * Richiama la schermata o animazione che riporta la tabella dei punteggi
	 */
    void punteggi();

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali
	 * scegliere per poter continuare il gioco.
	 */
    void impostaAzioni();

}
