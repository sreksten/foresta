package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

public class Gioco {
	
	private Gioco() {
	}

	private static Temporizzatore temporizzatore;
	private static ControlloreDiGioco motore;
	
	public static final void impostaTemporizzatore(Temporizzatore t) {
		temporizzatore = t;
		if (motore != null) {
			motore.setTemporizzatore(t);
		}
	}
	
	public static final void impostaControlloreDiGioco(ControlloreDiGioco c) {
		motore = c;
		if (temporizzatore != null) {
			motore.setTemporizzatore(temporizzatore);
		}
	}

	public static final void inizia() {
		motore.inizia();
	}

	public static final void riceviTesto(String s) {
		motore.riceviTesto(s);
	}

	public static final void processaAzione(Comando azione) {
		motore.processaAzione(azione);
	}
}
