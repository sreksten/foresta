package com.threeamigos.foresta.locazioni;

public class CittaFleena extends Citta {

	private static final CittaFleena istanza = new CittaFleena();

	private static final String NOME_LOCANDA = "Manticora";
	
	private CittaFleena() {
	}
	
	public static CittaFleena getIstanza() {
		return istanza;
	}
	
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
