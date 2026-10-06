package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * Questa classe contiene la codifica numero-stringa per ordinali e cardinali,
 * articoli, alcuni pezzi della storia (introduzione e finali) nonche' una
 * funzione che indica la direzione e la distanza tra due punti all'interno
 * della foresta.
 */

public class Misc {
	
	private Misc() {}

	public static final String IL = "il ";
	public static final String LO = "lo ";
	public static final String LA = "la ";
	public static final String L_APOSTROFO = "l'";
	public static final String I = "i ";
	public static final String GLI = "gli ";
	public static final String LE = "le ";
	public static final String UN = "un ";
	public static final String UN_PAIO_DI = "un paio di ";
	public static final String UNO = "uno ";
	public static final String UNA = "una ";
	public static final String UN_APOSTROFO = "un'";
	public static final String ALCUNI = "alcuni ";
	public static final String ALCUNE = "alcune ";
	public static final String ALCUNE_PAIA_DI = "alcune paia di ";
	public static final String DEL = "del ";
	public static final String DELLO = "dello ";
	public static final String DELLA = "della ";
	public static final String DELL_APOSTROFO = "dell'";
	public static final String DEGLI = "degli ";
	public static final String DELLE = "delle ";
	public static final String DEI = "dei ";
	public static final String DAL = "dal ";
	public static final String DALLA = "dalla ";
	public static final String DALL_APOSTROFO = "dall'";
	public static final String DA_UN = "da un ";
	public static final String DA_UNO = "da uno ";
	public static final String DA_UNA = "da una ";
	public static final String DA_UN_APOSTROFO = "da un'";
	public static final String ELLA = "Ella";
	public static final String EGLI = "Egli";
	public static final String ESSA = "Essa";
	public static final String ESSO = "Esso";
	
	private static final String NORD = "nord";
	private static final String EST = "est";
	private static final String SUD = "sud";
	private static final String OVEST = "ovest";

	private static final String[] cardinali = {
			"zero", "uno", "due", "tre", "quattro", "cinque", "sei", "sette", "otto", "nove", "dieci"
	};

	/**
	 * La preposizione "a", "di", "da" o "in" fusa con l'articolo con cui comincia il nome: "in" + "il Tempio del Sole"
	 * fa "nel Tempio del Sole", "a" + "l'Eremo" fa "all'Eremo". Se il nome non comincia con un articolo, la
	 * preposizione resta staccata.
	 */
	public static String conPreposizione(String preposizione, String nomeConArticolo) {
		String[][] articoli = {{"il ", "l"}, {"lo ", "llo"}, {"la ", "lla"}, {"l'", "ll'"}, {"i ", "i"}, {"gli ", "gli"}, {"le ", "lle"}};
		String radice;
		switch (preposizione) {
			case "a": radice = "a"; break;
			case "di": radice = "de"; break;
			case "da": radice = "da"; break;
			case "in": radice = "ne"; break;
			default: throw new IllegalArgumentException("Preposizione non articolabile: " + preposizione);
		}
		for (String[] articolo : articoli) {
			if (nomeConArticolo.startsWith(articolo[0])) {
				String fusa = radice + articolo[1];
				String separatore = articolo[0].endsWith("'") ? "" : " ";
				return fusa + separatore + nomeConArticolo.substring(articolo[0].length());
			}
		}
		return preposizione + " " + nomeConArticolo;
	}

	/**
	 * Il testo con la prima lettera maiuscola: "il Tempio del Sole" diventa "Il Tempio del Sole".
	 */
	public static String inizialeMaiuscola(String testo) {
		return testo.isEmpty() ? testo : Character.toUpperCase(testo.charAt(0)) + testo.substring(1);
	}

	public static String getCardinaleM(int numero) {
		return cardinali[numero];
	}

	public static String getCardinaleF(int numero) {
		if (numero == 1)
			return "una";
		else
			return cardinali[numero];
	}

	private static final String[] ordinali = {
			null, "prim", "second", "terz", "quart", "quint", "sest", "settim", "ottav", "non", "decim",
			"undic", "dodic", "tredic", "quattordic", "quindic", "sedic", "diciassett", "diciott", "diciannov",
			"vent", "ventun", "ventidu", "ventitre", "ventiquattr", "venticinqu", "ventisei", "ventisett", "ventott", "ventinov",
			"trent", "trentun", "trentadu", "trentatre", "trentaquattr", "trentacinqu", "trentasei", "trentasett", "trentott", "trentanov",
			"quarant", "quarantun"
	};

