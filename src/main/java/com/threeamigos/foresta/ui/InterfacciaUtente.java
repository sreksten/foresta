package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.Personaggio;

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
	 * Va alla schermata di gioco vera e propria
	 */
	void mostraSchermataGioco();

	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
    void centraMappa();

	/** Muove la visuale della mappa di gioco conosciuta su
	 * ricezione di Comando.(NORD|SUD|EST|OVEST)
	 */
    void muoviMappa(Comando direzione);

	/**
	 * Mostra la finestra di scambio artefatti con l'armaiolo
	 */
	void armaiolo();

	/**
	 * Assegna l'automa che gestisce lo scambio di artefatti nella finestra dell'armaiolo
	 */
	void impostaAutomaArmaiolo(AutomaAcquistiArtefatti automaScambiatoreArtefatti);

	/**
	 * Mostra la finestra di scambio consumabili con l'alchimista
	 */
	void alchimista();

	/**
	 * Richiede se si vuole uscire dal gioco
	 */
    void confermaUscita();
	
	/**
	 * Richiama la schermata o animazione di sconfitta
	 */
    void perso();

	/**
	 * Richiama la schermata o animazione di vittoria
	 */
    void vinto();

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
	 * Porta in primo piano una finestra di gioco (UI.FINESTRA_...)
	 */
    void primoPiano(Finestra finestra);

	/**
	 * Porta in secondo piano una finestra di gioco (UI.FINESTRA_...)
	 */
	void secondoPiano(Finestra finestra);

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali
	 * scegliere per poter continuare il gioco.
	 */
    void impostaAzioni();

	/**
	 * Prepara tutto quel che può servire per disegnare la locazione corrente
	 */
    void preparaLocazione();
	
	/**
	 * Mostra la forza in combattimento.
	 */
    void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario);

	void raccogliOggetto();

	/**
	 * Rinfresca l'interfaccia utente (ad esempio dopo aver richiesto un
	 * primo piano di una qualche finestra)
	 */
    void rinfresca();
}
