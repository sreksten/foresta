package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

public class GruppoAvversario extends Gruppo {

	private GruppoAvversario() {
		super();
	}
	
	private static GruppoAvversario gruppoAvversario;
	
	public static GruppoAvversario getIstanza() {
		if (gruppoAvversario == null) {
			gruppoAvversario = new GruppoAvversario();
		}
		return gruppoAvversario;
	}

	@Override
	public boolean isGruppoGiocatore() {
		return false;
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
