package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.testi.GestoreGrammatiche;
import com.threeamigos.foresta.testi.GrammaticaNonValidaException;
import com.threeamigos.foresta.testi.GrammaticaPigra;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.threeamigos.foresta.testi.CaricatoreDiGrammatiche;

/**
 * Nomi ed effetti degli artefatti presi da una grammatica (artefatti2.txt, che ne descrive il formato, o
 * ingredienti.txt per gli ingredienti magici).
 * Ogni riga prodotta è il nome dell'artefatto con dentro dei marcatori {@code <chiave:valore>}: qui si
 * tolgono dal testo e diventano un {@link Risultato}, che dice che cosa ha l'artefatto ma non quanto,
 * perché i valori dipendono dal livello e li decide il generatore.
 * <p>
 * GestoreGrammatiche non è thread-safe, quindi {@link #genera} è sincronizzato.
 */
final class GrammaticaArtefatti {

	private static final String GRAMMATICA = "/com/threeamigos/foresta/artefatti/artefatti2.txt";
	private static final String GRAMMATICA_INGREDIENTI = "/com/threeamigos/foresta/artefatti/ingredienti.txt";
	private static final String POST_PRODUZIONE = "/com/threeamigos/foresta/artefatti/artefatti2_pp.txt";

	/**
	 * Fin dove si cercano le radici {@code <TIPO>_<n>}
	 */
	private static final int EFFETTI_CERCATI = 10;

	private static final Pattern MARCATORE = Pattern.compile("<([a-z]+):([^<>]*)>");
	private static final Pattern MODIFICATORE = Pattern.compile("([A-Z_]+)([+-]\\d+)");

	private final GestoreGrammatiche grammatica;
	/**
	 * Per ogni tipo presente nella grammatica il numero minimo e massimo di effetti, cioè la radice {@code <TIPO>_<n>} più
	 * piccola e più grande (le radici vanno dalla prima all'ultima senza buchi)
	 */
	private final Map<TipoArtefatto, Integer> effettiMinimi = new EnumMap<>(TipoArtefatto.class);
	private final Map<TipoArtefatto, Integer> effettiMassimi = new EnumMap<>(TipoArtefatto.class);

	GrammaticaArtefatti(GestoreGrammatiche grammatica) {
		this.grammatica = grammatica;
		for (TipoArtefatto tipo : TipoArtefatto.values()) {
			for (int effetti = 0; effetti <= EFFETTI_CERCATI; effetti++) {
				if (esisteRadice(tipo, effetti)) {
					effettiMinimi.putIfAbsent(tipo, effetti);
					effettiMassimi.put(tipo, effetti);
				}
			}
		}
	}

	// Le due grammatiche, caricate una volta per lingua al primo uso. Come ogni grammatica del gioco, se una manca o è
	// sbagliata il caricamento solleva GrammaticaNonValidaException, e il gioco non parte (vedi PrecaricatoreDeiTesti)
	private static final GrammaticaPigra ARTEFATTI = new GrammaticaPigra(() -> CaricatoreDiGrammatiche.carica(
			GrammaticaArtefatti.class, GRAMMATICA, POST_PRODUZIONE));
	private static final GrammaticaPigra INGREDIENTI = new GrammaticaPigra(() -> CaricatoreDiGrammatiche.carica(
			GrammaticaArtefatti.class, GRAMMATICA_INGREDIENTI, POST_PRODUZIONE));

	/**
	 * La grammatica degli artefatti del gioco (artefatti2.txt), nella lingua in uso.
	 *
	 * @throws GrammaticaNonValidaException se manca o non è valida
	 */
	static GrammaticaArtefatti artefatti() {
		return new GrammaticaArtefatti(ARTEFATTI.get());
	}

	/**
	 * La grammatica degli ingredienti magici (pergamene, gemme…: ingredienti.txt), nella lingua in uso.
	 *
	 * @throws GrammaticaNonValidaException se manca o non è valida
	 */
	static GrammaticaArtefatti ingredienti() {
		return new GrammaticaArtefatti(INGREDIENTI.get());
	}

	/**
	 * Una grammatica degli artefatti da una risorsa qualunque, con la post-produzione del gioco: per i test.
	 *
	 * @throws GrammaticaNonValidaException se manca o non è valida
	 */
	static GrammaticaArtefatti carica(String risorsa) {
		return new GrammaticaArtefatti(CaricatoreDiGrammatiche.carica(GrammaticaArtefatti.class, risorsa, POST_PRODUZIONE));
	}

	/**
	 * Carica subito le due grammatiche, invece di aspettare il primo uso (vedi {@code PrecaricatoreDeiTesti}).
	 *
	 * @throws GrammaticaNonValidaException se una manca o non è valida
	 */
	static void precarica() {
		ARTEFATTI.get();
		INGREDIENTI.get();
	}

