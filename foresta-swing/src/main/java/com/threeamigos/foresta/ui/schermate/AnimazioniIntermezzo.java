package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.intermezzi.Animazione;
import com.threeamigos.foresta.ui.sprite.AnimazioneFuocoDaCampo;
import com.threeamigos.foresta.ui.sprite.AnimazioneImmagine;

/**
 * Associa a ogni {@link Animazione} nominata dal motore la classe della UI che la disegna.
 */
final class AnimazioniIntermezzo {

	private AnimazioniIntermezzo() {
	}

	static AnimazioneImmagine crea(Animazione animazione) {
		switch (animazione) {
			case FUOCO_DA_CAMPO:
				return new AnimazioneFuocoDaCampo();
			default:
				throw new IllegalArgumentException("Animazione senza una classe che la disegni: " + animazione);
		}
	}
}
