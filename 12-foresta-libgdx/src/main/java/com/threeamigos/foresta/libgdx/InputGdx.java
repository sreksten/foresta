package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.RouterDegliInput;

/**
 * Gli eventi di libGDX (mouse e dito) tradotti per il {@link RouterDegliInput} del modulo comune, quello che instrada gli eventi alle finestre anche
 * in Swing. Le coordinate dello schermo diventano quelle del mondo virtuale (asse y verso il basso, come Swing) passando dal viewport.
 * <p>
 * libGDX non ha gli eventi di click: qui si ricavano. Un click è un tocco rilasciato quasi dove è cominciato (entro {@link #SOGLIA_DI_SPOSTAMENTO}
 * pixel virtuali), e se segue un altro click vicino entro {@link #INTERVALLO_FRA_I_CLICK} millisecondi è il secondo di un doppio click.
 * Con un dito solo (il primo): gli altri si ignorano.
 */
public final class InputGdx extends InputAdapter {

	private static final int SOGLIA_DI_SPOSTAMENTO = 8;
	private static final long INTERVALLO_FRA_I_CLICK = 400;
	private static final int DISTANZA_FRA_I_CLICK = 16;

	private final RouterDegliInput router;
	private final Viewport viewport;
	private final Vector2 temporaneo = new Vector2();

	// Il tocco in corso
	private int xPressione;
	private int yPressione;
	private boolean spostato;
	// L'ultimo click, per riconoscere il doppio
	private long istanteDelClick;
	private int xClick;
	private int yClick;
	private Finestra.Tasto tastoDelClick;
	private int conteggioDelClick;

	public InputGdx(RouterDegliInput router, Viewport viewport) {
		this.router = router;
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
		xPressione = p[0];
		yPressione = p[1];
		spostato = false;
		router.premuto(p[0], p[1], tasto(pulsante));
		return true;
	}

	@Override
	public boolean touchDragged(int schermoX, int schermoY, int dito) {
		if (dito != 0) {
			return false;
		}
		int[] p = mondo(schermoX, schermoY);
		if (Math.abs(p[0] - xPressione) > SOGLIA_DI_SPOSTAMENTO || Math.abs(p[1] - yPressione) > SOGLIA_DI_SPOSTAMENTO) {
			spostato = true;
		}
		router.trascinato(p[0], p[1]);
		return true;
	}

	@Override
	public boolean touchUp(int schermoX, int schermoY, int dito, int pulsante) {
		if (dito != 0) {
			return false;
		}
		int[] p = mondo(schermoX, schermoY);
		Finestra.Tasto tasto = tasto(pulsante);
		router.rilasciato(p[0], p[1], tasto);
		if (!spostato) {
			long ora = System.currentTimeMillis();
			boolean secondo = conteggioDelClick == 1 && tasto == tastoDelClick && ora - istanteDelClick <= INTERVALLO_FRA_I_CLICK
					&& Math.abs(p[0] - xClick) <= DISTANZA_FRA_I_CLICK && Math.abs(p[1] - yClick) <= DISTANZA_FRA_I_CLICK;
			conteggioDelClick = secondo ? 2 : 1;
			istanteDelClick = ora;
			xClick = p[0];
			yClick = p[1];
			tastoDelClick = tasto;
			router.clic(p[0], p[1], tasto, conteggioDelClick);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int schermoX, int schermoY) {
		int[] p = mondo(schermoX, schermoY);
		router.mosso(p[0], p[1]);
		return true;
	}

	@Override
	public boolean scrolled(float quantoX, float quantoY) {
		if (quantoY == 0) {
			return false;
		}
		int[] p = mondo(Gdx.input.getX(), Gdx.input.getY());
		router.rotella(p[0], p[1], (int) Math.signum(quantoY) * Math.max(1, Math.round(Math.abs(quantoY))));
		return true;
	}
}
