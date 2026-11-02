package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Partite giocate da un giocatore automatico nelle lingue inglesi: tutto il testo che il gioco racconta (narrazione, intermezzi) dev'essere in
 * inglese, senza parole italiane (e senza spazi doppi o spazi prima della punteggiatura).
 */
class PartiteInIngleseTest {

	private static final int PASSI = 3000;
	private static final Set<String> NOTE_SALVATE = new HashSet<>(Arrays.asList("la lealtà", "Ignominia della fuga", "Pelle di corteccia", "Grido di battaglia", "Nebbia", "Negazione della Magia"));

	// Parole italiane che in inglese non esistono
	private static final Pattern ITALIANO = Pattern.compile("(?<![-\\w])(attacca|attaccano|colpisce|colpiscono|muore|muoiono|cade|cadono|lancia|lanciano|beve|bevono|prende|prendono|trova|trovano|perde|perdono|guadagna|guadagnano|ottiene|ottengono|entra|entrano|esce|escono|dorme|dormono|riposa|riposano|scappa|scappano|fugge|fuggono|evoca|canta|cantano|parla|parlano|chiede|chiedono|risponde|rispondono|vince|vincono|sconfigge|sconfiggono|ferisce|feriscono|manca|mancano|schiva|schivano|resiste|resistono|subisce|subiscono|recupera|recuperano|rimane|rimangono|riceve|ricevono|viene|vengono|diventa|diventano|colpito|colpita|sconfitto|sconfitta|il|gli|della|dello|delle|degli|dei|del|che|non|sono|nel|nella|nei|nelle|una|uno|voi|più|anche|dove|questo|questa|hanno|poi|giorno|ora|della|alla|alle|allo|agli|dal|dalla|dalle|sul|sulla|sulle|suo|sua|suoi|sue|mio|mia|nostro|nostra|vostro|vostra|perché|però|quando|molto|tutto|tutti|ancora|sempre|dopo|prima|ogni|fra|tra)(?![-\\w])|\\bè\\b|\\bsi è\\b");

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	private static void paragrafi(PaginaIntermezzo pagina, Set<String> testi) {
		if (pagina.getTesto() != null) {
			testi.add(pagina.getTesto());
		}
		pagina.getBattuteProgrammate().forEach(b -> {
			if (b.getBattuta().getTesto() != null) {
				testi.add(b.getBattuta().getTesto());
			}
		});
	}

	/**
	 * Tutte le stringhe che le viste in sola lettura della partita (quelle che legge la UI: missioni, personaggi, inventario, mappa, statistiche...) danno,
	 * seguendo i metodi senza argomenti fino a una certa profondità.
	 */
	private static void leggiLeViste(Object oggetto, Set<String> testi, int profondita, Set<Object> visti) {
		if (oggetto == null || profondita < 0 || !visti.add(oggetto)) {
			return;
		}
		if (oggetto instanceof String) {
			testi.add((String) oggetto);
			return;
		}
		if (oggetto instanceof Collection) {
			for (Object o : new java.util.ArrayList<Object>((Collection<?>) oggetto)) {
				leggiLeViste(o, testi, profondita, visti);
			}
			return;
		}
		if (!oggetto.getClass().getName().startsWith("com.threeamigos") || oggetto.getClass().isEnum()) {
			return;
		}
		for (java.lang.reflect.Method m : oggetto.getClass().getMethods()) {
			if (m.getParameterCount() != 0 || m.getDeclaringClass() == Object.class || java.lang.reflect.Modifier.isStatic(m.getModifiers())
					|| !(m.getName().startsWith("get") || m.getName().startsWith("is") || m.getName().startsWith("descri"))) {
				continue;
			}
			try {
				leggiLeViste(m.invoke(oggetto), testi, profondita - 1, visti);
			} catch (java.lang.reflect.InvocationTargetException | IllegalAccessException | RuntimeException e) {
				// una vista che non ha niente da dire in questo momento
			}
		}
	}

