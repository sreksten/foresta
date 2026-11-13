package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il necromante: avversario umano oscuro, che risveglia i morti e li manda avanti a sé (vedi le missioni dei morti che camminano). Umanoide,
 * non si corrompe e non diventa amico. Combatte con l'energia necrotica (la dissipazione dell'anima, vedi ArmaNaturale) e conosce le magie dei maghi
 * nemici; subisce meno danno necrotico e più danno sacro (vedi TipoPersonaggio).
 */
public class Necromante extends PersonaggioBase {

	public Necromante(int livello) {
		super(TipoPersonaggio.NECROMANTE, livello);
	}

	public Necromante(String nome, int livello) {
		super(nome, TipoPersonaggio.NECROMANTE, livello);
	}

}
