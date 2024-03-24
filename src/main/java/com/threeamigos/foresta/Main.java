package com.threeamigos.foresta;

import com.threeamigos.foresta.motore.Automa;
import com.threeamigos.foresta.motore.Gioco;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tools.GestorePunteggi;
import com.threeamigos.foresta.tools.GestorePunteggiSuFile;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.GestoreSalvataggiSuFile;
import com.threeamigos.foresta.tools.TemporizzatoreJ2SE;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.Orientamento;
import com.threeamigos.foresta.ui.UI;

public class Main {

	private static Orientamento orientamento = Orientamento.VERTICALE;
	private static boolean tuttoSchermo = false;

	private static void leggiArgomenti(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("ORIZZONTALE")) {
				orientamento = Orientamento.ORIZZONTALE;
			} else if (args[i].equalsIgnoreCase("TUTTOSCHERMO")) {
				tuttoSchermo = true;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		leggiArgomenti(args);
		ProduttoreDiTestiCasuale.fiaba();
		ProduttoreDiTestiCasuale.oroscopo();
		GestorePunteggi.impostaGestorePunteggi(new GestorePunteggiSuFile());
		GestoreSalvataggi.impostaGestoreSalvataggi(new GestoreSalvataggiSuFile());
		Gioco.impostaTemporizzatore(new TemporizzatoreJ2SE());
		Gioco.impostaControlloreDiGioco(new Automa());
		UI.impostaInterfacciaUtente(new ForestaUI(orientamento, tuttoSchermo));
		while (!UI.isInterfacciaUtentePronta()) {
			Thread.sleep(1000);
		}
		Gioco.inizia();
	}
}
