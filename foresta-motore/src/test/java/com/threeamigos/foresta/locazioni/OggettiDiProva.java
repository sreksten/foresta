package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.oggetti.Oggetto;

/**
 * Per i test di altri package: mette nella locazione l'oggetto che il gruppo raccoglierà a
 * fine locazione, al posto di quello scelto a caso.
 */
public final class OggettiDiProva {

	private OggettiDiProva() {
	}

	public static void impostaOggetto(Locazione locazione, Oggetto oggetto) {
		((LocazioneBase) locazione).setOggetto(oggetto);
	}
}
