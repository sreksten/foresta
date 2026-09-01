package com.threeamigos.foresta.locazioni;

public class CittaRuuna extends Citta {

	private static final CittaRuuna istanza = new CittaRuuna();
	
	private CittaRuuna() {
	}
	
	public static CittaRuuna getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CITTA_RUUNA;
	}

	@Override
	public String getNome() {
		return "la città di Ruuna";
	}

	@Override
	public String getNomeLocanda() {
		return "al 'Liocorno Rampante'";
	}
}