	private static final String ESIM = "esim";

	public static String getOrdinaleM(int numero) {
		return getOrdinaleM(numero, false);
	}

	public static String getOrdinaleM(int numero, boolean articolo) {
		return getOrdinaleImpl(numero, articolo, IL, "o");
	}

	public static String getOrdinaleF(int numero) {
		return getOrdinaleF(numero, false);
	}

	public static String getOrdinaleF(int numero, boolean articolo) {
		return getOrdinaleImpl(numero, articolo, LA, "a");
	}

	private static String getOrdinaleImpl(int numero, boolean aggiungiArticolo, String qualeArticolo, String letteraFinale) {
		StringBuilder sb = new StringBuilder(15);
		if (aggiungiArticolo) {
			if (numero == 8 || numero == 11)
				sb.append(L_APOSTROFO);
			else
				sb.append(qualeArticolo);
		}
		sb.append(ordinali[numero]);
		if (numero > 10)
			sb.append(ESIM);
		sb.append(letteraFinale);
		return sb.toString();
	}

	/**
	 * Riporta la direzione di un punto della foresta rispetto ad un gruppo
	 * (per le informazioni su città, castelli, artefatti, o per gli eventi)
	 */
	public static String getDirezione(GruppoGiocatore g, CoordinateMD coordinate) {
		return getDirezione(g.getX(), g.getY(), coordinate.getX(), coordinate.getY());
	}

	public static String getDirezione(int daX, int daY, int aX, int aY) {
		int dx = (daX > aX ? daX - aX : aX - daX);
		int dy = (daY > aY ? daY - aY : aY - daY);
		StringBuilder sb = new StringBuilder();
		int distanza = dx + dy;
		if (distanza > 36) {
			if (distanza > 50)
				sb.append("molto ");
			sb.append("lontano in direzione ");
		} else {
			int giorni = distanza / 6;
			int resto = distanza % 6;
			sb.append("a ");
			if (giorni == 0) {
				if (resto == 1)
					sb.append("un'ora");
				else
					sb.append(cardinali[resto]).append(" ore");
			} else {
				if (resto < 4) {
					if (resto > 0)
						sb.append("poco piu' di ");
					if (giorni == 1)
						sb.append("un giorno");
					else
						sb.append(cardinali[giorni]).append(" giorni");
				} else {
					sb.append("quasi ").append(cardinali[giorni + 1]).append(" giorni");
				}
			}
			sb.append(" di cammino verso ");
		}
		if (daY < aY) {
			if (dy >= (dx + 1) / 2) {
				sb.append(SUD);
			}
			if (dx >= (dy + 1) / 2) {
				if (aX > daX) {
					sb.append(EST);
				} else {
					sb.append(OVEST);
				}
			}
		} else if (daY > aY) {
			if (dy >= (dx + 1) / 2) {
				sb.append(NORD);
			}
			if (dx >= (dy + 1) / 2) {
				if (aX > daX) {
					sb.append(EST);
				} else {
					sb.append(OVEST);
				}
			}
		} else {
			if (aX > daX)
				sb.append(EST);
			else
				sb.append(OVEST);
		}
		sb.append('.');
		return sb.toString();
	}

	public static final String[] STORIA = {
			"molto, molto tempo fa, quando il mondo era da poco stato creato,",
			"un gruppo di eroi venne riunito per liberarlo dalla minaccia dei draghi.",
			"questi, invece di comportarsi da bravi ragazzi, presto cominciarono a degenerare",
			"andando per locande a sbevazzare e a cercare compagne di sbornie e avventure",
			"finche' un giorno il piu' scoppiato di tutti sbaglio' strada e si ritrovo' in un postaccio conosciuto come"
	};

	public static final String[] PERSO = {
			"con la morte del cacciatore, il drago ha finalmente via libera... la foresta e' perduta!",
	};

	public static final String[] VINTO = {
			"congratulazioni!",
			"la foresta e' stata liberata",
			"i tuoi amici sono venuti per offrirti doni come loro salvatore",
			"possa la pace oggi conquistata durare a lungo!",
			""
	};
}
