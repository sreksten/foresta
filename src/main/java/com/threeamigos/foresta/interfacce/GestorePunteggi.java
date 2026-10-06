package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.strumenti.Punteggio;

/**
 * La classifica dei punteggi: Main crea l'implementazione (GestorePunteggiSuFile) e la passa all'Automa, che vi
 * aggiunge i punteggi a fine partita, e alla UI, che la mostra nell'intro.
 */
public interface GestorePunteggi {

	/**
	 * Recupera i punteggi salvati da qualche parte
	 * @return true se la lettura è andata a buon fine
	 */
	boolean carica();

	/**
	 * @return il numero di punteggi salvati
	 */
	int getConteggio();

	/**
	 * Recupera un punteggio dalla classifica
	 * @param posizione la posizione del punteggio da recuperare
	 * @return il punteggio nella posizione richiesta
	 */
	Punteggio getPunteggio(int posizione);

	/**
	 * Verifica se un punteggio possa entrare in classifica. Se la partita c'è già, solo migliorando il suo
	 * punteggio: chi ricarica un salvataggio e rifinisce la partita non deve riempire la classifica di sé.
	 * @param punteggio il punteggio da controllare
	 * @param idPartita la partita che l'ha fatto
	 * @return true se il punteggio può entrare in classifica
	 */
	boolean isPunteggioInClassifica(int punteggio, String idPartita);

	/**
	 * Aggiunge un punteggio alla classifica: se la partita c'era già, il suo punteggio precedente se ne va (se il
	 * nuovo è migliore), altrimenti se ne va il più basso.
	 * @param nome il nome del giocatore
	 * @param punteggio il punteggio da aggiungere
	 * @param idPartita la partita che l'ha fatto
	 */
	void addPunteggio(String nome, int punteggio, String idPartita);

	/**
	 * Salva i punteggi in qualche modo
	 * @return true se la scrittura è andata a buon fine
	 */
	boolean salva();

}
