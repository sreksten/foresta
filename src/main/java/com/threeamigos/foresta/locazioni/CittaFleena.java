package com.threeamigos.foresta.locazioni;

public class CittaFleena extends Citta {


	private static final String NOME_LOCANDA = "Manticora";
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_FLEENA;
	}

	@Override
	public String getNome() {
		return "la città di Fleena";
	}

	@Override
	public String getNomeLocanda() {
		return "alla '" + NOME_LOCANDA + "'";
	}

	@Override
	public String getNomeSempliceLocanda() {
		return NOME_LOCANDA;
	}
}