	private static Map<String, String> giocaESegnala(String lingua, long primoSeme, long ultimoSeme) {
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		Map<String, String> problemi = new TreeMap<>();
		Comando[] classi = {Comando.GUERRIERO, Comando.MAGO, Comando.LADRO, Comando.CHIERICO, Comando.DRUIDO, Comando.BARDO, Comando.SACERDOTE, Comando.ELFO};
		for (long seme = primoSeme; seme <= ultimoSeme; seme++) {
			Testi.impostaLingua(lingua);
			Set<String> testi = new HashSet<>();
			try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme)) {
				BusEventi.iscriviti(NotificaTestoFrase.class, e -> testi.add(e.getMessaggio()));
				BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> testi.add(e.getMessaggio()));
				BusEventi.iscriviti(NotificaPaginaIntermezzo.class, e -> paragrafi(e.getPagina(), testi));
				partita.iniziaCon("Arsenio", Comando.MASCHIO, classi[(int) (seme % classi.length)], () -> {
				});
				GiocatoreAutomatico giocatore = new GiocatoreAutomatico(partita, new Random(seme));
				com.threeamigos.foresta.interfacce.VistaPartita vista = new VistaPartitaMotore();
				for (int passo = 0; passo < PASSI; passo++) {
					Stato stato = partita.stato();
					if (passo % 100 == 0) {
						leggiLeViste(vista, testi, 4, java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
					}
					if (stato == Stato.GIOCO_PERSO || stato == Stato.GIOCO_VINTO) {
						break;
					}
					Comando comando = giocatore.scegli();
					try {
						if (comando == null) {
							partita.scatta();
						} else {
							partita.comando(comando);
						}
					} catch (AssertionError | RuntimeException e) {
						break;
					}
				}
			}
			for (String testo : testi) {
				// Le righe grezze delle missioni (CHIAVE=...) sono dati, non testo mostrato
				// (e le note dei modificatori: la vista le dà come stanno salvate, in italiano, e la UI le traduce con TestoDeiModificatori.nota)
				if (testo == null || testo.contains("CHIAVE=") || NOTE_SALVATE.contains(testo)) {
					continue;
				}
				Matcher m = ITALIANO.matcher(testo.toLowerCase(Locale.ROOT));
				if (m.find()) {
					problemi.putIfAbsent(testo, "parola italiana «" + m.group() + "»");
				} else if (testo.contains("  ") || testo.matches("(?s).*\\s[,.;:!?].*")) {
					problemi.putIfAbsent(testo, "spaziatura");
				}
			}
		}
		return problemi;
	}

	@Test
	void unaPartitaInInglesePiaccioloInglese() {
		Map<String, String> problemi = giocaESegnala("en-GB", 400, 415);
		// Un solo esempio per contesto della parola italiana (due parole prima e dopo, senza cifre), per leggere l'elenco senza ripetizioni
		Map<String, String> esempi = new TreeMap<>();
		problemi.forEach((t, p) -> {
			Matcher m = ITALIANO.matcher(t.toLowerCase(Locale.ROOT));
			String chiave = p;
			if (m.find()) {
				String[] parole = t.toLowerCase(Locale.ROOT).replaceAll("[0-9]+", "#").split("\\s+");
				int i = Arrays.asList(parole).indexOf(m.group());
				chiave = String.join(" ", Arrays.copyOfRange(parole, Math.max(0, i - 2), Math.min(parole.length, i + 3)));
			}
			esempi.putIfAbsent(chiave, t.length() > 200 ? t.substring(0, 200) + "…" : t);
		});
		StringBuilder sb = new StringBuilder();
		esempi.forEach((k, t) -> sb.append("\n[").append(k).append("] ").append(t));
		assertTrue(problemi.isEmpty(), problemi.size() + " testi, " + esempi.size() + " contesti da sistemare:" + sb);
	}
}
