package com.threeamigos.foresta.tipi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * I testi del gioco che stanno in file di risorse e non nel codice, uno per lingua: {@code <nome>_<lingua>.properties},
 * per esempio {@code note_personaggi_it.properties}, accanto a questa classe. I file sono in UTF-8 (il
 * {@code ResourceBundle} di Java 8 li leggerebbe in ISO-8859-1) e si leggono una volta sola.
 * <p>
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

	private Testi() {
	}

	/**
	 * Sceglie la lingua dei testi, con il suo codice a due lettere ("it", "en"); con null o vuoto torna l'italiano.
	 */
	public static void impostaLingua(String codice) {
		lingua = codice == null || codice.trim().isEmpty() ? LINGUA_PREDEFINITA : codice.trim().toLowerCase(java.util.Locale.ROOT);
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

	private static Properties carica(String file, String codiceLingua) {
		return CACHE.computeIfAbsent(file + "_" + codiceLingua, nome -> {
			Properties testi = new Properties();
			try (InputStream flusso = Testi.class.getResourceAsStream(nome + ".properties")) {
				if (flusso != null) {
					testi.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				throw new IllegalStateException("File di testi illeggibile: " + nome, e);
			}
			return testi;
		});
	}
}
