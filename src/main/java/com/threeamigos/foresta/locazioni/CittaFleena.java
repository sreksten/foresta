package com.threeamigos.foresta.locazioni;

public class CittaFleena extends Citta {

	private static CittaFleena istanza = new CittaFleena();
	
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
		return "la citta' di Fleena";
	}

	@Override
	public String getNomeLocanda() {
		return "alla 'Manticora'";
	}
}
