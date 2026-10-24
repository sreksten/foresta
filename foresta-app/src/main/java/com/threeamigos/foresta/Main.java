package com.threeamigos.foresta;

import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.eventi.notifiche.NotificaUscitaDalGioco;
import com.threeamigos.foresta.interfacce.ControlloreDiGioco;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.salvataggi.GestoreBestiarioSuFile;
import com.threeamigos.foresta.salvataggi.GestorePunteggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreSalvataggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreTrofeiSuFile;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.strumenti.temporizzatore.TemporizzatoreJ2SE;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import com.threeamigos.foresta.ui.EsecutoreSwing;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Orientamento;
import com.threeamigos.foresta.ui.sviluppo.FinestraDiProva;

import java.util.Locale;

/**
 * Il punto d'ingresso del gioco: l'unico posto che conosce insieme motore e interfaccia grafica e li collega.
 * Sceglie dove gira la logica (il thread di Swing), registra gli ascoltatori del bus eventi, crea l'automa e
 * la finestra e li lascia dialogare solo tramite eventi.
 * <p>
 * Argomenti riconosciuti (senza distinzione fra maiuscole e minuscole): {@code ORIZZONTALE}, {@code VERTICALE},
 * {@code TUTTOSCHERMO}, {@code SALTALOGO}, {@code BARRACLASSICA}, {@code ZOOM=<numero>}, {@code MODALITA_DI_PROVA}.
 */
public class Main {

	private static Orientamento orientamento = Orientamento.ORIZZONTALE;
	private static boolean tuttoSchermo = false;
	// Per le partite di prova: niente logo iniziale, si va dritti all'INTRO
	private static boolean saltaLogo = false;
	// La barra delle icone che si ingrandisce sotto il cursore, come il Dock di macOS (solo in orizzontale);
	// con l'argomento BARRACLASSICA si torna alla barra fissa
	private static boolean barraDock = true;

	/**
	 * Imposta le opzioni di avvio dagli argomenti; quelli sconosciuti, e uno {@code ZOOM=} che non è un numero positivo,
	 * sono ignorati con una riga nel log.
	 */
	private static void leggiArgomenti(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("ORIZZONTALE")) {
				orientamento = Orientamento.ORIZZONTALE;
			} else if (arg.equalsIgnoreCase("VERTICALE")) {
				orientamento = Orientamento.VERTICALE;
            } else if (arg.equalsIgnoreCase("TUTTOSCHERMO")) {
                tuttoSchermo = true;
            } else if (arg.equalsIgnoreCase("SALTALOGO")) {
                saltaLogo = true;
            } else if (arg.equalsIgnoreCase("BARRACLASSICA")) {
                barraDock = false;
            } else if (arg.toUpperCase(Locale.ROOT).startsWith("ZOOM=")) {
                try {
                    LivelloDiZoom.imposta(Double.parseDouble(arg.substring("ZOOM=".length())));
                } catch (IllegalArgumentException e) {
                    // Anche NumberFormatException: un valore che non è un numero, o non è positivo, lascia lo zoom predefinito
                    Logger.log(() -> "Argomento ignorato, lo zoom resta " + LivelloDiZoom.valore() + ": " + arg);
                }
            } else if (arg.equalsIgnoreCase("MODALITA_DI_PROVA")) {
                ModalitaDiProva.setAttiva(true);
            } else {
                Logger.log(() -> "Argomento sconosciuto ignorato: " + arg);
            }
        }
	}

	/**
	 * Avvia il gioco: il resto del lavoro lo fanno gli eventi, perché la finestra, quando è pronta, avvisa l'automa che può cominciare.
	 */
	// Un impulso fallito si dice a tutti sul bus: la UI lo mostra al giocatore
	private static void segnalaImpulsoFallito(String messaggio, RuntimeException errore) {
		BusEventi.pubblica(messaggio == null ? new InternoException(errore) : new InternoException(messaggio, errore));
	}

	public static void main(String[] args) throws Exception {

		// Tutta la logica di gioco gira sull'Event Dispatch Thread di Swing
		ThreadPrincipale.imposta(new EsecutoreSwing());

		// Si registra per ascoltare qualsiasi evento venga generato
		new SnifferBusEventi();
		// Sul desktop si esce chiudendo il processo: il motore lo chiede con questo evento, e non lo fa da solo
		BusEventi.iscriviti(NotificaUscitaDalGioco.class, e -> {
			if (e.isErrore()) {
				Logger.log(() -> "Uscita per errore (" + e.getCodice() + "): " + e.getMotivo());
			}
			System.exit(e.getCodice());
		});
		// Si registrano per ricordare gli ultimi messaggi e le ultime notizie e per tenere il conto di quanto succede in gioco
		StatoDelMotore.registraComponenti();

		leggiArgomenti(args);
		GestorePunteggi gestorePunteggi = new GestorePunteggiSuFile();
		// I trofei vinti valgono da una partita all'altra: si rileggono una volta sola, all'avvio
		RegistroTrofei.impostaGestoreTrofei(new GestoreTrofeiSuFile());
		// Come i trofei, il bestiario vale da una partita all'altra
		RegistroBestiario.impostaGestoreBestiario(new GestoreBestiarioSuFile());

		Temporizzatore temporizzatoreAutoma = new TemporizzatoreJ2SE(Main::segnalaImpulsoFallito);
		ControlloreDiGioco controlloreDiGioco = new Automa(temporizzatoreAutoma, new GestoreSalvataggiSuFile(), gestorePunteggi);

		// Ci si iscrive prima di creare la UI: ForestaUI pubblica InternoInterfacciaUtentePronta sul thread principale,
		// e un evento senza iscritti andrebbe perso lasciando il gioco sulla finestra nera
		BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, e -> controlloreDiGioco.inizia());

		// In modalità di prova i dadi si possono truccare dalla finestra di prova (vedi strumenti_di_prova.md)
		if (ModalitaDiProva.isAttiva()) {
			TrucchiDiSviluppo.istanza().installa();
		}

		VistaPartitaMotore vistaPartita = new VistaPartitaMotore();
		Temporizzatore temporizzatoreUI = new TemporizzatoreJ2SE(Main::segnalaImpulsoFallito);
		new ForestaUI(orientamento, tuttoSchermo, saltaLogo, barraDock, gestorePunteggi, vistaPartita,
				temporizzatoreUI);

		if (ModalitaDiProva.isAttiva()) {
			ThreadPrincipale.accoda(() -> new FinestraDiProva(vistaPartita, new StrumentiDiSviluppoMotore()).mostra());
		}

	}
}
