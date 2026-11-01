package com.threeamigos.foresta.testi;

import java.util.Objects;
import java.util.Optional;

/**
 * Un nome comune con quel che serve per articolarlo e declinarlo: singolare, plurale, genere, suono iniziale e pronome con
 * cui lo si riprende. Non ha logica di lingua: le regole sono di {@link RegoleDellaLingua}.
 * <p>
 * I dati di una costante di enum stanno nei suoi file dei testi, nella lingua scelta (vedi {@link #di(Enum)}): voci
 * {@code NOME_SINGOLARE}, {@code NOME_PLURALE}, {@code GENERE} ("M", "F", "N"), {@code SUONO} (solo se fa eccezione alla
 * regola della lingua) e {@code PRONOME}, {@code AGENTE_DETERMINATO} ("true" per i nomi unici o noti) e, per i nomi che hanno articoli tutti loro,
 * {@code ADS}, {@code ADP}, {@code AIS}, {@code AIP}. Così un nome nuovo, o una lingua nuova, non chiede codice.
 */
public final class Nome {

	private final String singolare;
	private final String plurale;
	private final Genere genere;
	private final Suono suono;
	private final String pronome;
	private final boolean agenteDeterminato;
	private final String[] articoli;

	/**
	 * @param suono   null se lo decide la lingua dalla lettera iniziale
	 * @param pronome null se il nome non ha un pronome proprio
	 */
	public Nome(String singolare, String plurale, Genere genere, Suono suono, String pronome) {
		this(singolare, plurale, genere, suono, pronome, false);
	}

	/**
	 * @param agenteDeterminato se, quando compie un'azione, lo si nomina col determinativo ("dal Drago") e non con
	 *                          l'indeterminativo ("da un goblin"): è il caso dei nomi unici o noti
	 */
	public Nome(String singolare, String plurale, Genere genere, Suono suono, String pronome, boolean agenteDeterminato) {
		this(singolare, plurale, genere, suono, pronome, agenteDeterminato, null);
	}

	/**
	 * @param articoli gli articoli che fanno eccezione alla regola della lingua, nell'ordine determinativo singolare,
	 *                 determinativo plurale, indeterminativo singolare, indeterminativo plurale (null o un elemento null
	 *                 se vale la regola): per i nomi che si usano solo al plurale, come «gli schinieri», «un paio di schinieri»
	 */
	public Nome(String singolare, String plurale, Genere genere, Suono suono, String pronome, boolean agenteDeterminato, String[] articoli) {
		this.articoli = articoli == null ? new String[4] : articoli.clone();
		this.agenteDeterminato = agenteDeterminato;
		this.singolare = Objects.requireNonNull(singolare);
		this.plurale = plurale == null ? singolare : plurale;
		this.genere = genere == null ? Genere.NEUTRO : genere;
		this.suono = suono;
		this.pronome = pronome;
	}

	/**
	 * Un nome di cui servono solo le forme (senza genere e senza eccezioni di suono).
	 */
	public static Nome di(String singolare, String plurale) {
		return new Nome(singolare, plurale, Genere.NEUTRO, null, null);
	}

	/**
	 * Il nome di una costante di enum, letto dai suoi file dei testi nella lingua scelta; se non ha {@code NOME_SINGOLARE}
	 * si prova con {@code NOME}, e se manca anche quello il nome è quello della costante.
	 */
	public static Nome di(Enum<?> costante) {
		String singolare = Testi.testo(costante, "NOME_SINGOLARE").orElseGet(() -> Testi.testoOCostante(costante, "NOME"));
		return new Nome(singolare,
				Testi.testo(costante, "NOME_PLURALE").orElse(singolare),
				Genere.dallaSigla(Testi.testo(costante, "GENERE").orElse(null)),
				Suono.dalNome(Testi.testo(costante, "SUONO").orElse(null)),
				Testi.testo(costante, "PRONOME").orElse(null),
				Testi.testo(costante, "AGENTE_DETERMINATO").map(v -> v.trim().equalsIgnoreCase("true")).orElse(false),
				new String[] {Testi.testo(costante, "ADS").orElse(null), Testi.testo(costante, "ADP").orElse(null),
						Testi.testo(costante, "AIS").orElse(null), Testi.testo(costante, "AIP").orElse(null)});
	}

	/**
	 * Come {@link #di(Enum)}, ma con le voci in un file a parte ({@code <file>_<lingua>.properties}, chiavi
	 * {@code COSTANTE.VOCE}): per le enum i cui file dei testi hanno già un loro schema.
	 */
	public static Nome di(String file, Enum<?> costante) {
		String prefisso = costante.name() + ".";
		String singolare = Testi.get(file, prefisso + "NOME_SINGOLARE").orElse(costante.name());
		return new Nome(singolare,
				Testi.get(file, prefisso + "NOME_PLURALE").orElse(singolare),
				Genere.dallaSigla(Testi.get(file, prefisso + "GENERE").orElse(null)),
				Suono.dalNome(Testi.get(file, prefisso + "SUONO").orElse(null)),
				Testi.get(file, prefisso + "PRONOME").orElse(null),
				Testi.get(file, prefisso + "AGENTE_DETERMINATO").map(v -> v.trim().equalsIgnoreCase("true")).orElse(false),
				new String[] {Testi.get(file, prefisso + "ADS").orElse(null), Testi.get(file, prefisso + "ADP").orElse(null),
						Testi.get(file, prefisso + "AIS").orElse(null), Testi.get(file, prefisso + "AIP").orElse(null)});
	}

	public String getSingolare() {
		return singolare;
	}

	public String getPlurale() {
		return plurale;
	}

	public String getNome(boolean plurale) {
		return plurale ? this.plurale : singolare;
	}

	public Genere getGenere() {
		return genere;
	}

	/**
	 * Il suono dichiarato, se il nome fa eccezione alla regola della lingua.
	 */
	public Optional<Suono> getSuono() {
		return Optional.ofNullable(suono);
	}

	/**
	 * Il pronome personale con cui si riprende il nome ("Egli", "Esso", "He", "It"), se ne ha uno.
	 */
	public Optional<String> getPronome() {
		return Optional.ofNullable(pronome);
	}

	/**
	 * L'articolo che il nome dichiara per sé, se fa eccezione alla regola della lingua (voci {@code ADS}, {@code ADP},
	 * {@code AIS}, {@code AIP}), col suo separatore finale.
	 */
	public Optional<String> getArticolo(boolean determinativo, boolean plurale) {
		return Optional.ofNullable(articoli[(determinativo ? 0 : 2) + (plurale ? 1 : 0)]);
	}

	public boolean isAgenteDeterminato() {
		return agenteDeterminato;
	}

	@Override
	public String toString() {
		return singolare;
	}
}
