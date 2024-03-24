package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.Personaggio;

public interface InterfacciaUtente {

	public enum Finestra {
		GRAFICA,
		STATO,
		INCANTESIMI,
		MAPPA,
		STATISTICHE,
		TESTO,
		MISSIONI,
		INFO_COMBATTIMENTO;
	}
	
	/**
	 * Ripulisce eventuali dati da partite precedenti
	 */
	public void reinizializza();
	
	/**
	 * Richiama la schermata o animazione di introduzione
	 */
	public void intro();

	/**
	 * Richiama la schermata di selezione nuovo gioco o caricamento di un salvataggio
	 */
	public void nuovoGiocoOCaricaPrecedente();
	
	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public void selezioneSlotSalvataggioDaCaricare();
	
	/**
	 * Mostra la mappa di gioco conosciuta
	 */
	public void mappa();
	
	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
	public void centraMappa();

	/** Muove la visuale della mappa di gioco conosciuta su
	 * ricezione di Comando.(NORD|SUD|EST|OVEST)
	 */
	public void muoviMappa(Comando direzione);

	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public void selezioneSlotSalvataggioDaSalvare();
	
	/**
	 * Richiede se si vuole uscire dal gioco
	 */
	public void confermaUscita();
	
	/**
	 * Richiama la schermata o animazione di sconfitta
	 */
	public void perso();

	/**
	 * Richiama la schermata o animazione di vittoria
	 */
	public void vinto();

	/**
	 * Richiama la schermata o animazione che mostra le statistiche
	 * sui mostri uccisi eccetera.
	 */
	public void statistiche();

	/**
	 * Richiama la schermata o animazione che riporta la tabella dei punteggi
	 */
	public void hiscore();

	/**
	 * Mostra un messaggio di comunicazione prima/dopo il gioco
	 * come ad esempio la richiesta di inserire il nome del giocatore
	 */
	public void scriviGrande(String messaggio);

	/**
	 * Mostra un messaggio di gioco
	 */
	public void notifica(String messaggio);

	/**
	 * Porta in primo piano una finestra di gioco (UI.FINESTRA_...)
	 */
	public void primoPiano(Finestra finestra);

	/**
	 * Mostra la richiesta di un testo da parte di un controllore di gioco
	 */
	public void chiediTesto();

	/**
	 * Inoltra un testo ricevuto ad un controllore di gioco
	 */
	public void riceviTesto(String s);

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali
	 * scegliere per poter continuare il gioco.
	 */
	public void impostaAzioni();

	/**
	 * Prepara tutto quel che può servire per disegnare la locazione corrente
	 */
	public void preparaLocazione();
	
	/**
	 * Mostra la forza in combattimento.
	 */
	public void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario);

	public void notificaMorte(Personaggio personaggio);
	
	public void variaForza(Personaggio personaggio, int variazione);
	
	public void variaForzaMassima(Personaggio personaggio, int variazione);
	
	public void variaMagia(Personaggio personaggio, int variazione);
	
	public void variaMagiaMassima(Personaggio personaggio, int variazione);
	
	public void variaCoraggio(Personaggio personaggio, int variazione);
	
	public void variaValore(Personaggio personaggio, int variazione);
	
	public void variaCarisma(Personaggio personaggio, int variazione);
	
	public void variaStanchezza(Personaggio personaggio, int variazione);	

	public void variaTempo(Personaggio personaggio, int variazione);
	
	public void variaGemme(int variazione);

	public void variaMonete(int variazione);

	public void variaPunti(int variazione);

	public void variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione);

	public void variaPozioniForza(int variazione);

	public void variaPozioniMagia(int variazione);

	public void variaPozioniGrandeForza(int variazione);

	public void variaMappa();

	public void raccogliOggetto();

	/**
	 * Rinfresca l'interfaccia utente (ad esempio dopo aver richiesto un
	 * primo piano di una qualche finestra)
	 */
	public void rinfresca();
}
