package com.threeamigos.foresta.motore.tipi;

/**
 * Perché l'incantatore non accetta qualcosa sul banco o non fa la fusione (vedi RegoleIncantatura).
 */
public enum TipoMotivoRifiutoIncantatura {

	NESSUN_ARTEFATTO("Sul banco manca l'artefatto da incantare."),
	PIU_ARTEFATTI("Sul banco ci va un artefatto alla volta."),
	NESSUNA_PERGAMENA("Sul banco manca un ingrediente magico da fondere."),
	NON_INCANTABILE("Questo non si può incantare: solo armi, scudi, elmi, armature, schinieri e libri magici."),
	LIMITE_SUPERATO("L'artefatto non ha più posto per altri effetti."),
	POTERE_MAGICO_FUORI_POSTO("Il potere magico si fonde solo su bastoni e libri magici, e questo ingrediente non ha altro."),
	INCANTAMENTO_SU_LIBRO("Su un libro magico gli incantamenti elementali non hanno effetto, e questo ingrediente non ha altro."),
	MONETE_INSUFFICIENTI("Non avete abbastanza monete per la fusione.");

	private final String frase;

	TipoMotivoRifiutoIncantatura(String frase) {
		this.frase = frase;
	}

	public String getFrase() {
		return frase;
	}
}
