package com.threeamigos.foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.SnifferBusEventi;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.motore.Automa;
import com.threeamigos.foresta.motore.ControlloreDiGioco;
import com.threeamigos.foresta.motore.Notizie;
import com.threeamigos.foresta.tools.*;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.Orientamento;

public class Main {

	private static Orientamento orientamento = Orientamento.ORIZZONTALE;
	private static boolean tuttoSchermo = false;

	private static void leggiArgomenti(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("ORIZZONTALE")) {
				orientamento = Orientamento.ORIZZONTALE;
			} else if (arg.equalsIgnoreCase("VERTICALE")) {
				orientamento = Orientamento.VERTICALE;
            } else if (arg.equalsIgnoreCase("TUTTOSCHERMO")) {
                tuttoSchermo = true;
            }
        }
	}

	public static void main(String[] args) throws Exception {

		// Si registra per ascoltare qualsiasi evento venga generato
		new SnifferBusEventi();
		// Si registra per ricordare gli ultimi messaggi e le ultime notizie
		Notizie.registrati();

		leggiArgomenti(args);
		GestorePunteggi.impostaGestorePunteggi(new GestorePunteggiSuFile());
		GestoreSalvataggi.impostaGestoreSalvataggi(new GestoreSalvataggiSuFile());

		Temporizzatore temporizzatoreAutoma = new TemporizzatoreJ2SE();
		ControlloreDiGioco controlloreDiGioco = new Automa(temporizzatoreAutoma);

		Temporizzatore temporizzatoreUI = new TemporizzatoreJ2SE();
		new ForestaUI(orientamento, tuttoSchermo, temporizzatoreUI);

		//FIXME gestire l'elenco finestre togliendolo da InterfacciaUtente

		BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, e -> controlloreDiGioco.inizia());
	}
}
