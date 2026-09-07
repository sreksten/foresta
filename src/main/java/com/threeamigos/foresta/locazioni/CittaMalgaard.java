package com.threeamigos.foresta.locazioni;

public class CittaMalgaard extends Citta {

	private static final CittaMalgaard istanza = new CittaMalgaard();

	private static final String NOME_LOCANDA = "Aquila Nera";
	
	private CittaMalgaard() {
	}
	
	public static CittaMalgaard getIstanza() {
		return istanza;
	}
	
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
