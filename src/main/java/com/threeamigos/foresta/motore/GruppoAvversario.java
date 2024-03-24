package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

public class GruppoAvversario extends Gruppo {

	private GruppoAvversario() {
		super();
	}
	
	private static GruppoAvversario gruppoAvversario;
	
	public static final GruppoAvversario getIstanza() {
		if (gruppoAvversario == null) {
			gruppoAvversario = new GruppoAvversario();
		}
		return gruppoAvversario;
	}

	public Personaggio getPersonaggioVivo() {
		for (Personaggio personaggio : personaggi) {
			if (personaggio.isVivo()) {
				return personaggio;
			}
		}
		return null;
	}
}
