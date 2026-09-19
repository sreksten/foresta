package com.threeamigos.foresta.ui;

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
	 * Propone al giocatore una serie di possibili azioni tra le quali
	 * scegliere per poter continuare il gioco.
	 */
    void impostaAzioni();

}
