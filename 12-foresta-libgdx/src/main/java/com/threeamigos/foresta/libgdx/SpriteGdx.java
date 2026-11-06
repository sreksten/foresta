package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.sprite.SpriteDelloSchermo;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;

/**
 * La base degli sprite in libGDX: il tempo, il movimento e la dissolvenza sono di {@link ModelloDelloSprite} (nel modulo comune, gli stessi di Swing), qui c'è
 * il disegno. A ogni fotogramma chi tiene lo sprite chiama {@link #anima}: lo sprite si disegna dove il modello dice, con la sua opacità e dentro il suo
 * ritaglio, e fa avanzare il tempo; finita la durata non è più in vita ({@link #isAttivo()}) e chi lo tiene lo toglie.
 * <p>
 * A differenza di Swing, che compone lo sprite in un'immagine e la sfuma intera, qui le parti si disegnano una per volta con la stessa opacità: dove due
 * parti si sovrappongono (il contorno nero di una variazione, il testo sulla nuvola di un fumetto) durante la dissolvenza si vede un po' di più di quel che
 * sta sotto.
 */
public abstract class SpriteGdx implements SpriteDelloSchermo {

	private final ModelloDelloSprite modello;

	protected SpriteGdx(ModelloDelloSprite modello) {
		this.modello = modello;
	}

	protected ModelloDelloSprite getModello() {
		return modello;
	}

	public final boolean isAttivo() {
		return modello.isAttivo();
	}

	/**
	 * Limita il disegno a quest'area, in coordinate dello schermo (null: nessun limite)
	 */
	public final void setRitaglio(Area ritaglio) {
		modello.setRitaglio(ritaglio);
	}

	/**
	 * Disegna lo sprite per l'istante corrente (se è ancora in vita) e fa avanzare il suo tempo
	 */
	public final void anima(Batch batch, Camera camera, Viewport viewport) {
		if (!modello.prepara()) {
			return;
		}
		Area ritaglio = modello.getRitaglio();
		boolean ritagliato = ritaglio != null
				&& RitaglioGdx.entra(batch, camera, viewport, ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza());
		Color precedente = new Color(batch.getColor());
		try {
			disegna(batch, modello.getX(), modello.getY(), modello.getScala(), modello.getAlpha());
		} finally {
			batch.setColor(precedente);
			if (ritagliato) {
				RitaglioGdx.esci(batch);
			}
		}
		modello.aggiornaConOrologio(System.nanoTime());
	}

	/**
	 * Disegna lo sprite in quella posizione, a quella scala e con quell'opacità (da 0 a 1)
	 */
	protected abstract void disegna(Batch batch, float x, float y, float scala, float alpha);
}
