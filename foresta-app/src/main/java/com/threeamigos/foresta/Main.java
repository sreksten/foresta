package com.threeamigos.foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.interfacce.ControlloreDiGioco;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.motore.Automa;
import com.threeamigos.foresta.motore.Notizie;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.strumenti.*;
import com.threeamigos.foresta.ui.EsecutoreSwing;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Orientamento;
import com.threeamigos.foresta.motore.ModalitaDiProva;
import com.threeamigos.foresta.motore.StrumentiDiSviluppoMotore;
import com.threeamigos.foresta.ui.sviluppo.FinestraDiProva;
import com.threeamigos.foresta.salvataggi.GestorePunteggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreSalvataggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreTrofeiSuFile;

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
	 * Imposta le opzioni di avvio dagli argomenti; quelli sconosciuti sono ignorati.
	 *
	 * @throws NumberFormatException se il valore di {@code ZOOM=} non è un numero
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
            } else if (arg.toUpperCase().startsWith("ZOOM=")) {
                LivelloDiZoom.imposta(Double.parseDouble(arg.substring("ZOOM=".length())));
            } else if (arg.equalsIgnoreCase("MODALITA_DI_PROVA")) {
                ModalitaDiProva.setAttiva(true);
            }
        }
	}

	/**
	 * Avvia il gioco: il resto del lavoro lo fanno gli eventi, perché la finestra, quando è pronta, avvisa l'automa che può cominciare.
	 */
	public static void main(String[] args) throws Exception {

		// Tutta la logica di gioco gira sull'Event Dispatch Thread di Swing
		ThreadPrincipale.imposta(new EsecutoreSwing());

		// Si registra per ascoltare qualsiasi evento venga generato
		new SnifferBusEventi();
		// Si registra per ricordare gli ultimi messaggi e le ultime notizie
		Notizie.registrati();
		// Si registrano per tenere il conto di quanto succede in gioco
		Statistiche.registrati();
		RegistroTrofei.registrati();
		RegistroArtefatti.registrati();
		RegistroMissioni.registrati();

		leggiArgomenti(args);
		GestorePunteggi gestorePunteggi = new GestorePunteggiSuFile();
		// I trofei vinti valgono da una partita all'altra: si rileggono una volta sola, all'avvio
		RegistroTrofei.impostaGestoreTrofei(new GestoreTrofeiSuFile());

		Temporizzatore temporizzatoreAutoma = new TemporizzatoreJ2SE();
		ControlloreDiGioco controlloreDiGioco = new Automa(temporizzatoreAutoma, new GestoreSalvataggiSuFile(), gestorePunteggi);

		// Ci si iscrive prima di creare la UI: ForestaUI pubblica InternoInterfacciaUtentePronta sul thread principale,
		// e un evento senza iscritti andrebbe perso lasciando il gioco sulla finestra nera
		BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, e -> controlloreDiGioco.inizia());

		// In modalità di prova i dadi si possono truccare dalla finestra di prova (vedi strumenti_di_prova.md)
		if (ModalitaDiProva.isAttiva()) {
			TrucchiDiSviluppo.istanza().installa();
		}

		VistaPartitaMotore vistaPartita = new VistaPartitaMotore();
		Temporizzatore temporizzatoreUI = new TemporizzatoreJ2SE();
		new ForestaUI(orientamento, tuttoSchermo, saltaLogo, barraDock, gestorePunteggi, vistaPartita,
				temporizzatoreUI);

		if (ModalitaDiProva.isAttiva()) {
			ThreadPrincipale.accoda(() -> new FinestraDiProva(vistaPartita, new StrumentiDiSviluppoMotore()).mostra());
		}

	}
}
