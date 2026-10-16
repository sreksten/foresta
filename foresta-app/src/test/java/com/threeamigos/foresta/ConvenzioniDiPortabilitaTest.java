package com.threeamigos.foresta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le chiamate che dipendono dalla lingua del dispositivo non si usano senza dire quale lingua: con un telefono in arabo
 * {@code String.format("%d")} scrive cifre che il font bitmap del gioco non sa disegnare, con uno in turco
 * {@code "I".toLowerCase()} dà una "ı" senza punto che non trova, e con uno in thailandese un {@code SimpleDateFormat}
 * conta gli anni del calendario buddista. Il compilatore non se ne accorge, quindi lo controlla questo test, che legge i
 * sorgenti di tutti i moduli, UI compresa. La regola è passare {@code Locale.ROOT}.
 */
class ConvenzioniDiPortabilitaTest {

	private static final Pattern CASO_SENZA_LINGUA = Pattern.compile("\\.to(Lower|Upper)Case\\(\\)");
	private static final Pattern FORMAT_SENZA_LINGUA = Pattern.compile("String\\.format\\((?!\\s*Locale\\.)");
	private static final Pattern DATA_SENZA_LINGUA = Pattern.compile("new SimpleDateFormat\\(\"[^\"]*\"\\)");
	private static final Pattern FORMATTAZIONI_ALTRE = Pattern.compile("\\.printf\\(|\\.formatted\\(");

	@Test
	void nessunSorgenteUsaChiamateDipendentiDallaLinguaSenzaIndicarla() throws IOException {
		Set<String> trovati = new TreeSet<>();
		for (Path file : SorgentiDelProgetto.sorgenti(false)) {
			String codice = codice(file);
			String nome = SorgentiDelProgetto.radice().relativize(file).toString().replace('\\', '/');
			segnala(trovati, nome, "toLowerCase()/toUpperCase() senza Locale", CASO_SENZA_LINGUA, codice);
			segnala(trovati, nome, "String.format senza Locale", FORMAT_SENZA_LINGUA, codice);
			segnala(trovati, nome, "SimpleDateFormat senza Locale", DATA_SENZA_LINGUA, codice);
			segnala(trovati, nome, "printf/formatted (usare String.format con Locale.ROOT)", FORMATTAZIONI_ALTRE, codice);
		}
		assertTrue(trovati.isEmpty(), "Chiamate che dipendono dalla lingua del dispositivo, da fare con Locale.ROOT:\n" + String.join("\n", trovati));
	}

	// Chiudere il processo e' cosa della piattaforma (Main sul desktop, l'attivita' su Android): i moduli che vanno su
	// Android e iOS chiedono di uscire con NotificaUscitaDalGioco e non chiamano mai System.exit
	private static final Pattern USCITA_DAL_PROCESSO = Pattern.compile("System\\.exit\\(|Runtime\\.getRuntime\\(\\)\\.(exit|halt)\\(");

	@Test
	void soloLaPiattaformaChiudeIlProcesso() throws IOException {
		Set<String> trovati = new TreeSet<>();
		for (Path file : SorgentiDelProgetto.sorgenti(false)) {
			String nome = SorgentiDelProgetto.radice().relativize(file).toString().replace('\\', '/');
			if (nome.startsWith("foresta-swing/") || nome.startsWith("foresta-app/")) {
				continue;
			}
			segnala(trovati, nome, "System.exit/Runtime.exit: usare NotificaUscitaDalGioco", USCITA_DAL_PROCESSO, codice(file));
		}
		assertTrue(trovati.isEmpty(), "Questi moduli non devono chiudere il processo:\n" + String.join("\n", trovati));
	}

	@Test
	void laRegolaDellUscitaRiconosceIlSystemExit() {
		assertTrue(USCITA_DAL_PROCESSO.matcher("System.exit(0);").find());
		assertTrue(USCITA_DAL_PROCESSO.matcher("Runtime.getRuntime().halt(1);").find());
		assertFalse(USCITA_DAL_PROCESSO.matcher("Runtime.getRuntime().addShutdownHook(t);").find());
	}

	@Test
	void ognTestoCostruitoPerIlLogEUnFornitore() throws IOException {
		Set<String> trovati = new TreeSet<>();
		for (Path file : SorgentiDelProgetto.sorgenti(false)) {
			if (file.endsWith("strumenti/Logger.java")) {
				continue;
			}
			for (String chiamata : logConTestoCostruito(codice(file))) {
				trovati.add(SorgentiDelProgetto.radice().relativize(file).toString().replace('\\', '/') + ": Logger.log(" + chiamata + ")");
			}
		}
		assertTrue(trovati.isEmpty(), "Questi testi si costruiscono anche se nessuno li scrive: usare Logger.log(() -> ...), o "
				+ "if (Logger.isAttivo()) quando una variabile cambia:\n" + String.join("\n", trovati));
	}

