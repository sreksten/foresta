package com.threeamigos.foresta.missioni;

/**
 * Un corriere che porta da una città all'altra una merce che non dovrebbe girare: vino senza dazio, erbe proibite,
 * pozioni rubate (vedi CONTRABBANDO in missioni.txt, con gli stessi campi delle spedizioni). Va fatto di nascosto:
 * se per strada il gruppo combatte, la voce si sparge e la missione fallisce (vedi IlCorriere).
 */
public class IlContrabbandiere extends IlCorriere {

	public IlContrabbandiere() {
		super(ClasseMissione.IL_CONTRABBANDIERE, "CONTRABBANDO");
	}

	@Override
	protected boolean isDiNascosto() {
		return true;
	}
}
