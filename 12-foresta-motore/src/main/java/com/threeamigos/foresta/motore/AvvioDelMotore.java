package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.eventi.notifiche.NotificaUscitaDalGioco;
import com.threeamigos.foresta.interfacce.ControlloreDiGioco;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.salvataggi.GestoreBestiarioSuFile;
import com.threeamigos.foresta.salvataggi.GestorePunteggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreSalvataggiSuFile;
import com.threeamigos.foresta.salvataggi.GestoreTrofeiSuFile;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tutorial.PilotaggioDelTutorial;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.strumenti.temporizzatore.TemporizzatoreJ2SE;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;
import com.threeamigos.foresta.trofei.RegistroTrofei;

import java.util.function.Consumer;

/**
 * Il collegamento del motore, uguale per ogni piattaforma (il <code>Main</code> di Swing, quello di libGDX, poi Android e iOS): il
 * modello dati, il thread principale, chi chiude l'applicazione, i componenti del motore che ascoltano il bus, i gestori su file di
 * punteggi, trofei, bestiario e salvataggi, l'automa (che parte quando l'interfaccia pubblica {@code InternoInterfacciaUtentePronta})
 * e, in modalità di prova, i trucchi sui dadi. Le piattaforme ci aggiungono l'interfaccia.
 */
public final class AvvioDelMotore {

	private final GestorePunteggi gestorePunteggi;
	private final Automa automa;
	private final VistaPartitaMotore vistaPartita;

	/**
	 * @param esecutore      il thread principale della piattaforma, su cui gira tutta la logica di gioco
	 * @param uscita         che cosa fa la piattaforma quando il motore chiede di uscire (sul desktop chiude il processo): il motore
	 *                       non lo fa da solo; il motivo di un'uscita per errore è già nel log
	 * @param linguaDecisa   vero se la lingua è già decisa (dalla riga di comando): una partita nuova non la chiede
	 */
	public AvvioDelMotore(ThreadPrincipale.Esecutore esecutore, Consumer<NotificaUscitaDalGioco> uscita, boolean linguaDecisa) {
		// Le parti del gioco ricevono i loro dati dal modello dati: prima di usarne una va detto loro dove sono
		ModelloDati.installa();
		ThreadPrincipale.imposta(esecutore);
		BusEventi.iscriviti(NotificaUscitaDalGioco.class, e -> {
			if (e.isErrore()) {
				Logger.log(() -> "Uscita per errore (" + e.getCodice() + "): " + e.getMotivo());
			}
			uscita.accept(e);
		});
		// Si registrano per ricordare gli ultimi messaggi e le ultime notizie e per tenere il conto di quanto succede in gioco
		StatoDelMotore.registraComponenti();

		gestorePunteggi = new GestorePunteggiSuFile();
		// Trofei e bestiario valgono da una partita all'altra: si rileggono una volta sola, all'avvio
		RegistroTrofei.impostaGestoreTrofei(new GestoreTrofeiSuFile());
		RegistroBestiario.impostaGestoreBestiario(new GestoreBestiarioSuFile());

		automa = new Automa(nuovoTemporizzatore(), new GestoreSalvataggiSuFile(), gestorePunteggi);
		if (!linguaDecisa) {
			automa.offriLaSceltaDellaLingua();
		}
		ControlloreDiGioco controlloreDiGioco = automa;
		// Ci si iscrive prima di creare l'interfaccia: un evento senza iscritti andrebbe perso lasciando il gioco sulla finestra nera
		BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, e -> controlloreDiGioco.inizia());

		// Il tutorial pilota i primi turni truccando i dadi; in modalità di prova i dadi si possono truccare anche dalla finestra di prova
		// (vedi strumenti_di_prova.md), e i suoi trucchi hanno la precedenza
		Dado.impostaTruccatore(new PilotaggioDelTutorial(ModalitaDiProva.isAttiva() ? TrucchiDiSviluppo.istanza() : null));
		vistaPartita = new VistaPartitaMotore();
	}

	/**
	 * Un temporizzatore il cui impulso fallito si dice a tutti sul bus (la UI lo mostra al giocatore): quello dell'automa e quello
	 * della UI.
	 */
	public static Temporizzatore nuovoTemporizzatore() {
		return new TemporizzatoreJ2SE(AvvioDelMotore::segnalaImpulsoFallito);
	}

	private static void segnalaImpulsoFallito(String messaggio, RuntimeException errore) {
		BusEventi.pubblica(messaggio == null ? new InternoException(errore) : new InternoException(messaggio, errore));
	}

	/** La classifica, la stessa che riceve l'automa: l'interfaccia la mostra nell'intro */
	public GestorePunteggi getGestorePunteggi() {
		return gestorePunteggi;
	}

	/** L'automa, che l'interfaccia avvisa quando l'applicazione va in background e torna in primo piano */
	public ControlloreDiGioco getControlloreDiGioco() {
		return automa;
	}

	/** Lo stato della partita, in sola lettura, per l'interfaccia */
	public VistaPartitaMotore getVistaPartita() {
		return vistaPartita;
	}
}
