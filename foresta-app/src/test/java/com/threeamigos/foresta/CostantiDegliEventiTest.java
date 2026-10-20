package com.threeamigos.foresta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ogni evento passa a {@code EventoBase} la costante di {@code TipoEvento} che porta il suo nome (NotificaFoo → NOTIFICA_FOO): una costante
 * sbagliata fa riconoscere un evento per un altro (la leggono lo sniffer e i log). Controlla i sorgenti di tutti i moduli, così vale anche per
 * gli eventi che nasceranno.
 */
class CostantiDegliEventiTest {

	private static final Pattern COSTANTE = Pattern.compile("TipoEvento\\.([A-Z_]+)");

	// Le poche costanti che non coincidono con il nome della classe, di proposito
	private static final Map<String, String> ECCEZIONI = new HashMap<>();
	static {
		ECCEZIONI.put("RichiestaSelezioneSiNo", "RICHIESTA_SELEZIONE_SI_O_NO");
		// "Exception" in italiano
		ECCEZIONI.put("InternoException", "INTERNO_ECCEZIONE");
	}

	/** Il nome della classe in maiuscolo con i trattini bassi fra le parole ("RefreshUI" → "REFRESH_UI", "SpriteATempo" → "SPRITE_A_TEMPO") */
	private static String comeCostante(String nomeClasse) {
		return nomeClasse.replaceAll("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", "_").toUpperCase(java.util.Locale.ROOT);
	}

	@Test
	void ogniEventoUsaLaCostanteDelSuoNome() throws IOException {
		Set<String> sbagliati = new TreeSet<>();
		int controllati = 0;
		for (Path file : SorgentiDelProgetto.sorgenti(false)) {
			String percorso = file.toString().replace('\\', '/');
			if (!percorso.contains("/eventi/")) {
				continue;
			}
			Set<String> costanti = new LinkedHashSet<>();
			Matcher matcher = COSTANTE.matcher(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
			while (matcher.find()) {
				costanti.add(matcher.group(1));
			}
			if (costanti.isEmpty()) {
				continue;
			}
			controllati++;
			String classe = file.getFileName().toString().replace(".java", "");
			String attesa = ECCEZIONI.getOrDefault(classe, comeCostante(classe));
			if (!costanti.equals(java.util.Collections.singleton(attesa))) {
				sbagliati.add(classe + " usa " + costanti + " invece di " + attesa);
			}
		}
		assertTrue(controllati > 100, "ho trovato troppo pochi eventi: " + controllati);
		assertTrue(sbagliati.isEmpty(), "Costanti di TipoEvento sbagliate:\n" + String.join("\n", sbagliati));
	}
}
