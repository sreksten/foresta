package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Pianificatore;
import com.threeamigos.foresta.ui.RiconoscitoreDiGesti;
import com.threeamigos.foresta.ui.RouterDegliInput;

/**
 * Gli eventi di libGDX (mouse e dito) tradotti per il {@link RouterDegliInput} del modulo comune, quello che instrada gli eventi alle finestre anche
 * in Swing. Le coordinate dello schermo diventano quelle del mondo virtuale (asse y verso il basso, come Swing) passando dal viewport.
 * <p>
 * I gesti (il click, che libGDX non ha, il doppio click, e col dito la pressione prolungata) li riconosce {@link RiconoscitoreDiGesti}, nel modulo
 * comune e uguale per ogni toolkit. Con un dito solo (il primo): gli altri si ignorano.
 */
public final class InputGdx extends InputAdapter {

	private final RiconoscitoreDiGesti gesti;
	private final Viewport viewport;
	private final Vector2 temporaneo = new Vector2();

	/**
	 * @param tocco vero per un telefono o per provare i gesti del dito con il mouse
	 */
	public InputGdx(RouterDegliInput router, Pianificatore pianificatore, Viewport viewport, boolean tocco) {
		this.gesti = new RiconoscitoreDiGesti(router, pianificatore, tocco);
		this.viewport = viewport;
	}

	private int[] mondo(int schermoX, int schermoY) {
		viewport.unproject(temporaneo.set(schermoX, schermoY));
		return new int[]{(int) temporaneo.x, (int) temporaneo.y};
	}

	private static Finestra.Tasto tasto(int pulsante) {
		switch (pulsante) {
			case Input.Buttons.RIGHT:
				return Finestra.Tasto.DESTRO;
			case Input.Buttons.MIDDLE:
				return Finestra.Tasto.CENTRALE;
			default:
				return Finestra.Tasto.SINISTRO;
		}
	}

	@Override
	public boolean touchDown(int schermoX, int schermoY, int dito, int pulsante) {
		if (dito != 0) {
			return false;
		}
		int[] p = mondo(schermoX, schermoY);
		gesti.premuto(p[0], p[1], tasto(pulsante));
		return true;
	}

	@Override
	public boolean touchDragged(int schermoX, int schermoY, int dito) {
		if (dito != 0) {
			return false;
		}
		int[] p = mondo(schermoX, schermoY);
		gesti.trascinato(p[0], p[1]);
		return true;
	}

	@Override
	public boolean touchUp(int schermoX, int schermoY, int dito, int pulsante) {
		if (dito != 0) {
			return false;
		}
		int[] p = mondo(schermoX, schermoY);
		gesti.rilasciato(p[0], p[1], tasto(pulsante));
		return true;
	}

	@Override
	public boolean mouseMoved(int schermoX, int schermoY) {
		int[] p = mondo(schermoX, schermoY);
		gesti.mosso(p[0], p[1]);
		return true;
	}

	@Override
	public boolean scrolled(float quantoX, float quantoY) {
		if (quantoY == 0) {
			return false;
		}
		int[] p = mondo(Gdx.input.getX(), Gdx.input.getY());
		gesti.rotella(p[0], p[1], (int) Math.signum(quantoY) * Math.max(1, Math.round(Math.abs(quantoY))));
		return true;
	}
}
