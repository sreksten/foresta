package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Lo sprite di effetto in libGDX: un testo che si ingrandisce sul posto mentre sfuma, con l'ombra scura attorno. Che cosa mostra lo dice
 * {@link DescrizioneSpriteEffetto}, nel modulo comune.
 */
public final class SpriteEffettoGdx extends SpriteGdx {

	private final DescrizioneSpriteEffetto dati;
	private final TestoGdx testo;

	/**
	 * @param testo il testo, nel font della descrizione
	 */
	public SpriteEffettoGdx(DescrizioneSpriteEffetto dati, TestoGdx testo) {
		super(new ModelloDelloSprite(DescrizioneSpriteEffetto.DURATA_IN_SECONDI, DescrizioneSpriteEffetto.ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				Dissolvenza.LINEARE_CON_SOGLIA, dati.getX(), dati.getY(), dati.getX(), dati.getY(),
				DescrizioneSpriteEffetto.SCALA_INIZIALE, DescrizioneSpriteEffetto.SCALA_FINALE));
		this.dati = dati;
		this.testo = testo;
	}

	private int larghezzaConOmbra() {
		return dati.getLarghezzaDelTesto() + 2 * DescrizioneSpriteEffetto.OMBRA_SCOSTAMENTO;
	}

	private int altezzaConOmbra() {
		return dati.getAltezzaDelTesto() + 2 * DescrizioneSpriteEffetto.OMBRA_SCOSTAMENTO;
	}

	@Override
	protected void disegna(Batch batch, float x, float y, float scala, float alpha) {
		int ombra = DescrizioneSpriteEffetto.OMBRA_SCOSTAMENTO;
		int larghezzaScalata = Math.round(larghezzaConOmbra() * scala);
		int altezzaScalata = Math.round(altezzaConOmbra() * scala);
		float sinistra = Math.round(x) - (larghezzaScalata >> 1);
		float alto = Math.round(y) - (altezzaScalata >> 1);
		float testoX = sinistra + ombra * scala;
		float testoY = alto + ombra * scala;

		float alfaOmbra = alpha * (DescrizioneSpriteEffetto.OMBRA_ARGB >>> 24) / 255f;
		Color coloreOmbra = TestoGdx.coloreConAlfa(DoomdarkColorModel.Color.BLACK, alfaOmbra);
		float scostamento = ombra * scala;
		float[] versi = {-scostamento, 0, scostamento};
		for (float dx : versi) {
			for (float dy : versi) {
				if (dx != 0 || dy != 0) {
					testo.disegna(batch, dati.getTesto(), testoX + dx, testoY + dy, coloreOmbra, scala);
				}
			}
		}
		Color colore;
		if (dati.getColore() != null) {
			colore = TestoGdx.coloreConAlfa(dati.getColore(), alpha);
		} else {
			int argb = dati.getColoreRgb();
			colore = new Color(((argb & 0xFFFFFF) << 8) | 0xFF);
			colore.a = alpha * (argb >>> 24) / 255f;
		}
		testo.disegna(batch, dati.getTesto(), testoX, testoY, colore, scala);
	}
}
