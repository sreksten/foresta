package com.threeamigos.foresta.locazioni;

public class CittaMalgaard extends Citta {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_MALGAARD;
	}

	@Override
	public String getNome() {
		return "la città di Malgaard";
	}

}
