package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

public class Gioco {
	
	private Gioco() {
	}

	private static Temporizzatore temporizzatore;
	private static ControlloreDiGioco motore;

	public static void impostaParametri(Temporizzatore t, ControlloreDiGioco c) {
		temporizzatore = t;
		motore = c;
		motore.setTemporizzatore(temporizzatore);
	}

	public static void inizia() {
		motore.inizia();
	}

	public static void riceviTesto(String s) {
		motore.riceviTesto(s);
	}

	public static void processaAzione(Comando azione) {
		motore.processaAzione(azione);
	}
}
