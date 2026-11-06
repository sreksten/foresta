package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Il fumetto in libGDX: un rettangolo bianco smussato con il testo e una punta che indica chi parla, che cresce verso l'alto da un punto e svanisce dopo
 * qualche secondo. Com'è fatta la nuvola e dov'è la punta lo dice {@link DescrizioneFumetto}, nel modulo comune.
 */
public final class SpriteFumettoGdx extends SpriteGdx {

	private final DescrizioneFumetto dati;
	private final DoomdarkColorModel.Color colore;
	private final int puntaVersoX;
	private final int puntaVersoY;
	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	/**
	 * @param xIniziale   la coordinata X dell'angolo in basso a sinistra del fumetto
	 * @param yIniziale   la coordinata Y dell'angolo in basso a sinistra; il fumetto cresce verso l'alto e non sfora il margine superiore dello schermo
	 * @param puntaVersoX dove deve andare la punta
	 */
	public SpriteFumettoGdx(DescrizioneFumetto dati, DoomdarkColorModel.Color colore, int xIniziale, int yIniziale, int puntaVersoX, int puntaVersoY,
							ImmaginiGdx immagini, TestoGdx testo) {
		super(new ModelloDelloSprite(DescrizioneFumetto.DURATA_IN_SECONDI, DescrizioneFumetto.ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				Dissolvenza.IPERBOLICA_CON_SOGLIA, xIniziale, Math.max(yIniziale, dati.getAltezza() + Finestra.SPACING),
				xIniziale, Math.max(yIniziale, dati.getAltezza() + Finestra.SPACING), 1f, 1f));
		this.dati = dati;
		this.colore = colore;
		this.puntaVersoX = puntaVersoX;
		this.puntaVersoY = puntaVersoY;
		this.immagini = immagini;
		this.testo = testo;
	}

	/**
	 * Fa ripartire da zero il tempo, per esempio quando un fumetto resta in vita perché ne arriva uno uguale
	 */
	public void resettaImpulsi() {
		getModello().resettaTempoTrascorso();
	}

	public String getTesto() {
		return dati.getTesto();
	}

	@Override
	protected void disegna(Batch batch, float x, float y, float scala, float alpha) {
		NuvolaDiFumettoGdx.disegna(batch, immagini, testo, dati, colore, Math.round(x), Math.round(y), puntaVersoX, puntaVersoY, alpha);
	}
}
