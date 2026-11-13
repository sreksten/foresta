package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.strumenti.Maiuscole;

import java.util.List;
import java.util.Locale;

/**
 * Le regole dell'inglese, americano e britannico: l'articolo determinativo è sempre "the", l'indeterminativo è "a" o "an"
 * secondo il suono ("a unicorn", "an hour": un nome che fa eccezione dichiara il suo con la voce {@code SUONO}); le
 * preposizioni non si fondono con l'articolo ("of the"); le serie hanno la virgola prima dell'"and" finale.
 */
public final class LinguaInglese implements RegoleDellaLingua {

	static final LinguaInglese ISTANZA = new LinguaInglese();

	private static final String[] CARDINALI = {
			"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
	};

	private static final String[] ORDINALI = {
			null, "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth",
			"eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth"
	};

	private static final String[] DECINE = {
			null, null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
	};

	private static final String[] DECINE_ORDINALI = {
			null, null, "twentieth", "thirtieth", "fortieth", "fiftieth", "sixtieth", "seventieth", "eightieth", "ninetieth"
	};

	private LinguaInglese() {
	}

	@Override
	public String articoloDeterminativo(Nome nome, boolean plurale) {
		return "the ";
	}

	@Override
	public String articoloIndeterminativo(Nome nome, boolean plurale) {
		if (plurale) {
			return "some ";
		}
		Suono suono = nome.getSuono().orElseGet(() -> suonoIniziale(nome.getSingolare()));
		return suono == Suono.VOCALE ? "an " : "a ";
	}

	@Override
	public String preposizioneArticolata(Preposizione preposizione, Nome nome, boolean plurale) {
		return preposizione == Preposizione.VICINO ? "the " : preposizione(preposizione) + " the ";
	}

	@Override
	public String daAgente(Nome nome) {
		return nome.isAgenteDeterminato() ? "by the " : "by " + articoloIndeterminativo(nome, false);
	}

	@Override
	public String conPreposizione(Preposizione preposizione, String nomeConArticolo) {
		// Un nome che comincia con l'articolo ("The Teetotal Wolf's Bough") dopo una preposizione, e quindi a metà frase, lo vuole in minuscolo
		String nome = nomeConArticolo.startsWith("The ") ? "the " + nomeConArticolo.substring("The ".length()) : nomeConArticolo;
		return preposizione == Preposizione.VICINO ? nome : preposizione(preposizione) + " " + nome;
	}

	private static String preposizione(Preposizione preposizione) {
		switch (preposizione) {
			case DI:
				return "of";
			case A:
			case PRESSO:
				return "to";
			case DA:
				return "from";
			case ARRIVO:
				return "in";
			case SU:
				return "on";
			default:
				return "in";
		}
	}

	@Override
	public String senzaArticolo(String nomeConArticolo) {
		return nomeConArticolo.replaceFirst("(?i)^(the|an|a) ", "");
	}

	@Override
	public String inizialeMaiuscola(String testo) {
		return Maiuscole.inizialeMaiuscola(testo);
	}

	@Override
	public String cardinale(int numero, Genere genere) {
		return numero >= 0 && numero < CARDINALI.length ? CARDINALI[numero] : String.valueOf(numero);
	}

	@Override
	public String ordinale(int numero, Genere genere, boolean conArticolo) {
		String parola;
		if (numero < 1 || numero > 99) {
			parola = numero + "th";
		} else if (numero < ORDINALI.length) {
			parola = ORDINALI[numero];
		} else if (numero % 10 == 0) {
			// Le decine tonde hanno un ordinale proprio ("twentieth"); le altre uniscono la decina e l'ordinale dell'unità
			// ("twenty-first")
			parola = DECINE_ORDINALI[numero / 10];
		} else {
			parola = DECINE[numero / 10] + "-" + ORDINALI[numero % 10];
		}
		return conArticolo ? "the " + parola : parola;
	}

	@Override
	public String elenco(List<String> elementi) {
		return Elenchi.unisci(elementi, ", ", " and ", ", and ");
	}

	@Override
	public Suono suonoIniziale(String testo) {
		String t = testo.toLowerCase(Locale.ROOT);
		int i = 0;
		while (i < t.length() && !Character.isLetter(t.charAt(i))) {
			i++;
		}
		if (i == t.length()) {
			return Suono.CONSONANTE;
		}
		// "hour", "honest", "heir": la h non si pronuncia; "unicorn", "one", "user": la vocale suona come consonante
		if (t.startsWith("hour", i) || t.startsWith("honest", i) || t.startsWith("honor", i) || t.startsWith("honour", i)
				|| t.startsWith("heir", i)) {
			return Suono.VOCALE;
		}
		if (t.startsWith("uni", i) && i + 3 < t.length() && "aeiou".indexOf(t.charAt(i + 3)) < 0
				|| t.startsWith("use", i) || t.startsWith("user", i) || t.startsWith("one", i) || t.startsWith("eu", i)
				|| t.startsWith("utensil", i)) {
			return Suono.CONSONANTE;
		}
		return "aeiou".indexOf(t.charAt(i)) >= 0 ? Suono.VOCALE : Suono.CONSONANTE;
	}
}
