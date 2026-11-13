package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La sacerdotessa, il Sacerdote al femminile. Come la Maga, ma con Saggezza e robustezza al posto della sola Intelligenza, e senza dardo arcano: le sue abilità
 * innate sono l'alba sacra e la luce divina (vedi abilita.AlbaSacra e abilita.LuceDivina), che costano Magia e non consumano pergamene. Usa il bastone magico e la mazza.
 */
public class Sacerdotessa extends PersonaggioBase {

	public Sacerdotessa(int livello) {
		super(TipoPersonaggio.SACERDOTESSA, livello);
	}

	public Sacerdotessa(String nome, int livello) {
		super(nome, TipoPersonaggio.SACERDOTESSA, livello);
	}

}
