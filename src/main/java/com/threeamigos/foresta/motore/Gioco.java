package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.Temporizzatore;

public class Gioco {
	
	private Gioco() {
	}

    private static ControlloreDiGioco controlloreDiGioco;

	public static void impostaParametri(Temporizzatore temporizzatore, ControlloreDiGioco controllore) {
        controlloreDiGioco = controllore;
		controlloreDiGioco.setTemporizzatore(temporizzatore);
	}

	public static void inizia() {
		controlloreDiGioco.inizia();
	}

	public static void processaAzione(Comando azione) {
		controlloreDiGioco.processaAzione(azione);
	}
}
