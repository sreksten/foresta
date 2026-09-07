package com.threeamigos.foresta.locazioni;

public class CittaNyena extends Citta {


	private static final String NOME_LOCANDA = "Leone Rosso";
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_NYENA;
	}

	@Override
	public String getNome() {
		return "la città di Nyena";
	}

	@Override
	public String getNomeLocanda() {
		return "al '" + NOME_LOCANDA + "'";
	}

	@Override
	public String getNomeSempliceLocanda() {
		return NOME_LOCANDA;
	}
}
