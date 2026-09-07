package com.threeamigos.foresta.locazioni;

public class CittaRuuna extends Citta {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_RUUNA;
	}

	@Override
	public String getNome() {
		return "la città di Ruuna";
	}

}
