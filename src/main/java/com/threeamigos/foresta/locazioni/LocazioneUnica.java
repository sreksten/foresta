package com.threeamigos.foresta.locazioni;

public abstract class LocazioneUnica extends LocazioneBase {

	/**
	 * Le locazioni uniche sono le citta' ed i castelli. Essendo uniche hanno
	 * una loro ubicazione all'interno della foresta e a volte chiacchierando
	 * un gruppo viene a sapere la direzione della locazione rispetto alla loro.
	 */
	public abstract String getNome();

}
