package com.threeamigos.foresta.locazioni;

public class CittaRuuna extends Citta {

	private static final CittaRuuna istanza = new CittaRuuna();

	private static final String NOME_LOCANDA = "Liocorno Rampante";
	
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
		return "al '" + NOME_LOCANDA + "'";
	}

	@Override
	public String getNomeSempliceLocanda() {
		return NOME_LOCANDA;
	}
}