	/**
	 * Il nome di un oggetto che fa salire di livello un artefatto (produzione NOME_AGGIORNAMENTO di artefatti2.txt). Lo
	 * produce la stessa grammatica che genera gli artefatti, così artefatti2.txt si carica una volta sola.
	 */
	static String nomeAggiornamento() {
		GestoreGrammatiche grammatica = ARTEFATTI.get();
		synchronized (grammatica) {
			return grammatica.produci("NOME_AGGIORNAMENTO").get(0).trim();
		}
	}

	// Se la grammatica ha la produzione TIPO_N (per esempio SPADA_2): setRadice lancia un'eccezione se non c'è, e qui la si
	// usa come domanda
	private boolean esisteRadice(TipoArtefatto tipo, int effetti) {
		try {
			grammatica.setRadice(radice(tipo, effetti));
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static String radice(TipoArtefatto tipo, int effetti) {
		return tipo.name() + '_' + effetti;
	}

	boolean supporta(TipoArtefatto tipo) {
		return effettiMassimi.containsKey(tipo);
	}

	/**
	 * Un artefatto con quel numero di effetti: se la grammatica non ne prevede tanti, o così pochi, il massimo o il
	 * minimo che ha.
	 */
	Risultato genera(TipoArtefatto tipo, int effetti) {
		// La grammatica è condivisa (una per lingua) e non si può usare da due thread insieme: il lucchetto è la grammatica stessa
		synchronized (grammatica) {
			return generaDallaGrammatica(tipo, effetti);
		}
	}

	private Risultato generaDallaGrammatica(TipoArtefatto tipo, int effetti) {
		Integer massimo = effettiMassimi.get(tipo);
		if (massimo == null) {
			throw new IllegalArgumentException("La grammatica degli artefatti non genera il tipo " + tipo);
		}
		String riga = String.join(" ", grammatica.produci(radice(tipo, Math.max(effettiMinimi.get(tipo), Math.min(massimo, effetti)))));
		grammatica.ripristina();
		return interpreta(riga);
	}

	/**
	 * Toglie i marcatori dalla riga e li trasforma in dati. Un marcatore sconosciuto o malformato è un errore
	 * della grammatica e lancia IllegalArgumentException, così i test lo trovano.
	 */
	static Risultato interpreta(String riga) {
		Risultato risultato = new Risultato();
		Matcher matcher = MARCATORE.matcher(riga);
		while (matcher.find()) {
			String chiave = matcher.group(1);
			String valore = matcher.group(2).trim();
			switch (chiave) {
				case "mod":
					Matcher modificatore = MODIFICATORE.matcher(valore);
					if (!modificatore.matches()) {
						throw new IllegalArgumentException("Modificatore malformato: " + matcher.group() + " in " + riga);
					}
					risultato.modificatori.add(new Modificatore(TipoAttributo.valueOf(modificatore.group(1)),
							Integer.parseInt(modificatore.group(2))));
					break;
				case "danno":
					risultato.danni.add(TipoDanno.valueOf(valore));
					break;
				case "soprannome":
					if (risultato.soprannome == null && !valore.isEmpty()) {
						risultato.soprannome = valore;
					}
					break;
				case "descrizione":
					if (risultato.descrizione == null && !valore.isEmpty()) {
						risultato.descrizione = valore;
					}
					break;
				default:
					throw new IllegalArgumentException("Marcatore sconosciuto: " + matcher.group() + " in " + riga);
			}
		}
		// Un marcatore prima di una virgola lascerebbe uno spazio: "del Tuono<danno:SONICO>, della…"
		risultato.nome = matcher.replaceAll(" ").replaceAll("\\s+", " ").replaceAll(" ([,.;:!?])", "$1").trim();
		if (risultato.nome.contains("<") || risultato.nome.contains(">")) {
			throw new IllegalArgumentException("Marcatore non chiuso in " + riga);
		}
		return risultato;
	}

	/**
	 * Che cosa ha un artefatto generato: nome, soprannome e descrizione (questi due possono mancare),
	 * modificatori con la loro intensità e tipi di danno degli incantamenti.
	 */
	static final class Risultato {

		private String nome;
		private String soprannome;
		private String descrizione;
		private final List<Modificatore> modificatori = new ArrayList<>();
		private final List<TipoDanno> danni = new ArrayList<>();

		String getNome() {
			return nome;
		}

		String getSoprannome() {
			return soprannome;
		}

		String getDescrizione() {
			return descrizione;
		}

		List<Modificatore> getModificatori() {
			return Collections.unmodifiableList(modificatori);
		}

		List<TipoDanno> getDanni() {
			return Collections.unmodifiableList(danni);
		}

		int getEffetti() {
			return modificatori.size() + danni.size();
		}
	}

	/**
	 * Un modificatore come lo scrive la grammatica: l'attributo e l'intensità (da -3 a +3), non ancora
	 * scalata sul livello.
	 */
	static final class Modificatore {

		private final TipoAttributo attributo;
		private final int intensita;

		Modificatore(TipoAttributo attributo, int intensita) {
			this.attributo = attributo;
			this.intensita = intensita;
		}

		TipoAttributo getAttributo() {
			return attributo;
		}

		int getIntensita() {
			return intensita;
		}
	}
}
