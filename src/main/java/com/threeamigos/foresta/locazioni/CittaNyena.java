package com.threeamigos.foresta.locazioni;

public class CittaNyena extends Citta {

	private static final CittaNyena istanza = new CittaNyena();
	
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
		return "al 'Leone Rosso'";
	}
}
