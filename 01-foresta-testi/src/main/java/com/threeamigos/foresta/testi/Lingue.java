package com.threeamigos.foresta.testi;

import java.util.Locale;

/**
 * Le regole grammaticali della lingua in uso (vedi {@link Testi#getLingua()}). Una lingua senza regole proprie usa quelle
 * dell'italiano, la lingua in cui il gioco è scritto, come i testi che mancano.
 */
public final class Lingue {

	private static final RegoleDellaLingua ITALIANO = new RegoleConEccezioni(LinguaItaliana.ISTANZA);
	private static final RegoleDellaLingua INGLESE = new RegoleConEccezioni(LinguaInglese.ISTANZA);

	private Lingue() {
	}

	/**
	 * Le regole della lingua scelta con {@link Testi#impostaLingua(String)}.
	 */
	public static RegoleDellaLingua corrente() {
		return per(Testi.getLingua());
	}

	/**
	 * Le regole della lingua con quel codice ("it", "en", "en-GB"): conta solo la lingua, non la regione.
	 */
	public static RegoleDellaLingua per(String codice) {
		String lingua = codice.toLowerCase(Locale.ROOT);
		int separatore = lingua.indexOf('-');
		if (separatore > 0) {
			lingua = lingua.substring(0, separatore);
		}
		return "en".equals(lingua) ? INGLESE : ITALIANO;
	}
}
