package com.threeamigos.foresta.tutorial;

/**
 * La memoria del tutorial: per ora ricorda soltanto se è acceso. Non sa niente della UI e non manda eventi: chi vuole sapere se un
 * suggerimento va mostrato, o se la partita va pilotata per il tutorial (vedi {@code tutorial.md}), lo chiede qui.
 * <p>
 * Il tutorial vale da una partita all'altra, come i trofei e il bestiario, e non fa parte del salvataggio. Finché il tutorial non è finito
 * non si salva niente: parte sempre acceso.
 */
public final class RegistroSuggerimenti {

	private static volatile boolean attivo = true;

	private RegistroSuggerimenti() {
	}

	/**
	 * Se il tutorial è acceso.
	 */
	public static boolean isAttivo() {
		return attivo;
	}

	/**
	 * Accende o spegne il tutorial.
	 */
	public static void setAttivo(boolean attivo) {
		RegistroSuggerimenti.attivo = attivo;
	}
}
