package com.threeamigos.foresta.missioni;

public abstract class MissioneRecuperaBersaglio extends MissioneBase {

	private static final String BERSAGLIO_RECUPERATO = "BERSAGLIO_RECUPERATO";
	
	@Override
	public final boolean isPrimaria() {
		return false;
	}
	
	protected final boolean isBersaglioRecuperato() {
		return md.ottieniProprieta(BERSAGLIO_RECUPERATO) != null;
	}
	
	protected final void setBersaglioRecuperato() {
		md.aggiungiProprieta(BERSAGLIO_RECUPERATO, AFFERMATIVO);
	}
}
