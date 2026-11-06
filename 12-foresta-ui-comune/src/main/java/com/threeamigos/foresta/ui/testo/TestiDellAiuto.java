package com.threeamigos.foresta.ui.testo;

/**
 * I testi di aiuto che compaiono nei cartigli accanto al mouse (ad aiuto acceso) e che più schermate hanno in comune
 */
public final class TestiDellAiuto {

	private TestiDellAiuto() {
	}

	/**
	 * Il testo dell'aiuto per gli elenchi che si scorrono con la rotella
	 */
	public static String rotella() {
		return TestiDellaUI.get("AIUTO_ROTELLA");
	}

	/**
	 * Il testo dell'aiuto per il click su una voce che apre e chiude qualcosa (un elenco, una descrizione)
	 */
	public static String click(boolean aperto, String cosaSiApre) {
		return TestiDellaUI.get(aperto ? "AIUTO_CLICK_CHIUDI" : "AIUTO_CLICK_APRI", cosaSiApre);
	}
}
