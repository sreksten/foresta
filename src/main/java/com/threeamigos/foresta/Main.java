package com.threeamigos.foresta;

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
		leggiArgomenti(args);
		new SnifferBusEventi();
		GestorePunteggi.impostaGestorePunteggi(new GestorePunteggiSuFile());
		GestoreSalvataggi.impostaGestoreSalvataggi(new GestoreSalvataggiSuFile());
		Gioco.impostaParametri(new TemporizzatoreJ2SE(), new Automa());
		UI.impostaInterfacciaUtente(new ForestaUI(orientamento, tuttoSchermo));
		UI.aspettaInterfacciaUtentePronta();
		Gioco.inizia();
	}
}
