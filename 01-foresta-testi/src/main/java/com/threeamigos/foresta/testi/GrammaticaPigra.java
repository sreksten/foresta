package com.threeamigos.foresta.testi;

import java.util.function.Supplier;

/**
 * Una grammatica che si carica la prima volta che serve (e non all'avvio della classe che la usa), così un errore di caricamento
 * è un'eccezione normale, {@link GrammaticaNonValidaException}, che chi precarica può prendere. Se il caricamento fallisce si
 * riprova alla richiesta successiva.
 */
public final class GrammaticaPigra {

	private final Supplier<GestoreGrammatiche> caricamento;
	// Una per lingua: se la lingua cambia se ne carica un'altra, e al ritorno si ritrova la prima. È una mappa "concurrent"
	// perché get() la legge senza lucchetto, mentre carica() la scrive con il lucchetto
	private final java.util.concurrent.ConcurrentMap<String, GestoreGrammatiche> perLingua = new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * @param caricamento come si carica la grammatica; può sollevare una {@link GrammaticaNonValidaException}
	 */
	public GrammaticaPigra(Supplier<GestoreGrammatiche> caricamento) {
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
	public GestoreGrammatiche get() {
		GestoreGrammatiche corrente = perLingua.get(Testi.getLingua());
		return corrente != null ? corrente : carica();
	}

	/**
	 * La prima riga di testo prodotta da quella produzione, senza spazi ai bordi: è quel che chiede quasi sempre chi usa
	 * una grammatica per un nome o una frase.
	 */
	public String riga(String produzione) {
		return get().produci(produzione).get(0).trim();
	}

	// Se due thread arrivano insieme, il secondo trova già la grammatica del primo
	synchronized GestoreGrammatiche carica() {
		String lingua = Testi.getLingua();
		GestoreGrammatiche grammatica = perLingua.get(lingua);
		if (grammatica == null) {
			grammatica = caricamento.get();
			perLingua.put(lingua, grammatica);
		}
		return grammatica;
	}
}
