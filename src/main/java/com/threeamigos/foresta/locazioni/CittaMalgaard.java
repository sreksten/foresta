package com.threeamigos.foresta.locazioni;

public class CittaMalgaard extends Citta {


	private static final String NOME_LOCANDA = "Aquila Nera";
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_MALGAARD;
	}

	@Override
	public String getNome() {
		return "la città di Malgaard";
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
