package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.testi.GrammarBean.InvalidGrammarException;

import java.io.IOException;

/**
 * Carica le grammatiche del gioco: i file {@code .txt} delle risorse. Quelle dei testi stanno nella cartella {@code testi} di
 * questo modulo; chi ha grammatiche sue (per esempio gli artefatti) indica la risorsa intera. Una grammatica che manca o non è valida
 * solleva una {@link GrammaticaNonValidaException}: decidere cosa farne spetta a chi gestisce il gioco.
 */
public final class CaricatoreDiGrammatiche {

	/**
	 * La cartella delle risorse con le grammatiche dei testi.
	 */
	public static final String CARTELLA_TESTI = "/com/threeamigos/foresta/testi/";
	// Le sostituzioni finali che sistemano le preposizioni articolate ("a il" diventa "al")
	private static final String PREPOSIZIONI_ARTICOLATE = CARTELLA_TESTI + "preposizioni_articolate_pp.txt";

	private CaricatoreDiGrammatiche() {
	}

	/**
	 * La grammatica di una risorsa, con le sue inclusioni.
	 *
	 * @param ancora         una classe per trovare le risorse, come per {@link Class#getResourceAsStream}
	 * @param risorsa        la risorsa con la grammatica, per esempio {@code /com/mio/pacchetto/leggendari.txt}
	 * @param postProduzione la risorsa con le sostituzioni finali, o null
	 * @throws GrammaticaNonValidaException se una risorsa manca o la grammatica non è valida
	 */
	public static GrammarBean carica(Class<?> ancora, String risorsa, String postProduzione) {
		try {
			return GrammarBean.fromResource(ancora, risorsa, postProduzione);
		} catch (InvalidGrammarException | IOException e) {
			throw new GrammaticaNonValidaException(risorsa, e);
		}
	}

	/**
	 * Una grammatica dei testi, dalla cartella {@link #CARTELLA_TESTI}.
	 *
	 * @param nomeFile                  il file, per esempio {@code fiabe.txt}
	 * @param conPreposizioniArticolate se le preposizioni articolate del testo prodotto vanno sistemate
	 * @throws GrammaticaNonValidaException se manca o non è valida
	 */
	public static GrammarBean caricaDeiTesti(String nomeFile, boolean conPreposizioniArticolate) {
		return carica(CaricatoreDiGrammatiche.class, CARTELLA_TESTI + nomeFile,
				conPreposizioniArticolate ? PREPOSIZIONI_ARTICOLATE : null);
	}
}
