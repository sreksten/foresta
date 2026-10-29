package com.threeamigos.foresta.testi;

/**
 * I numeri in lettere: i cardinali fino a dieci e gli ordinali fino a 41, al maschile e al femminile. Sono testi italiani:
 * la localizzazione completa verrà dopo.
 *
 * @author Stefano Reksten
 */
public final class Numeri {

	// I numeri da zero a dieci
	private static final String[] cardinali = {
			"zero", "uno", "due", "tre", "quattro", "cinque", "sei", "sette", "otto", "nove", "dieci"
	};

	// La radice degli ordinali da 1 a 41 (l'indice 0 non ha ordinale)
	private static final String[] ordinali = {
			null, "prim", "second", "terz", "quart", "quint", "sest", "settim", "ottav", "non", "decim",
			"undic", "dodic", "tredic", "quattordic", "quindic", "sedic", "diciassett", "diciott", "diciannov",
			"vent", "ventun", "ventidu", "ventitre", "ventiquattr", "venticinqu", "ventisei", "ventisett", "ventott", "ventinov",
			"trent", "trentun", "trentadu", "trentatre", "trentaquattr", "trentacinqu", "trentasei", "trentasett", "trentott", "trentanov",
			"quarant", "quarantun"
	};

	// Il suffisso degli ordinali oltre il decimo
	private static final String ESIM = "esim";

	/**
	 * Una classe di funzioni statiche: non si istanzia.
	 */
	private Numeri() {
	}

	/**
	 * Il numero in lettere fino a dieci; oltre (o se negativo) resta in cifre: "15".
	 */
	public static String getCardinaleM(int numero) {
		return numero >= 0 && numero < cardinali.length ? cardinali[numero] : String.valueOf(numero);
	}

	/**
	 * Il numero in lettere al femminile: "una" per 1, il resto come {@link #getCardinaleM(int)}.
	 */
	public static String getCardinaleF(int numero) {
		if (numero == 1)
			return "una";
		else
			return getCardinaleM(numero);
	}

	/**
	 * L'ordinale maschile in lettere, senza articolo (vedi {@link #getOrdinaleM(int, boolean)}).
	 */
	public static String getOrdinaleM(int numero) {
		return getOrdinaleM(numero, false);
	}

	/**
	 * L'ordinale maschile in lettere ("primo", "undicesimo", "quarantunesimo"), con l'articolo davanti se richiesto.
	 * Vale da 1 a 41: i numeri da contare nel gioco non vanno oltre (il tempo finisce al giorno 40, i gruppi sono di pochi
	 * personaggi), quindi fuori da questo intervallo non c'è un ripiego: 0 dà "nullo" e da 42 in su lancia
	 * {@code ArrayIndexOutOfBoundsException}. Se un giorno servono numeri più alti si allunga {@code ordinali}.
	 */
	public static String getOrdinaleM(int numero, boolean articolo) {
		return getOrdinaleImpl(numero, articolo, Articoli.IL, "o");
	}

	/**
	 * L'ordinale femminile in lettere, senza articolo (vedi {@link #getOrdinaleF(int, boolean)}).
	 */
	public static String getOrdinaleF(int numero) {
		return getOrdinaleF(numero, false);
	}

	/**
	 * Come {@link #getOrdinaleM(int, boolean)}, al femminile ("prima", "undicesima"): vale da 1 a 41.
	 */
	public static String getOrdinaleF(int numero, boolean articolo) {
		return getOrdinaleImpl(numero, articolo, Articoli.LA, "a");
	}

	// La radice dell'ordinale, l'eventuale "-esim" oltre il decimo e la lettera finale; l'articolo si elide davanti a "otta..." e "undic..."
	private static String getOrdinaleImpl(int numero, boolean aggiungiArticolo, String qualeArticolo, String letteraFinale) {
		StringBuilder sb = new StringBuilder(15);
		if (aggiungiArticolo) {
			if (numero == 8 || numero == 11)
				sb.append(Articoli.L_APOSTROFO);
			else
				sb.append(qualeArticolo);
		}
		sb.append(ordinali[numero]);
		if (numero > 10)
			sb.append(ESIM);
		sb.append(letteraFinale);
		return sb.toString();
	}

}
