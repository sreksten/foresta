package com.threeamigos.foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoInterfacciaUtentePronta;
import com.threeamigos.foresta.eventi.SnifferBusEventi;
import com.threeamigos.foresta.motore.Automa;
import com.threeamigos.foresta.motore.Gioco;
import com.threeamigos.foresta.tools.*;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.Orientamento;
import com.threeamigos.foresta.ui.UI;

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

		leggiArgomenti(args);
		GestorePunteggi.impostaGestorePunteggi(new GestorePunteggiSuFile());
		GestoreSalvataggi.impostaGestoreSalvataggi(new GestoreSalvataggiSuFile());
		Temporizzatore temporizzatore = new TemporizzatoreJ2SE();
		Gioco.impostaParametri(temporizzatore, new Automa());
		ForestaUI forestaUI = new ForestaUI(orientamento, tuttoSchermo, temporizzatore);

		// FIXME l'ultima cosa che andrà levata quando si passa il tutto a eventi lasciando solo il costruttore
		UI.impostaInterfacciaUtente(forestaUI);

		BusEventi.iscriviti(EventoInterfacciaUtentePronta.class, e -> Gioco.inizia());
	}
}
