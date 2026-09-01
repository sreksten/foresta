package com.threeamigos.foresta.locazioni;

public class CittaMalgaard extends Citta {

	private static final CittaMalgaard istanza = new CittaMalgaard();
	
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
		return "alla 'Aquila Nera'";
	}
}
