package com.threeamigos.foresta.locazioni;

public class CittaFleena extends Citta {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_FLEENA;
	}

	@Override
	public String getNome() {
		return "la città di Fleena";
	}

}
