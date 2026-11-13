package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il Bardo giocante, al maschile: umanoide, corrompibile e disposto all'amicizia. Conosce i canti (vedi il pacchetto
 * {@code canti}). Il femminile è la {@link Cantastorie}, che ha lo stesso ruolo.
 */
public class Bardo extends PersonaggioBase {

	public Bardo(int livello) {
		super(TipoPersonaggio.BARDO, livello);
	}

	public Bardo(String nome, int livello) {
		super(nome, TipoPersonaggio.BARDO, livello);
	}

	/**
	 * Per chi ha le caratteristiche del bardo sotto un'altra classe (vedi BardoLocanda).
	 */
	protected Bardo(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}
	

}
