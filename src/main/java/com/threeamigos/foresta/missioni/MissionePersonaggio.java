package com.threeamigos.foresta.missioni;

public abstract class MissionePersonaggio extends MissioneBase {

	protected static final String PERSONAGGIO_RICHIEDENTE = "PERSONAGGIO_RICHIEDENTE";
	protected static final String PERSONAGGIO_BERSAGLIO = "PERSONAGGIO_BERSAGLIO";

	public MissionePersonaggio() {
		GeneratoreTestiMissioni gtm = GeneratoreTestiMissioni.getIstanza();
		gtm.reset();
		aggiungiProprieta(PERSONAGGIO_RICHIEDENTE, gtm.produceSingle(PERSONAGGIO_RICHIEDENTE));
		aggiungiProprieta(PERSONAGGIO_BERSAGLIO, gtm.produceSingle(PERSONAGGIO_BERSAGLIO));
	}
}
