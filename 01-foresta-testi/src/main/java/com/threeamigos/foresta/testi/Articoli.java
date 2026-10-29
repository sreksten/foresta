package com.threeamigos.foresta.testi;

/**
 * Gli articoli e le preposizioni articolate italiane, già con lo spazio finale (o l'apostrofo) per attaccarli al nome che
 * seguono, e le due funzioni che li usano: fondere una preposizione con l'articolo di un nome e mettere la maiuscola
 * iniziale. Sono testi italiani: la localizzazione completa verrà dopo.
 *
 * @author Stefano Reksten
 */
public final class Articoli {

	/** L'articolo determinativo maschile singolare davanti a una consonante Vale "il ". */
	public static final String IL = "il ";
	/** L'articolo determinativo maschile singolare davanti a "s" impura, "z", "gn" o "ps" Vale "lo ". */
	public static final String LO = "lo ";
	/** L'articolo determinativo femminile singolare Vale "la ". */
	public static final String LA = "la ";
	/** L'articolo determinativo elisa davanti a una vocale, maschile o femminile Vale "l'". */
	public static final String L_APOSTROFO = "l'";
	/** L'articolo determinativo maschile plurale Vale "i ". */
	public static final String I = "i ";
	/** L'articolo determinativo maschile plurale davanti a vocale, "s" impura o "z" Vale "gli ". */
	public static final String GLI = "gli ";
	/** L'articolo determinativo femminile plurale Vale "le ". */
	public static final String LE = "le ";
	/** L'articolo indeterminativo maschile singolare Vale "un ". */
	public static final String UN = "un ";
	/** L'espressione per un paio di cose, come i guanti o gli stivali Vale "un paio di ". */
	public static final String UN_PAIO_DI = "un paio di ";
	/** L'articolo indeterminativo maschile singolare davanti a "s" impura o "z" Vale "uno ". */
	public static final String UNO = "uno ";
	/** L'articolo indeterminativo femminile singolare Vale "una ". */
	public static final String UNA = "una ";
	/** L'articolo indeterminativo femminile elisa davanti a una vocale Vale "un'". */
	public static final String UN_APOSTROFO = "un'";
	/** Il plurale indeterminativo maschile Vale "alcuni ". */
	public static final String ALCUNI = "alcuni ";
	/** Il plurale indeterminativo femminile Vale "alcune ". */
	public static final String ALCUNE = "alcune ";
	/** Il plurale di "un paio di" Vale "alcune paia di ". */
	public static final String ALCUNE_PAIA_DI = "alcune paia di ";
	/** La preposizione "di" articolata davanti a un nome maschile singolare Vale "del ". */
	public static final String DEL = "del ";
	/** La preposizione "di" articolata davanti a un nome maschile singolare che comincia con "s" impura o "z" Vale "dello ". */
	public static final String DELLO = "dello ";
	/** La preposizione "di" articolata davanti a un nome femminile singolare Vale "della ". */
	public static final String DELLA = "della ";
	/** La preposizione "di" articolata elisa davanti a una vocale Vale "dell'". */
	public static final String DELL_APOSTROFO = "dell'";
	/** La preposizione "di" articolata davanti a un nome maschile plurale che comincia con vocale, "s" impura o "z" Vale "degli ". */
	public static final String DEGLI = "degli ";
	/** La preposizione "di" articolata davanti a un nome femminile plurale Vale "delle ". */
	public static final String DELLE = "delle ";
	/** La preposizione "di" articolata davanti a un nome maschile plurale Vale "dei ". */
	public static final String DEI = "dei ";
	/** La preposizione "da" articolata davanti a un nome maschile singolare Vale "dal ". */
	public static final String DAL = "dal ";
	/** La preposizione "da" articolata davanti a un nome femminile singolare Vale "dalla ". */
	public static final String DALLA = "dalla ";
	/** La preposizione "da" articolata elisa davanti a una vocale Vale "dall'". */
	public static final String DALL_APOSTROFO = "dall'";
	/** "da un", davanti a un nome maschile singolare Vale "da un ". */
	public static final String DA_UN = "da un ";
	/** "da uno", davanti a un nome maschile singolare che comincia con "s" impura o "z" Vale "da uno ". */
	public static final String DA_UNO = "da uno ";
	/** "da una", davanti a un nome femminile singolare Vale "da una ". */
	public static final String DA_UNA = "da una ";
	/** "da un'", elisa davanti a una vocale femminile Vale "da un'". */
	public static final String DA_UN_APOSTROFO = "da un'";
	/** Il pronome per una donna all'inizio di una frase Vale "Ella". */
	public static final String ELLA = "Ella";
	/** Il pronome per un uomo all'inizio di una frase Vale "Egli". */
	public static final String EGLI = "Egli";
	/** Il pronome per una cosa femminile all'inizio di una frase Vale "Essa". */
	public static final String ESSA = "Essa";
	/** Il pronome per una cosa maschile all'inizio di una frase Vale "Esso". */
	public static final String ESSO = "Esso";

	/**
	 * Una classe di costanti e di funzioni statiche: non si istanzia.
	 */
	private Articoli() {
	}

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

}
