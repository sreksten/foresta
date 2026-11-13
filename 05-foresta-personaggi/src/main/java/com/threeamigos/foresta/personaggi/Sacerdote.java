package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il sacerdote giocante, al maschile. Come il Mago, ma con Saggezza e robustezza al posto della sola Intelligenza, e senza dardo arcano: le sue abilità
 * innate sono l'alba sacra e la luce divina (vedi abilita.AlbaSacra e abilita.LuceDivina), che costano Magia e non consumano pergamene. Usa il bastone magico e la mazza.
 */
public class Sacerdote extends PersonaggioBase {

	public Sacerdote(int livello) {
		super(TipoPersonaggio.SACERDOTE, livello);
	}

	public Sacerdote(String nome, int livello) {
		super(nome, TipoPersonaggio.SACERDOTE, livello);
	}

}
