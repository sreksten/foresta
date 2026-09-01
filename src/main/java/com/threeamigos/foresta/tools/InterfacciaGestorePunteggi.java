package com.threeamigos.foresta.tools;

public interface InterfacciaGestorePunteggi {

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
	 * Verifica se un punteggio possa entrare in classifica
	 * @param punteggio il punteggio da controllare
	 * @return true se il punteggio può entrare in classifica
	 */
	boolean isPunteggioInClassifica(int punteggio);

	/**
	 * Aggiunge un punteggio alla classifica eliminando il più basso
	 * @param nome il nome del giocatore
	 * @param punteggio il punteggio da aggiungere
	 */
	void addPunteggio(String nome, int punteggio);

	/**
	 * Salva i punteggi in qualche modo
	 * @return true se la scrittura è andata a buon fine
	 */
	boolean salva();

}
