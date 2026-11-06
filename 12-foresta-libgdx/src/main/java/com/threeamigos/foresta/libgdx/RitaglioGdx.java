package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Il ritaglio rettangolare del disegno (quello che in Swing è <code>setClip</code> o i limiti del raster): quel che sta fuori dal rettangolo,
 * in coordinate del mondo come tutto il resto (asse y verso il basso), non si vede. Si usa a coppie: {@link #entra} e, finito, {@link #esci}.
 */
public final class RitaglioGdx {

	private RitaglioGdx() {
	}

	/**
	 * Limita il disegno al rettangolo; restituisce false se il rettangolo non si vede (allora non si deve chiamare {@link #esci})
	 */
	public static boolean entra(Batch batch, Camera camera, Viewport viewport, float x, float y, float larghezza, float altezza) {
		Rectangle area = new Rectangle(x, y, larghezza, altezza);
		Rectangle scissor = new Rectangle();
		batch.flush();
		ScissorStack.calculateScissors(camera, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(),
				viewport.getScreenHeight(), batch.getTransformMatrix(), area, scissor);
		return ScissorStack.pushScissors(scissor);
	}

	public static void esci(Batch batch) {
		batch.flush();
		ScissorStack.popScissors();
	}
}
