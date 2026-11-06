package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Lo sprite a tempo in libGDX: l'icona (il nome di una risorsa) con a fianco la variazione scritta col contorno nero, che sale e sfuma. Dove sta ogni parte
 * lo dice {@link DescrizioneSpriteATempo}, nel modulo comune.
 */
public final class SpriteATempoGdx extends SpriteGdx {

	private final DescrizioneSpriteATempo dati;
	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	/**
	 * @param dati           che cosa mostra, con l'icona come nome di risorsa
	 * @param testo          il testo, nel font della variazione
	 */
	public SpriteATempoGdx(DescrizioneSpriteATempo dati, ImmaginiGdx immagini, TestoGdx testo) {
		super(new ModelloDelloSprite(DescrizioneSpriteATempo.DURATA_IN_SECONDI, DescrizioneSpriteATempo.ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				Dissolvenza.IPERBOLICA_CON_SOGLIA, dati.getXIniziale(), dati.getYIniziale(),
				dati.getXIniziale(), dati.getYIniziale() - DescrizioneSpriteATempo.DISTANZA_TOTALE_MOVIMENTO, 1f, 1f));
		this.dati = dati;
		this.immagini = immagini;
		this.testo = testo;
		setRitaglio(dati.getRitaglio());
	}

	@Override
	protected void disegna(Batch batch, float x, float y, float scala, float alpha) {
		int xi = Math.round(x);
		int yi = Math.round(y);
		if (dati.getIcona() != null) {
			batch.setColor(1f, 1f, 1f, alpha);
			immagini.disegna(batch, (String) dati.getIcona(), xi, yi + dati.getYIcona(), dati.getScalaDellIcona());
		}
		if (dati.isSoloImmagine()) {
			return;
		}
		Color nero = TestoGdx.coloreConAlfa(DoomdarkColorModel.Color.BLACK, alpha);
		for (int[] scostamento : DescrizioneSpriteATempo.getContorno()) {
			testo.disegna(batch, dati.getTestoVariazione(), xi + dati.getXTesto() + scostamento[0], yi + dati.getYTesto() + scostamento[1], nero, 1f);
		}
		testo.disegna(batch, dati.getTestoVariazione(), xi + dati.getXTesto() + 1, yi + dati.getYTesto() + 1,
				TestoGdx.coloreConAlfa(dati.getColore(), alpha), 1f);
	}
}
