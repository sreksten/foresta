package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

/**
 * Un'immagine ferma che sfuma sul posto in libGDX, subito o dopo un'attesa durante la quale resta intera. Swing, a dissolvenza immediata, la sfoca anche con
 * un filtro 3x3: qui sfuma soltanto.
 */
public final class SpriteInDissolvenzaGdx extends SpriteGdx {

	private static final float DURATA_IN_SECONDI = 1.2f;

	private final TextureRegion regione;
	private final float scala;

	/**
	 * Sfuma da subito, in poco più di un secondo
	 */
	public SpriteInDissolvenzaGdx(ImmaginiGdx immagini, String risorsa, float scala, int x, int y) {
		this(immagini.regione(risorsa), scala, x, y);
	}

	/**
	 * Come sopra, con una regione già pronta (per esempio specchiata)
	 */
	public SpriteInDissolvenzaGdx(TextureRegion regione, float scala, int x, int y) {
		super(new ModelloDelloSprite(DURATA_IN_SECONDI, 0, Dissolvenza.DA_SUBITO, x, y, x, y, 1f, 1f));
		this.regione = regione;
		this.scala = scala;
	}

	/**
	 * Resta intera per <code>attesaInSecondi</code> e poi sfuma in linea retta in <code>dissolvenzaInSecondi</code>
	 */
	public SpriteInDissolvenzaGdx(ImmaginiGdx immagini, String risorsa, float scala, int x, int y, float attesaInSecondi, float dissolvenzaInSecondi) {
		super(new ModelloDelloSprite(attesaInSecondi + dissolvenzaInSecondi, attesaInSecondi, Dissolvenza.LINEARE_CON_SOGLIA, x, y, x, y, 1f, 1f));
		this.regione = immagini.regione(risorsa);
		this.scala = scala;
	}

	@Override
	protected void disegna(Batch batch, float x, float y, float scalaDelloSprite, float alpha) {
		batch.setColor(1f, 1f, 1f, alpha);
		batch.draw(regione, Math.round(x), Math.round(y), regione.getRegionWidth() * scala, regione.getRegionHeight() * scala);
	}
}
