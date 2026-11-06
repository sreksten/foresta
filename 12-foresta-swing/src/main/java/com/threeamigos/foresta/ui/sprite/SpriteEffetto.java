package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Notifica di interazione elementale o effetto di stato da sovrapporre a un nemico.
 * Il testo è costruito con il Doomdark font (come {@link SpriteATempo}) ma l'animazione
 * è quella di {@link SpriteAnnuncioGlobale}: ingrandimento progressivo con dissolvenza finale.
 * A differenza di {@link SpriteAnnuncioGlobale} non è centrato sullo schermo ma ancorato alle
 * coordinate passate al costruttore. Il testo può essere colorato con un colore pieno
 * oppure con un pattern (bitmap tilata) al posto del colore.
 */
public class SpriteEffetto extends SpriteBase {

	private static final float DURATA_IN_SECONDI = DescrizioneSpriteEffetto.DURATA_IN_SECONDI;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DescrizioneSpriteEffetto.ATTACCO_DISSOLVENZA_DOPO_SECONDI;
	private static final float SCALA_INIZIALE = DescrizioneSpriteEffetto.SCALA_INIZIALE;
	private static final float SCALA_FINALE = DescrizioneSpriteEffetto.SCALA_FINALE;
	private static final int OMBRA_SCOSTAMENTO = DescrizioneSpriteEffetto.OMBRA_SCOSTAMENTO;
	private static final Color OMBRA_COLORE = new Color(DescrizioneSpriteEffetto.OMBRA_ARGB, true);

	private final String testo;
	private final BufferedImage testoRenderizzato;

	/**
	 * Uno sprite che mostra quel che dice la descrizione
	 */
	public SpriteEffetto(DescrizioneSpriteEffetto dati) {
		this(dati.getTesto(), dati.getColore() != null ? costruisciTestoColorato(dati.getTesto(), dati.getFont(), dati.getColore())
				: costruisciTestoConPattern(dati.getTesto(), dati.getFont(), pixelDelColore(dati.getColoreRgb())), dati.getX(), dati.getY());
	}

	private static BufferedImage pixelDelColore(int argb) {
		BufferedImage pattern = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		pattern.setRGB(0, 0, argb);
		return pattern;
	}

	public SpriteEffetto(String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int xIniziale, int yIniziale) {
		this(testo, costruisciTestoColorato(testo, font, color), xIniziale, yIniziale);
	}

	public SpriteEffetto(String testo, DoomdarkFont font, BufferedImage pattern, int xIniziale, int yIniziale) {
		this(testo, costruisciTestoConPattern(testo, font, pattern), xIniziale, yIniziale);
	}

	private SpriteEffetto(String testo, BufferedImage testoRenderizzato, float xIniziale, float yIniziale) {
		this.testo = testo;
		this.testoRenderizzato = testoRenderizzato;
		inizializza(buildImage(),
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI, Dissolvenza.LINEARE_CON_SOGLIA,
				OMBRA_COLORE, OMBRA_SCOSTAMENTO,
				xIniziale, yIniziale,
				xIniziale, yIniziale,
				SCALA_INIZIALE, SCALA_FINALE);
	}

	public String getTesto() {
		return testo;
	}

	/**
	 * Centra lo sprite su quel punto, spostandolo quanto serve perché, alla sua scala finale, non esca dai limiti.
	 */
	public void centraEntro(float xCentro, float limiteSinistro, float limiteDestro) {
		impostaX(centroEntro(xCentro, immagine.getWidth() * SCALA_FINALE, limiteSinistro, limiteDestro));
	}

	/**
	 * Il centro di uno sprite largo così, il più vicino possibile a quel punto, che lo tiene dentro i limiti; se non ci sta, il centro dei limiti.
	 */
	public static float centroEntro(float xCentro, float larghezza, float limiteSinistro, float limiteDestro) {
		return DescrizioneSpriteEffetto.centroEntro(xCentro, larghezza, limiteSinistro, limiteDestro);
	}

	@Override
	protected BufferedImage buildImage() {
		return testoRenderizzato;
	}

	private static BufferedImage costruisciTestoColorato(String testo, DoomdarkFont font, DoomdarkColorModel.Color color) {
		Image doomdark = ImageCache.get(testo, font, color);
		BufferedImage immagine = new BufferedImage(doomdark.getWidth(null), doomdark.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		g.drawImage(doomdark, 0, 0, null);
		g.dispose();
		return immagine;
	}

	private static BufferedImage costruisciTestoConPattern(String testo, DoomdarkFont font, BufferedImage pattern) {
		Image sagoma = ImageCache.get(testo, font, DoomdarkColorModel.Color.WHITE);
		int larghezza = sagoma.getWidth(null);
		int altezza = sagoma.getHeight(null);

		BufferedImage immagine = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		g.drawImage(sagoma, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setPaint(new TexturePaint(pattern, new Rectangle(0, 0, pattern.getWidth(), pattern.getHeight())));
		g.fillRect(0, 0, larghezza, altezza);
		g.dispose();
		return immagine;
	}


	@Override
	public void disegna(Graphics2D g, float x, float y, float scala) {
		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		int disegnaX = Math.round(x) - (larghezzaScalata >> 1);
		int disegnaY = Math.round(y) - (altezzaScalata >> 1);
		g.drawImage(immagine, disegnaX, disegnaY, larghezzaScalata, altezzaScalata, null);
	}
}
