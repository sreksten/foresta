package com.threeamigos.foresta.testi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
 * <li>{@link #get(String, String)}: un file nel pacchetto {@code testi} (qualunque modulo può metterci il suo), con una chiave a piacere;</li>
 * <li>{@link #testo(Enum, String)}: i testi di una costante di enum, per esempio il nome e la descrizione di un
 * {@code TipoAttributo}. Il file sta accanto all'enum e si chiama come lei, in minuscolo con i trattini bassi
 * ({@code tipo_attributo_it.properties}), e la chiave è {@code COSTANTE.VOCE} ({@code FORZA.NOME}). Così un'enum non ha
 * nel codice nessun testo da tradurre.</li>
 * </ul>
 * La lingua è l'italiano finché la piattaforma non ne sceglie un'altra con {@link #impostaLingua(String)}. Può avere una
 * regione ({@code en-GB}): il testo si cerca allora nel file della regione ({@code _en_GB}), poi in quello della lingua
 * ({@code _en}) e infine in italiano, la lingua in cui il gioco è scritto; così il file di una regione contiene solo ciò
 * che differisce dalla lingua ("armour", "colour"), e una traduzione incompleta non lascia buchi.
 *
 * @author Stefano Reksten
 */
public final class Testi {

	/**
	 * La lingua in cui il gioco è scritto, e a cui si ripiega.
	 */
	public static final String LINGUA_PREDEFINITA = "it";

	private static final List<Runnable> ASCOLTATORI = new java.util.concurrent.CopyOnWriteArrayList<>();

	/**
	 * La lingua scelta e i file da cercare per lei, sempre cambiati insieme: chi legge da un altro thread non vede mai la
	 * lingua nuova con i file di quella vecchia.
	 */
	private static final class LinguaScelta {
		// Come "en-GB"
		final String codice;
		// I suffissi dei file da cercare, dal più specifico al più generico ("en_GB", "en", "it")
		final String[] catena;

		LinguaScelta(String codice, String[] catena) {
			this.codice = codice;
			this.catena = catena;
		}
	}

	// volatile perché si legge da più thread; si sostituisce intera, mai un pezzo alla volta
	private static volatile LinguaScelta scelta = new LinguaScelta(LINGUA_PREDEFINITA, new String[] {LINGUA_PREDEFINITA});

	// Per nome del file e lingua; una lingua senza file vale un elenco vuoto
	private static final ConcurrentMap<String, Properties> CACHE = new ConcurrentHashMap<>();

	// Per enum e lingua: i testi delle costanti si chiedono spesso (a ogni fotogramma, per un cartiglio), quindi la ricerca
	// non deve costruire stringhe per trovare il file
	private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Properties>> CACHE_DELLE_ENUM = new ConcurrentHashMap<>();

	/**
	 * Sceglie la lingua dei testi, con il suo codice a due lettere ("it", "en") e, se serve, la regione ("en-GB", "en_gb"
	 * vale lo stesso); con null o vuoto torna l'italiano.
	 */
	public static void impostaLingua(String codice) {
		String precedente = scelta.codice;
		scelta = linguaDa(codice);
		if (!scelta.codice.equals(precedente)) {
			for (Runnable ascoltatore : ASCOLTATORI) {
				ascoltatore.run();
			}
		}
	}

	/**
	 * Chiede di essere avvisato quando la lingua cambia, per buttare via quel che si è costruito nella lingua di prima (una
	 * grammatica caricata, un catalogo).
	 */
	public static void alCambioDiLingua(Runnable ascoltatore) {
		ASCOLTATORI.add(ascoltatore);
	}

	/**
	 * I suffissi dei file della lingua in uso, dal più specifico al più generico e senza l'italiano, che è il file senza
	 * suffisso di chi non ne ha uno ({@code fiabe.txt}): per en-GB sono {@code en_GB} e {@code en}, per l'italiano nessuno.
	 */
	public static List<String> suffissiDellaLingua() {
		List<String> suffissi = new ArrayList<>();
		for (String suffisso : scelta.catena) {
			if (!LINGUA_PREDEFINITA.equals(suffisso)) {
				suffissi.add(suffisso);
			}
		}
		return suffissi;
	}

	// Da "en-GB" (o "en_gb") ricava la lingua da mostrare ("en-GB") e la catena dei file da cercare (en_GB, en, it)
	private static LinguaScelta linguaDa(String codice) {
		String pulito = codice == null ? "" : codice.trim().replace('_', '-');
		if (pulito.isEmpty()) {
			return new LinguaScelta(LINGUA_PREDEFINITA, new String[] {LINGUA_PREDEFINITA});
		}
		String[] parti = pulito.split("-", 2);
		String soloLingua = parti[0].toLowerCase(Locale.ROOT);
		List<String> suffissi = new ArrayList<>();
		String lingua;
		if (parti.length > 1 && !parti[1].isEmpty()) {
			String regione = parti[1].toUpperCase(Locale.ROOT);
			suffissi.add(soloLingua + "_" + regione);
			lingua = soloLingua + "-" + regione;
		} else {
			lingua = soloLingua;
		}
		suffissi.add(soloLingua);
		if (!LINGUA_PREDEFINITA.equals(soloLingua)) {
			suffissi.add(LINGUA_PREDEFINITA);
		}
		return new LinguaScelta(lingua, suffissi.toArray(new String[0]));
	}

	/**
	 * Il codice della lingua in uso ("it" finché la piattaforma non ne sceglie un'altra), con la regione se c'è ("en-GB").
	 */
	public static String getLingua() {
		return scelta.codice;
	}

	/**
	 * Il testo con quella chiave nel file di quel nome (senza lingua né estensione), nella lingua scelta o, se lì manca,
	 * in italiano.
	 */
	public static Optional<String> get(String file, String chiave) {
		for (String suffisso : scelta.catena) {
			String testo = carica(file, suffisso).getProperty(chiave);
			if (testo != null) {
				return Optional.of(testo);
			}
		}
		return Optional.empty();
	}

	/**
	 * Le voci numerate di un file, {@code PREFISSO.1}, {@code PREFISSO.2}... finché ce ne sono (una voce vuota è una voce):
	 * un elenco di parole o di pagine. Si legge nella lingua scelta, voce per voce, con il ripiego di sempre.
	 */
	public static java.util.List<String> serie(String file, String prefisso) {
		java.util.List<String> voci = new ArrayList<>();
		for (int i = 1;; i++) {
			Optional<String> voce = get(file, prefisso + "." + i);
			if (!voce.isPresent()) {
				return voci;
			}
			voci.add(voce.get());
		}
	}

	/**
	 * Come {@link #get(String, String)}, con i valori al posto di {@code {0}}, {@code {1}}... Non usa {@code MessageFormat},
	 * che dipende dal {@code Locale} del dispositivo e chiede di raddoppiare gli apostrofi (in italiano e in francese sono
	 * ovunque). Se la chiave manca, la chiave stessa, così l'errore si nota subito.
	 */
	public static String formatta(String file, String chiave, Object... valori) {
		return sostituisci(get(file, chiave).orElse(chiave), valori);
	}

	/**
	 * Il modello con {@code {0}}, {@code {1}}... sostituiti dai valori; un segnaposto senza valore resta com'è.
	 */
	public static String sostituisci(String modello, Object... valori) {
		String testo = modello;
		for (int i = 0; i < valori.length; i++) {
			testo = testo.replace("{" + i + "}", String.valueOf(valori[i]));
		}
		return testo;
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
		for (String suffisso : scelta.catena) {
			String testo = proprietaDellEnum(perLingua, classe, suffisso).getProperty(chiave);
			if (testo != null) {
				return Optional.of(testo);
			}
		}
		return Optional.empty();
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

	// I testi di quell'enum in quella lingua, letti una volta sola e tenuti in memoria
	private static Properties proprietaDellEnum(ConcurrentMap<String, Properties> perLingua, Class<?> classe, String codiceLingua) {
		return perLingua.computeIfAbsent(codiceLingua, codice -> leggi(classe, nomeDelFile(classe) + "_" + codice));
	}

	// I testi di quel file in quella lingua, letti una volta sola e tenuti in memoria
	private static Properties carica(String file, String codiceLingua) {
		return CACHE.computeIfAbsent(file + "_" + codiceLingua, nome -> leggi(Testi.class, nome));
	}

	// Il file accanto alla classe indicata (nello stesso pacchetto); una lingua senza file vale un elenco vuoto
	private static Properties leggi(Class<?> accanto, String nome) {
		return leggi(accanto.getResourceAsStream(nome + ".properties"), nome);
	}

	/**
	 * I testi di un flusso in UTF-8, che chiude; null vale un elenco vuoto.
	 *
	 * @throws IllegalStateException se il flusso non si legge
	 */
	static Properties leggi(InputStream flusso, String nome) {
		Properties testi = new Properties();
		if (flusso == null) {
			return testi;
		}
		try (InputStream daChiudere = flusso) {
			testi.load(new InputStreamReader(daChiudere, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException("File di testi illeggibile: " + nome, e);
		}
		return testi;
	}

	/**
	 * Una classe di soli metodi statici: non si istanzia.
	 */
	private Testi() {
	}

}
