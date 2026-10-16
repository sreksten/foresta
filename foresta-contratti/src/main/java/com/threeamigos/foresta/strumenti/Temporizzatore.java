package com.threeamigos.foresta.strumenti;

/**
 * Il temporizzatore serve per far ricevere ad un oggetto un impulso
 * ogni tot tempo, per i combattimenti, la intro, la fine, le animazioni, eccetera.
 */
public interface Temporizzatore {

	// Le animazioni avanzano in base al tempo reale, non per fotogramma: alzarlo le rende
	// più fluide senza cambiarne la velocità. A 30 lo scorrimento del notiziario scattava
	// visibilmente, perché ogni fotogramma lo spostava di diversi pixel.
	int FRAME_PER_SECONDO = 60;

	void setTemporizzabile(Temporizzabile temporizzabile);

	/**
	 * Avvia gli impulsi: il primo subito, poi uno ogni millisecondi. Adatto a ciò che
	 * deve partire immediatamente, come il primo round di un combattimento.
	 */
	void inizia(int millisecondi);

	/**
	 * Avvia gli impulsi: il primo dopo millisecondi, poi uno ogni millisecondi. Adatto
	 * alle sequenze di schermate, dove la prima deve restare visibile per un periodo intero.
	 */
	void iniziaDopo(int millisecondi);

	void termina();

	/**
	 * Ferma gli impulsi senza dimenticare il ritmo: serve quando l'app passa in background, dove un impulso non deve
	 * arrivare (un combattimento non avanza, un'animazione non scorre). Gli impulsi già accodati non arrivano. Si
	 * riprende con {@link #riprendi()}; un {@link #inizia(int)} o {@link #iniziaDopo(int)} chiamato intanto vale, ma
	 * parte solo alla ripresa.
	 */
	void sospendi();

	/**
	 * Fa ripartire gli impulsi fermati da {@link #sospendi()}, con lo stesso ritmo: il primo dopo un periodo intero.
	 * Non fa niente se non c'era nulla da riprendere.
	 */
	void riprendi();
}