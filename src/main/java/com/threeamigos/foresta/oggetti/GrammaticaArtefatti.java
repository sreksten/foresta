package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.GrammarBean;
import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nomi ed effetti degli artefatti presi da una grammatica (artefatti2.txt, che ne descrive il formato).
 * Ogni riga prodotta è il nome dell'artefatto con dentro dei marcatori {@code <chiave:valore>}: qui si
 * tolgono dal testo e diventano un {@link Risultato}, che dice che cosa ha l'artefatto ma non quanto,
 * perché i valori dipendono dal livello e li decide il generatore.
 * <p>
 * GrammarBean non è thread-safe, quindi {@link #genera} è sincronizzato.
 */
final class GrammaticaArtefatti {

	private static final String GRAMMATICA = "/com/threeamigos/foresta/motore/artefatti2.txt";
	private static final String POST_PRODUZIONE = "/com/threeamigos/foresta/motore/artefatti2_pp.txt";

	private static final Pattern MARCATORE = Pattern.compile("<([a-z]+):([^<>]*)>");
	private static final Pattern MODIFICATORE = Pattern.compile("([A-Z_]+)([+-]\\d+)");

	private final GrammarBean grammatica;
	/**
	 * Per ogni tipo presente nella grammatica il numero massimo di effetti, cioè la radice <TIPO>_<n> più grande
	 */
	private final Map<TipoArtefatto, Integer> effettiMassimi = new EnumMap<>(TipoArtefatto.class);

	GrammaticaArtefatti(GrammarBean grammatica) {
		this.grammatica = grammatica;
		for (TipoArtefatto tipo : TipoArtefatto.values()) {
			for (int effetti = 0; esisteRadice(tipo, effetti); effetti++) {
				effettiMassimi.put(tipo, effetti);
			}
		}
	}

	/**
	 * La grammatica del gioco, o null se non si carica: in quel caso il generatore resta alle sue tabelle.
	 */
	static GrammaticaArtefatti caricaOppureNull() {
		try (InputStream grammatica = GrammaticaArtefatti.class.getResourceAsStream(GRAMMATICA);
			 InputStream postProduzione = GrammaticaArtefatti.class.getResourceAsStream(POST_PRODUZIONE)) {
			return new GrammaticaArtefatti(new GrammarBean(grammatica, postProduzione));
		} catch (InvalidGrammarException | IOException | RuntimeException e) {
			Logger.log(e);
			return null;
		}
	}

	private boolean esisteRadice(TipoArtefatto tipo, int effetti) {
		try {
			grammatica.setRootNode(radice(tipo, effetti));
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
	 * Un artefatto con al più quel numero di effetti: se la grammatica non ne prevede tanti, il massimo che ha.
	 */
	synchronized Risultato genera(TipoArtefatto tipo, int effetti) {
		Integer massimo = effettiMassimi.get(tipo);
		if (massimo == null) {
			throw new IllegalArgumentException("La grammatica degli artefatti non genera il tipo " + tipo);
		}
		String riga = String.join(" ", grammatica.produce(radice(tipo, Math.max(0, Math.min(massimo, effetti)))));
		grammatica.reset();
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
		risultato.nome = matcher.replaceAll(" ").replaceAll("\\s+", " ").trim();
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
