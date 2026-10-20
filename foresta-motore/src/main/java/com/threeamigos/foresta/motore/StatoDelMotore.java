package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.trofei.RegistroTrofei;

/**
 * Il punto solo da cui si prepara e si azzera lo stato statico del motore, che vive per tutto il processo: il modello
 * dati, i due gruppi, lo stato del combattimento e i componenti che ascoltano il bus.
 * <p>
 * Chi avvia il gioco (il {@code Main} del desktop, l'attività Android, l'applicazione iOS) chiama
 * {@link #registraComponenti()} una volta sola, dopo aver installato il thread principale. Una partita nuova o caricata
 * non passa da qui: la prepara l'{@link Automa} ({@code Foresta.reimposta} per una nuova, un {@code GestoreSalvataggi}
 * e {@link RiletturaPartita#ricostruisci()} per una caricata). {@link #azzera()} riporta tutto a uno stato vuoto, come
 * a processo appena avviato: serve ai test e a chi, su una piattaforma che può chiudere l'app in background, vuole
 * ripartire pulito prima di riprendere la partita interrotta ({@code ControlloreDiGioco.riprendiPartitaInterrotta}).
 */
public final class StatoDelMotore {

	// L'automa in funzione: ne vale uno solo, e quando se ne crea un altro (o si azzera) il vecchio si chiude
	private static Automa automaAttivo;

	private StatoDelMotore() {
	}

	/**
	 * Un automa nuovo è nato: se ce n'era uno, lo si chiude (smette di ascoltare il bus), così i comandi del giocatore non li gestiscono
	 * due automi, uno dei quali con i gruppi della partita di prima.
	 */
	static void automaCreato(Automa nuovo) {
		if (automaAttivo != null && automaAttivo != nuovo) {
			automaAttivo.chiudi();
		}
		automaAttivo = nuovo;
	}

	/**
	 * Fa iscrivere al bus i componenti che tengono il conto di ciò che succede in gioco: gli ultimi messaggi e notizie,
	 * le statistiche, i trofei, gli artefatti e le missioni. Dopo un {@code BusEventi.azzera()} va richiamato.
	 */
	public static void registraComponenti() {
		Notizie.registrati();
		Statistiche.registrati();
		RegistroTrofei.registrati();
		RegistroArtefatti.registrati();
		RegistroMissioni.registrati();
	}

	/**
	 * Azzera lo stato statico: un modello dati vuoto, nessun gruppo, nessun combattimento. Non tocca il bus, i dadi
	 * né i trofei, che passano da una partita all'altra.
	 * <p>
	 * L'{@link Automa} in funzione si chiude: tiene i gruppi della partita di prima nei suoi campi, e dopo l'azzeramento
	 * non è più utilizzabile. Per ripartire se ne crea uno nuovo (e si richiama {@link #registraComponenti()} se si è
	 * azzerato anche il bus). Il vecchio gruppo del giocatore smette di ascoltare il bus.
	 */
	public static void azzera() {
		if (automaAttivo != null) {
			automaAttivo.chiudi();
			automaAttivo = null;
		}
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		GruppoAvversario.azzeraIstanza();
		StatoDelCombattimento.azzeraIstanza();
	}
}
