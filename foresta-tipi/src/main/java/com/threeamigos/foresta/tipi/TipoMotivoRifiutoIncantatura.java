package com.threeamigos.foresta.tipi;

/**
 * Perché l'incantatore non accetta qualcosa sul banco o non fa la fusione (vedi RegoleIncantatura).
 * <p>
 * I testi di ogni costante (FRASE) non stanno qui ma nel file {@code tipo_motivo_rifiuto_incantatura_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoMotivoRifiutoIncantatura {

	NESSUN_ARTEFATTO,
	PIU_ARTEFATTI,
	NESSUNA_PERGAMENA,
	NON_INCANTABILE,
	NESSUN_MIGLIORAMENTO,
	POTERE_MAGICO_FUORI_POSTO,
	INCANTAMENTO_SU_LIBRO,
	LIVELLO_DEL_MONDO_RAGGIUNTO,
	MONETE_INSUFFICIENTI;


	TipoMotivoRifiutoIncantatura() {
	}

	public String getFrase() {
		return Testi.testoOCostante(this, "FRASE");
	}
}
