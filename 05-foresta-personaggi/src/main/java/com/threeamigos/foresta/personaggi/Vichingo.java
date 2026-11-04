package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il vichingo: ha le caratteristiche del guerriero (e il suo berserk, ma più precoce e più forte: vedi {@link Berserk}), con immagini e icona
 * proprie. Solo al maschile. Non è una classe giocante e non si recluta.
 */
public class Vichingo extends Guerriero {

	public Vichingo(int livello) {
		this(null, livello);
	}

	public Vichingo(String nome, int livello) {
		super(nome, TipoPersonaggio.VICHINGO, livello);
	}
}
