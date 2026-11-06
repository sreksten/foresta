package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Che cosa mostra uno sprite di effetto, senza immagini: un testo (il nome di un effetto di stato, di un'interazione, di un modificatore, "Mancato") che
 * si ingrandisce sul posto mentre sfuma, con un'ombra scura. Il testo è di un colore del font o di un colore qualunque (RGB); chi disegna lo scrive
 * in quel colore. Il movimento e la dissolvenza sono di {@link ModelloDelloSprite}.
 */
public final class DescrizioneSpriteEffetto {

	public static final float DURATA_IN_SECONDI = 2.0f;
	public static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	public static final float SCALA_INIZIALE = 1.0f;
	public static final float SCALA_FINALE = 1.8f;
	public static final int OMBRA_SCOSTAMENTO = 2;
	/**
	 * L'ombra, ARGB: nero con un po' di trasparenza
	 */
	public static final int OMBRA_ARGB = (160 << 24);

	private final String testo;
	private final DoomdarkFont font;
	private final DoomdarkColorModel.Color colore;
	private final Integer coloreRgb;
	private float x;
	private final int y;

	private DescrizioneSpriteEffetto(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore, Integer coloreRgb, int x, int y) {
		this.testo = testo;
		this.font = font;
		this.colore = colore;
		this.coloreRgb = coloreRgb;
		this.x = x;
		this.y = y;
	}

	/**
	 * Un testo di un colore del font, che parte centrato su (x, y)
	 */
	public static DescrizioneSpriteEffetto colorato(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore, int x, int y) {
		return new DescrizioneSpriteEffetto(testo, font, colore, null, x, y);
	}

	/**
	 * Un testo di un colore qualunque (ARGB)
	 */
	public static DescrizioneSpriteEffetto conColoreRgb(String testo, DoomdarkFont font, int coloreArgb, int x, int y) {
		return new DescrizioneSpriteEffetto(testo, font, null, coloreArgb, x, y);
	}

	public String getTesto() {
		return testo;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	/**
	 * Il colore del font, o null se il colore è un RGB qualunque ({@link #getColoreRgb()})
	 */
	public DoomdarkColorModel.Color getColore() {
		return colore;
	}

	public Integer getColoreRgb() {
		return coloreRgb;
	}

	public float getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/**
	 * Centra il testo su quel punto, spostandolo quanto serve perché, alla sua scala finale e con l'ombra, non esca dai limiti
	 */
	public DescrizioneSpriteEffetto centraEntro(float xCentro, float limiteSinistro, float limiteDestro) {
		float larghezzaConOmbra = getLarghezzaDelTesto() + 2 * OMBRA_SCOSTAMENTO;
		x = centroEntro(xCentro, larghezzaConOmbra * SCALA_FINALE, limiteSinistro, limiteDestro);
		return this;
	}

	/**
	 * La larghezza del testo scritto (almeno un pixel)
	 */
	public int getLarghezzaDelTesto() {
		return Math.max(1, FontTool.getWidth(font, testo));
	}

	/**
	 * L'altezza del testo scritto
	 */
	public int getAltezzaDelTesto() {
		return font.getHeight();
	}

	/**
	 * Il centro di uno sprite largo così, il più vicino possibile a quel punto, che lo tiene dentro i limiti; se non ci sta, il centro dei limiti.
	 */
	public static float centroEntro(float xCentro, float larghezza, float limiteSinistro, float limiteDestro) {
		float meta = larghezza / 2.0f;
		if (limiteDestro - limiteSinistro < larghezza) {
			return (limiteSinistro + limiteDestro) / 2.0f;
		}
		return Math.max(limiteSinistro + meta, Math.min(limiteDestro - meta, xCentro));
	}
}
