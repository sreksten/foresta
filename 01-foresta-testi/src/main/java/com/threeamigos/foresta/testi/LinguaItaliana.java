package com.threeamigos.foresta.testi;

import java.util.List;
import java.util.Locale;

/**
 * Le regole dell'italiano: l'articolo dipende dal genere e da come comincia il nome (il/lo/l'/la/i/gli/le, un/uno/una/un'),
 * le preposizioni si fondono con l'articolo (del, nello, all'), i numeri si accordano nel genere. I numeri sono quelli di
 * {@link Numeri}, le preposizioni articolate quelle di {@link Articoli}.
 */
public final class LinguaItaliana implements RegoleDellaLingua {

	static final LinguaItaliana ISTANZA = new LinguaItaliana();

	private LinguaItaliana() {
	}

	@Override
	public String articoloDeterminativo(Nome nome, boolean plurale) {
		Suono suono = suonoDi(nome);
		boolean femminile = nome.getGenere() == Genere.FEMMINILE;
		if (plurale) {
			return femminile ? Articoli.LE : suono == Suono.CONSONANTE ? Articoli.I : Articoli.GLI;
		}
		if (suono == Suono.VOCALE) {
			return Articoli.L_APOSTROFO;
		}
		if (femminile) {
			return Articoli.LA;
		}
		return suono == Suono.S_IMPURA ? Articoli.LO : Articoli.IL;
	}

	@Override
	public String articoloIndeterminativo(Nome nome, boolean plurale) {
		boolean femminile = nome.getGenere() == Genere.FEMMINILE;
		if (plurale) {
			return femminile ? Articoli.ALCUNE : Articoli.ALCUNI;
		}
		Suono suono = suonoDi(nome);
		if (femminile) {
			return suono == Suono.VOCALE ? Articoli.UN_APOSTROFO : Articoli.UNA;
		}
		return suono == Suono.S_IMPURA ? Articoli.UNO : Articoli.UN;
	}

	@Override
	public String preposizioneArticolata(Preposizione preposizione, Nome nome, boolean plurale) {
		String articolo = articoloDeterminativo(nome, plurale);
		// Il nome serve solo a far fondere preposizione e articolo: si toglie dal risultato
		String fusa = Articoli.conPreposizione(preposizione(preposizione), articolo + "x");
		return fusa.substring(0, fusa.length() - 1);
	}

	@Override
	public String daAgente(Nome nome) {
		return nome.isAgenteDeterminato() ? preposizioneArticolata(Preposizione.DA, nome, false)
				: "da " + articoloIndeterminativo(nome, false);
	}

	@Override
	public String conPreposizione(Preposizione preposizione, String nomeConArticolo) {
		return Articoli.conPreposizione(preposizione(preposizione), nomeConArticolo);
	}

	private static String preposizione(Preposizione preposizione) {
		return preposizione.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public String inizialeMaiuscola(String testo) {
		return Articoli.inizialeMaiuscola(testo);
	}

	@Override
	public String cardinale(int numero, Genere genere) {
		return genere == Genere.FEMMINILE ? Numeri.getCardinaleF(numero) : Numeri.getCardinaleM(numero);
	}

	@Override
	public String ordinale(int numero, Genere genere, boolean conArticolo) {
		return genere == Genere.FEMMINILE ? Numeri.getOrdinaleF(numero, conArticolo) : Numeri.getOrdinaleM(numero, conArticolo);
	}

	@Override
	public String elenco(List<String> elementi) {
		return Elenchi.unisci(elementi, ", ", " e ", " e ");
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
		char c = t.charAt(i);
		char dopo = i + 1 < t.length() ? t.charAt(i + 1) : ' ';
		if ("aeiouàèéìòù".indexOf(c) >= 0) {
			return Suono.VOCALE;
		}
		boolean dopoConsonante = Character.isLetter(dopo) && "aeiouàèéìòù".indexOf(dopo) < 0;
		if (c == 's' && dopoConsonante || c == 'z' || c == 'x' || c == 'y'
				|| c == 'g' && dopo == 'n' || c == 'p' && (dopo == 's' || dopo == 'n')) {
			return Suono.S_IMPURA;
		}
		return Suono.CONSONANTE;
	}

	private Suono suonoDi(Nome nome) {
		return nome.getSuono().orElseGet(() -> suonoIniziale(nome.getSingolare()));
	}
}
