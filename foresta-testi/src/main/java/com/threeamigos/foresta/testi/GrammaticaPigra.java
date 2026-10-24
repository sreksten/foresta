package com.threeamigos.foresta.testi;

import java.util.function.Supplier;

/**
 * Una grammatica che si carica la prima volta che serve (e non all'avvio della classe che la usa), così un errore di caricamento
 * è un'eccezione normale, {@link GrammaticaNonValidaException}, che chi precarica può prendere. Se il caricamento fallisce si
 * riprova alla richiesta successiva.
 */
public final class GrammaticaPigra {

	private final Supplier<GrammarBean> caricamento;
	private volatile GrammarBean grammatica;

	/**
	 * @param caricamento come si carica la grammatica; può sollevare una {@link GrammaticaNonValidaException}
	 */
	public GrammaticaPigra(Supplier<GrammarBean> caricamento) {
		this.caricamento = caricamento;
	}

	/**
	 * Una grammatica dei testi (vedi {@link CaricatoreDiGrammatiche#caricaDeiTesti}).
	 */
	public static GrammaticaPigra deiTesti(String nomeFile, boolean conPreposizioniArticolate) {
		return new GrammaticaPigra(() -> CaricatoreDiGrammatiche.caricaDeiTesti(nomeFile, conPreposizioniArticolate));
	}

	/**
	 * La grammatica, caricata se non lo è già.
	 *
	 * @throws GrammaticaNonValidaException se non si può caricare
	 */
	public GrammarBean get() {
		GrammarBean corrente = grammatica;
		return corrente != null ? corrente : carica();
	}

	// Se due thread arrivano insieme, il secondo trova già la grammatica del primo
	synchronized GrammarBean carica() {
		if (grammatica == null) {
			grammatica = caricamento.get();
		}
		return grammatica;
	}
}
