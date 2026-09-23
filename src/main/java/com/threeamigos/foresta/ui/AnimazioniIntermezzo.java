package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.Animazione;

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
