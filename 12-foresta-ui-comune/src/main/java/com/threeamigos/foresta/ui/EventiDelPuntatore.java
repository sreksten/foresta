package com.threeamigos.foresta.ui;

/**
 * Gli eventi del puntatore (mouse o dito) già in coordinate dello schermo di gioco, quelli che il {@link RouterDegliInput} sa instradare alle finestre.
 * Chi riconosce i gesti ({@link RiconoscitoreDiGesti}) li produce, il router li consuma; l'interfaccia permette di provare il riconoscitore senza finestre.
 */
public interface EventiDelPuntatore {

	/** Il puntatore è fermo o si muove senza premere (col mouse; il dito non ha questo stato) */
	void mosso(int x, int y);

	/** Il puntatore esce dallo schermo: nessuna finestra è più sotto di lui */
	void mouseUscito(int x, int y);

	void premuto(int x, int y, Finestra.Tasto tasto);

	void trascinato(int x, int y);

	void rilasciato(int x, int y, Finestra.Tasto tasto);

	/**
	 * Un click (premuto e rilasciato nello stesso punto)
	 *
	 * @param conteggio 1 per il primo click, 2 per il secondo di un doppio click
	 */
	void clic(int x, int y, Finestra.Tasto tasto, int conteggio);

	/** La rotella: positiva verso il basso (verso chi guarda), negativa verso l'alto */
	void rotella(int x, int y, int rotazioni);

	/**
	 * Quanto tempo passa fra un click e il secondo perché formino un doppio click (il click singolo si rimanda di tanto, per poterlo scartare)
	 */
	int getIntervalloDoppioClick();
}
