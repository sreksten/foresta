package com.threeamigos.foresta.tipi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * I testi del gioco che stanno in file di risorse e non nel codice, uno per lingua: {@code <nome>_<lingua>.properties},
 * per esempio {@code note_personaggi_it.properties}. I file sono in UTF-8 (il {@code ResourceBundle} di Java 8 li
 * leggerebbe in ISO-8859-1) e si leggono una volta sola.
 * <p>
 * Due modi di usarli:
 * <ul>
 * <li>{@link #get(String, String)}: un file accanto a questa classe, con una chiave a piacere;</li>
 * <li>{@link #testo(Enum, String)}: i testi di una costante di enum, per esempio il nome e la descrizione di un
 * {@code TipoAttributo}. Il file sta accanto all'enum e si chiama come lei, in minuscolo con i trattini bassi
 * ({@code tipo_attributo_it.properties}), e la chiave è {@code COSTANTE.VOCE} ({@code FORZA.NOME}). Così un'enum non ha
 * nel codice nessun testo da tradurre.</li>
 * </ul>
 * La lingua è l'italiano finché la piattaforma non ne sceglie un'altra con {@link #impostaLingua(String)}. Un testo che
 * manca nella lingua scelta si cerca in italiano, la lingua in cui il gioco è scritto: una traduzione incompleta non
 * lascia buchi.
 */
public final class Testi {

	/**
	 * La lingua in cui il gioco è scritto, e a cui si ripiega.
	 */
	public static final String LINGUA_PREDEFINITA = "it";

	private static volatile String lingua = LINGUA_PREDEFINITA;

	// Per nome del file e lingua; una lingua senza file vale un elenco vuoto
	private static final ConcurrentMap<String, Properties> CACHE = new ConcurrentHashMap<>();

	// Per enum e lingua: i testi delle costanti si chiedono spesso (a ogni fotogramma, per un cartiglio), quindi la ricerca
	// non deve costruire stringhe per trovare il file
	private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Properties>> CACHE_DELLE_ENUM = new ConcurrentHashMap<>();

	private Testi() {
	}

	/**
	 * Sceglie la lingua dei testi, con il suo codice a due lettere ("it", "en"); con null o vuoto torna l'italiano.
	 */
	public static void impostaLingua(String codice) {
		lingua = codice == null || codice.trim().isEmpty() ? LINGUA_PREDEFINITA : codice.trim().toLowerCase(Locale.ROOT);
	}

	public static String getLingua() {
		return lingua;
	}

	/**
	 * Il testo con quella chiave nel file di quel nome (senza lingua né estensione), nella lingua scelta o, se lì manca,
	 * in italiano.
	 */
	public static Optional<String> get(String file, String chiave) {
		String testo = carica(file, lingua).getProperty(chiave);
		if (testo == null && !LINGUA_PREDEFINITA.equals(lingua)) {
			testo = carica(file, LINGUA_PREDEFINITA).getProperty(chiave);
		}
		return Optional.ofNullable(testo);
	}

	/**
	 * Un testo di una costante di enum, per esempio {@code testo(TipoAttributo.FORZA, "NOME")}: nella lingua scelta o, se
	 * lì manca, in italiano. Vuoto se la costante non ha quella voce (è il caso delle voci facoltative: il nome proprio di
	 * una locazione che non è unica).
	 */
	public static Optional<String> testo(Enum<?> costante, String voce) {
		Class<?> classe = costante.getDeclaringClass();
		ConcurrentMap<String, Properties> perLingua = CACHE_DELLE_ENUM.computeIfAbsent(classe, k -> new ConcurrentHashMap<>());
		String chiave = costante.name() + '.' + voce;
		String codice = lingua;
		String testo = proprietaDellEnum(perLingua, classe, codice).getProperty(chiave);
		if (testo == null && !LINGUA_PREDEFINITA.equals(codice)) {
			testo = proprietaDellEnum(perLingua, classe, LINGUA_PREDEFINITA).getProperty(chiave);
		}
		return Optional.ofNullable(testo);
	}

	/**
	 * Come {@link #testo(Enum, String)}, ma se il testo manca anche in italiano si ha il nome della costante: un buco nei
	 * testi si vede, e non rompe niente.
	 */
	public static String testoOCostante(Enum<?> costante, String voce) {
		return testo(costante, voce).orElse(costante.name());
	}

	/**
	 * Il nome del file dei testi di un'enum, senza lingua né estensione: il nome della classe in minuscolo con i trattini
	 * bassi ({@code TipoAttributo} diventa {@code tipo_attributo}).
	 */
	public static String nomeDelFile(Class<?> enumerazione) {
		StringBuilder nome = new StringBuilder();
		for (char c : enumerazione.getSimpleName().toCharArray()) {
			if (Character.isUpperCase(c) && nome.length() > 0) {
				nome.append('_');
			}
			nome.append(Character.toLowerCase(c));
		}
		return nome.toString();
	}

	private static Properties proprietaDellEnum(ConcurrentMap<String, Properties> perLingua, Class<?> classe, String codiceLingua) {
		return perLingua.computeIfAbsent(codiceLingua, codice -> leggi(classe, nomeDelFile(classe) + "_" + codice));
	}

	private static Properties carica(String file, String codiceLingua) {
		return CACHE.computeIfAbsent(file + "_" + codiceLingua, nome -> leggi(Testi.class, nome));
	}

	// Il file accanto alla classe indicata (nello stesso pacchetto); una lingua senza file vale un elenco vuoto
	private static Properties leggi(Class<?> accanto, String nome) {
		Properties testi = new Properties();
		try (InputStream flusso = accanto.getResourceAsStream(nome + ".properties")) {
			if (flusso != null) {
				testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			throw new IllegalStateException("File di testi illeggibile: " + nome, e);
		}
		return testi;
	}
}
