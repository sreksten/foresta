package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Un valore fatto di due testi di colori diversi, uno accanto all'altro (per esempio il livello di un artefatto in grigio e il suo prezzo in giallo): lo si dà
 * a un nodo di {@link ElencoScorrevole} come immagine opaca, con la sua {@link #getMisura() misura}, e chi disegna lo compone.
 */
public final class ValoreDiDueTesti {

	private final String primo;
	private final DoomdarkColorModel.Color coloreDelPrimo;
	private final String secondo;
	private final DoomdarkColorModel.Color coloreDelSecondo;
	private final DoomdarkFont font;
	private final int spazio;

	public ValoreDiDueTesti(String primo, DoomdarkColorModel.Color coloreDelPrimo, String secondo, DoomdarkColorModel.Color coloreDelSecondo, DoomdarkFont font,
							int spazio) {
		this.primo = primo;
		this.coloreDelPrimo = coloreDelPrimo;
		this.secondo = secondo;
		this.coloreDelSecondo = coloreDelSecondo;
		this.font = font;
		this.spazio = spazio;
	}

	public String getPrimo() {
		return primo;
	}

	public DoomdarkColorModel.Color getColoreDelPrimo() {
		return coloreDelPrimo;
	}

	public String getSecondo() {
		return secondo;
	}

	public DoomdarkColorModel.Color getColoreDelSecondo() {
		return coloreDelSecondo;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	/**
	 * Dove sta il secondo testo, a destra del primo
	 */
	public int getXDelSecondo() {
		return Math.max(1, FontTool.getWidth(font, primo)) + spazio;
	}

	/**
	 * Quanto è grande tutto il valore
	 */
	public Misura getMisura() {
		return new Misura(getXDelSecondo() + Math.max(1, FontTool.getWidth(font, secondo)), font.getHeight());
	}

	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof ValoreDiDueTesti)) {
			return false;
		}
		ValoreDiDueTesti v = (ValoreDiDueTesti) altro;
		return primo.equals(v.primo) && secondo.equals(v.secondo) && coloreDelPrimo == v.coloreDelPrimo && coloreDelSecondo == v.coloreDelSecondo && font == v.font
				&& spazio == v.spazio;
	}

	@Override
	public int hashCode() {
		return primo.hashCode() * 31 + secondo.hashCode();
	}
}
