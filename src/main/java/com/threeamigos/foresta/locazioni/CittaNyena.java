package com.threeamigos.foresta.locazioni;

public class CittaNyena extends Citta {

	private static final CittaNyena istanza = new CittaNyena();

	private static final String NOME_LOCANDA = "Leone Rosso";
	
	private CittaNyena() {
	}
	
	public static CittaNyena getIstanza() {
		return istanza;
	}
	
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
