package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * Perché l'incantatore non accetta qualcosa sul banco o non fa la fusione (vedi RegoleIncantatura).
 * <p>
 * I testi di ogni costante (FRASE) non stanno qui ma nel file {@code tipo_motivo_rifiuto_incantatura_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoMotivoRifiutoIncantatura {

	/**
	 * Sul banco ci sono solo ingredienti o aggiornamenti, e manca l'artefatto da incantare
	 */
	NESSUN_ARTEFATTO,
	/**
	 * Si prova a mettere sul banco un secondo artefatto (o ce ne sono già due). Se ne incanta uno alla volta
	 */
	PIU_ARTEFATTI,
	/**
	 * C'è l'artefatto, ma né un ingrediente magico da fondere (pergamena, gemma, monile, gingillo, sigillo) né un aggiornamento
	 */
	NESSUN_INGREDIENTE,
	/**
	 * L'oggetto non si può incantare: solo armi, scudi, elmi, maschere, armature, schinieri e libri magici
	 */
	NON_INCANTABILE,
	/**
	 * Gli effetti degli ingredienti non sono migliori di quelli che l'artefatto ha già,
	 * e non ci sono aggiornamenti: la fusione non cambierebbe niente
	 */
	NESSUN_MIGLIORAMENTO,
	/**
	 * Con il libro che riprogramma sul banco, l'artefatto non ha incantamenti da spezzare: la riprogrammazione non cambierebbe niente
	 */
	NIENTE_DA_RIPROGRAMMARE,
	/**
	 * Un ingrediente non avrebbe nessun effetto sull'artefatto: ha solo potere magico,
	 * che si fonde solo su bastoni e libri magici
	 */
	POTERE_MAGICO_FUORI_POSTO,
	/**
	 * Un ingrediente non avrebbe nessun effetto sul libro magico:
	 * ha solo incantamenti elementali, che su un libro non contano
	 */
	INCANTAMENTO_SU_LIBRO,
	/**
	 * Con gli aggiornamenti sul banco l'artefatto salirebbe oltre il livello del mondo
	 */
	LIVELLO_DEL_MONDO_RAGGIUNTO,
	/**
	 * Il gruppo non ha abbastanza monete per pagare la fusione (il costo è già scontato dalla contrattazione)
	 */
	MONETE_INSUFFICIENTI;

	TipoMotivoRifiutoIncantatura() {
	}

	/**
	 * La frase che spiega il rifiuto, nella lingua del gioco.
	 */
	public String getFrase() {
		return Testi.testoOCostante(this, "FRASE");
	}
}
