package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Gioca una partita con il giocatore automatico e raccoglie tutto il testo che l'utente può leggere a video: la narrazione
 * (frasi e paragrafi), le pagine e le battute degli intermezzi e le stringhe che le viste in sola lettura della partita danno
 * alla UI (missioni, personaggi, inventario, mappa, statistiche...). Lo usano {@link PartiteInIngleseTest}, che cerca le
 * parole italiane, e {@link RaccoltaTestiInIngleseTest}, che scrive i testi in un file da far rivedere.
 */
final class RaccoglitoreDeiTestiVisibili {

	private static final Comando[] CLASSI = {Comando.GUERRIERO, Comando.MAGO, Comando.LADRO, Comando.CHIERICO, Comando.DRUIDO, Comando.BARDO, Comando.SACERDOTE, Comando.ELFO};

	private RaccoglitoreDeiTestiVisibili() {
	}

	static void paragrafi(PaginaIntermezzo pagina, Set<String> testi) {
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
	static void leggiLeViste(Object oggetto, Set<String> testi, int profondita, Set<Object> visti) {
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

	/**
	 * I testi che si leggono giocando una partita col seme dato (la classe del capo dipende dal seme), in quella lingua.
	 *
	 * @param passi quanti comandi al massimo manda il giocatore automatico
	 */
	static Set<String> gioca(String lingua, long seme, int passi) {
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		Testi.impostaLingua(lingua);
		Set<String> testi = new HashSet<>();
		try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme)) {
			BusEventi.iscriviti(NotificaTestoFrase.class, e -> testi.add(e.getMessaggio()));
			BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> testi.add(e.getMessaggio()));
			BusEventi.iscriviti(NotificaPaginaIntermezzo.class, e -> paragrafi(e.getPagina(), testi));
			partita.iniziaCon("Arsenio", Comando.MASCHIO, CLASSI[(int) (seme % CLASSI.length)], () -> {
			});
			GiocatoreAutomatico giocatore = new GiocatoreAutomatico(partita, new Random(seme));
			com.threeamigos.foresta.interfacce.VistaPartita vista = new VistaPartitaMotore();
			for (int passo = 0; passo < passi; passo++) {
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
		return testi;
	}
}