	@Test
	void laRegolaDelLogRiconosceITestiCostruitiENonQuelliPronti() {
		assertFalse(logConTestoCostruito("Logger.log(\"un testo\");").size() > 0);
		assertFalse(logConTestoCostruito("Logger.log(messaggio);").size() > 0);
		assertFalse(logConTestoCostruito("Logger.log(e);").size() > 0);
		assertFalse(logConTestoCostruito("Logger.log(evento.getException());").size() > 0);
		assertFalse(logConTestoCostruito("Logger.log(() -> \"a \" + b);").size() > 0);
		assertFalse(logConTestoCostruito("if (Logger.isAttivo()) {\n\tLogger.log(\"a \" + b);\n}").size() > 0);
		assertEquals(1, logConTestoCostruito("Logger.log(\"a \" + b);").size());
		assertEquals(1, logConTestoCostruito("Logger.log(String.format(Locale.ROOT, \"%d\", 1));").size());
		assertEquals(1, logConTestoCostruito("Logger.log(headerEvento(evento));").size());
		assertEquals(2, logConTestoCostruito("Logger.log(\"a\" + b);\nLogger.log(x.nome());").size());
	}

	@Test
	void leRegoleRiconosconoICasiVietatiENonQuelliGiusti() {
		assertTrue(CASO_SENZA_LINGUA.matcher("nome.toLowerCase()").find());
		assertFalse(CASO_SENZA_LINGUA.matcher("nome.toLowerCase(Locale.ROOT)").find());
		assertTrue(FORMAT_SENZA_LINGUA.matcher("String.format(\"%d\", 5)").find());
		assertTrue(FORMAT_SENZA_LINGUA.matcher("String.format(fumetto, nome)").find());
		assertFalse(FORMAT_SENZA_LINGUA.matcher("String.format(Locale.ROOT, \"%d\", 5)").find());
		assertFalse(FORMAT_SENZA_LINGUA.matcher("String.format(\n\t\tLocale.ROOT, \"%d\", 5)").find());
		assertTrue(DATA_SENZA_LINGUA.matcher("new SimpleDateFormat(\"yyyy\")").find());
		assertFalse(DATA_SENZA_LINGUA.matcher("new SimpleDateFormat(\"yyyy\", Locale.ROOT)").find());
		assertTrue(FORMATTAZIONI_ALTRE.matcher("System.out.printf(\"%d\", 1)").find());
	}

	// Gli argomenti di Logger.log che sono un testo costruito (concatenazione, format, chiamata) e non un fornitore, un
	// testo gia' pronto o un'eccezione, fuori da una guardia Logger.isAttivo()
	private static List<String> logConTestoCostruito(String codice) {
		List<String> trovati = new ArrayList<>();
		Matcher m = Pattern.compile("Logger\\.log\\(").matcher(codice);
		while (m.find()) {
			int inizio = m.end();
			int fine = chiusura(codice, inizio);
			if (fine < 0) {
				continue;
			}
			String argomento = codice.substring(inizio, fine).trim();
			boolean pronto = argomento.matches("\"(?:[^\"\\\\]|\\\\.)*\"") || argomento.matches("[A-Za-z_]\\w*")
					|| argomento.equals("evento.getException()") || argomento.startsWith("() ->");
			boolean inGuardia = codice.substring(Math.max(0, m.start() - 80), m.start()).contains("Logger.isAttivo()");
			if (!pronto && !inGuardia) {
				trovati.add(argomento.replaceAll("\\s+", " "));
			}
		}
		return trovati;
	}

	// L'indice della parentesi che chiude quella gia' aperta in inizio - 1, o -1
	private static int chiusura(String codice, int inizio) {
		int profondita = 1;
		char aperta = 0;
		for (int i = inizio; i < codice.length(); i++) {
			char c = codice.charAt(i);
			if (aperta != 0) {
				if (c == '\\') {
					i++;
				} else if (c == aperta) {
					aperta = 0;
				}
			} else if (c == '"' || c == '\'') {
				aperta = c;
			} else if (c == '(') {
				profondita++;
			} else if (c == ')' && --profondita == 0) {
				return i;
			}
		}
		return -1;
	}

	private static void segnala(Set<String> trovati, String nome, String regola, Pattern modello, String codice) {
		if (modello.matcher(codice).find()) {
			trovati.add(nome + ": " + regola);
		}
	}

	// Senza commenti: un esempio scritto in un javadoc non e' una chiamata
	private static String codice(Path file) throws IOException {
		return new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "")
				.replaceAll("(?m)^\\s*//[^\\n]*", "")
				.replaceAll("(?<![:\"])//[^\\n\"]*$", "");
	}
}
