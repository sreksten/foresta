package com.threeamigos.foresta.locazioni;

public class CittaNyena extends Citta {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_NYENA;
	}

	@Override
	public String getNome() {
		return "la città di Nyena";
	}

}
