package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Il tocco sul pollice e sull'annulla del fumetto del tutorial, prima di tutto il resto: un tocco che comincia su un pulsante non arriva allo schermo, e
 * quando finisce lì (e non è stato trascinato altrove) il pulsante si preme. Gli altri tocchi passano, perché l'elemento che lampeggia risponde ai clic.
 */
final class InputDelTutorialGdx extends InputAdapter {

	private final TutorialGdx tutorial;
	private final Viewport viewport;
	private final Vector2 temporaneo = new Vector2();
	private boolean cominciatoSuUnPulsante;

	InputDelTutorialGdx(TutorialGdx tutorial, Viewport viewport) {
		this.tutorial = tutorial;
		this.viewport = viewport;
	}

	@Override
	public boolean touchDown(int schermoX, int schermoY, int dito, int pulsante) {
		if (dito != 0) {
			return false;
		}
		viewport.unproject(temporaneo.set(schermoX, schermoY));
		cominciatoSuUnPulsante = tutorial.suiPulsanti((int) temporaneo.x, (int) temporaneo.y);
		return cominciatoSuUnPulsante;
	}

	@Override
	public boolean touchDragged(int schermoX, int schermoY, int dito) {
		return dito == 0 && cominciatoSuUnPulsante;
	}

	@Override
	public boolean touchUp(int schermoX, int schermoY, int dito, int pulsante) {
		if (dito != 0 || !cominciatoSuUnPulsante) {
			return false;
		}
		cominciatoSuUnPulsante = false;
		viewport.unproject(temporaneo.set(schermoX, schermoY));
		tutorial.tocco((int) temporaneo.x, (int) temporaneo.y);
		return true;
	}
}
