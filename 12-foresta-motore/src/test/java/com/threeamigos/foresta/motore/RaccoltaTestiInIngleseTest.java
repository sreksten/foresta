package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Raccoglie in un file i testi che si leggono giocando una serie di partite automatiche in inglese, da far rivedere (grammatica,
 * parole italiane rimaste, tono troppo formale: vedi {@link RaccoglitoreDeiTestiVisibili}). Non fa parte della suite: si esegue
 * a mano con {@code -DraccogliTesti=true}, per esempio
 * <pre>mvn -o test -pl 12-foresta-motore -am -Dtest=RaccoltaTestiInIngleseTest -DraccogliTesti=true -Dsurefire.failIfNoSpecifiedTests=false</pre>
 * Parametri (propriet&agrave; di sistema, tutti facoltativi): {@code raccogliTesti.lingua} (default {@code en-GB}),
 * {@code raccogliTesti.semi} (quante partite, default 40), {@code raccogliTesti.passi} (comandi per partita, default 3000), {@code raccogliTesti.campioni} (esempi per modello, default 2),
 * {@code raccogliTesti.uscita} (il file, default {@code target/testi-inglesi/<lingua>.txt}).
 * <p>
 * Il file ha un testo per riga, con in testa quante volte è uscito. Per non avere decine di migliaia di righe quasi uguali, i testi
 * si raggruppano per "modello" (le prime {@value #PAROLE_DEL_MODELLO} parole, con le cifre sostituite da {@code #} e i nomi propri
 * da {@code N}) e di ogni modello si tengono al massimo {@code raccogliTesti.campioni} esempi (default 2). Non sono testo
 * mostrato e si saltano: gli identificatori (UUID, nomi di costanti), le righe grezze delle missioni ({@code CHIAVE=...}) e le
 * note dei modificatori salvate in italiano (la UI le traduce), come in {@link PartiteInIngleseTest}.
 */
class RaccoltaTestiInIngleseTest {

	static final int PAROLE_DEL_MODELLO = 5;
	// UUID e nomi di costanti: li danno le viste, ma non si leggono a video
	private static final Pattern IDENTIFICATORE = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|[A-Z0-9_]+");

	/** Le prime parole del testo, senza cifre né nomi propri: i testi che hanno lo stesso modello dicono la stessa cosa a persone diverse. */
	static String modello(String testo) {
		String normalizzato = testo.replaceAll("[0-9]+", "#").replaceAll("(?<=\\s)[A-Z][a-z']+", "N");
		String[] parole = normalizzato.trim().split("\\s+");
		return String.join(" ", Arrays.copyOfRange(parole, 0, Math.min(parole.length, PAROLE_DEL_MODELLO)));
	}

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	@EnabledIfSystemProperty(named = "raccogliTesti", matches = "true")
	void raccogliITestiDiUnaSerieDiPartite() throws IOException {
		String lingua = System.getProperty("raccogliTesti.lingua", "en-GB");
		int semi = Integer.getInteger("raccogliTesti.semi", 12);
		int passi = Integer.getInteger("raccogliTesti.passi", 3000);
		Path uscita = Paths.get(System.getProperty("raccogliTesti.uscita", "target/testi-inglesi/" + lingua + ".txt"));

		int campioni = Integer.getInteger("raccogliTesti.campioni", 2);
		// Modello -> gli esempi veri (al massimo "campioni") e quante volte è uscito un testo di quel modello
		Map<String, List<String>> esempi = new TreeMap<>();
		Map<String, Integer> volte = new TreeMap<>();
		for (long seme = 400; seme < 400 + semi; seme++) {
			Set<String> testi = RaccoglitoreDeiTestiVisibili.gioca(lingua, seme, passi);
			for (String testo : testi) {
				if (testo == null || testo.trim().isEmpty() || testo.contains("CHIAVE=") || PartiteInIngleseTest.NOTE_SALVATE.contains(testo)
						|| IDENTIFICATORE.matcher(testo).matches()) {
					continue;
				}
				String chiave = modello(testo);
				List<String> propri = esempi.computeIfAbsent(chiave, k -> new ArrayList<>());
				if (propri.size() < campioni && !propri.contains(testo)) {
					propri.add(testo);
				}
				volte.merge(chiave, 1, Integer::sum);
			}
		}
		List<String> righe = new ArrayList<>();
		righe.add("# Testi visibili di " + semi + " partite automatiche (" + lingua + "), " + esempi.size() + " modelli. Formato: <volte in cui è uscito un testo di questo modello>\t<testo>");
		esempi.forEach((chiave, propri) -> propri.forEach(testo -> righe.add(volte.get(chiave) + "\t" + testo.replace("\r", " ").replace("\n", " ⏎ "))));
		if (uscita.getParent() != null) {
			Files.createDirectories(uscita.getParent());
		}
		Files.write(uscita, righe, StandardCharsets.UTF_8);
		System.out.println("Testi scritti in " + uscita.toAbsolutePath() + ": " + esempi.size());
		assertFalse(esempi.isEmpty(), "nessun testo raccolto");
	}
}
