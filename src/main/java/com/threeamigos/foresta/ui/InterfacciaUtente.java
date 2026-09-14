package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.AutomaInventario;
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
	 * Ripulisce eventuali dati da partite precedenti
	 */
    void reinizializza();
	
	/**
	 * Richiama la schermata di selezione nuovo gioco o caricamento di un salvataggio
	 */
    void nuovoGiocoOCaricaPrecedente();
	
	/**
	 * Richiama la schermata di selezione salvataggio
	 */
    void selezioneSlotSalvataggioDaCaricare();

	/**
	 * Va alla schermata di gioco vera e propria
	 */
	void mostraSchermataGioco();

	/**
	 * Mostra la mappa di gioco conosciuta
	 */
    void mappa();
	
	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
    void centraMappa();

	/** Muove la visuale della mappa di gioco conosciuta su
	 * ricezione di Comando.(NORD|SUD|EST|OVEST)
	 */
    void muoviMappa(Comando direzione);

	/**
	 * Mostra l'inventario
	 */
	void inventario();

	/**
	 * Assegna l'automa che gestisce lo scambio di artefatti nella finestra di inventario
	 */
	void impostaAutomaInventario(AutomaInventario automaScambiatoreArtefatti);

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
	 * Richiama la schermata di selezione salvataggio
	 */
    void selezioneSlotSalvataggioDaSalvare();
	
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
	 * Mostra un messaggio di comunicazione prima/dopo il gioco
	 * come ad esempio la richiesta di inserire il nome del giocatore
	 */
    void scriviGrande(String messaggio);

	/**
	 * Porta in primo piano una finestra di gioco (UI.FINESTRA_...)
	 */
    void primoPiano(Finestra finestra);

	/**
	 * Porta in secondo piano una finestra di gioco (UI.FINESTRA_...)
	 */
	void secondoPiano(Finestra finestra);

	/**
	 * Mostra la richiesta di un testo da parte di un controllore di gioco
	 */
    void chiediTesto();

	/**
	 * Inoltra un testo ricevuto a un controllore di gioco
	 */
    void riceviTesto(String s);

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

	void variaGemme(int variazione);

	void variaMonete(int variazione);

	void variaPunti(int variazione);

	void variaIncantesimi(ClasseIncantesimo classeIncantesimo, int variazione);

	void variaPozioniSalute(int variazione);

	void variaPozioniMagia(int variazione);

	void variaPozioniMagiaGrande(int variazione);

	void variaPozioniSaluteGrande(int variazione);

	void variaMappa();

	void raccogliOggetto();

	/**
	 * Rinfresca l'interfaccia utente (ad esempio dopo aver richiesto un
	 * primo piano di una qualche finestra)
	 */
    void rinfresca();
}
