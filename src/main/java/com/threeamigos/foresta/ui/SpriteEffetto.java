package com.threeamigos.foresta.ui;

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

	private static final float DURATA_IN_SECONDI = 2.0f;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;
	private static final int OMBRA_SCOSTAMENTO = 2;
	private static final Color OMBRA_COLORE = new Color(0, 0, 0, 160);

	private final String testo;
	private final BufferedImage testoRenderizzato;

	SpriteEffetto(String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int xIniziale, int yIniziale) {
		this(testo, costruisciTestoColorato(testo, font, color), xIniziale, yIniziale);
	}

	SpriteEffetto(String testo, DoomdarkFont font, BufferedImage pattern, int xIniziale, int yIniziale) {
		this(testo, costruisciTestoConPattern(testo, font, pattern), xIniziale, yIniziale);
	}

	private SpriteEffetto(String testo, BufferedImage testoRenderizzato, int xIniziale, int yIniziale) {
		this.testo = testo;
		this.testoRenderizzato = testoRenderizzato;
		inizializza(buildImage(),
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				OMBRA_COLORE, OMBRA_SCOSTAMENTO,
				xIniziale, yIniziale,
				xIniziale, yIniziale,
				SCALA_INIZIALE, SCALA_FINALE);
	}

	public String getTesto() {
		return testo;
	}

	@Override
	protected BufferedImage buildImage() {
		return testoRenderizzato;
	}

	private static BufferedImage costruisciTestoColorato(String testo, DoomdarkFont font, DoomdarkColorModel.Color color) {
		Image doomdark = DoomdarkTextProducer.getImage(testo, font, color);
		BufferedImage immagine = new BufferedImage(doomdark.getWidth(null), doomdark.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		g.drawImage(doomdark, 0, 0, null);
		g.dispose();
		return immagine;
	}

	private static BufferedImage costruisciTestoConPattern(String testo, DoomdarkFont font, BufferedImage pattern) {
		Image sagoma = DoomdarkTextProducer.getImage(testo, font, DoomdarkColorModel.Color.WHITE);
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
	protected float calcolaAlpha(float secondiTrascorsi) {
		return dissolvenzaLineareConSoglia(secondiTrascorsi, momentoInizioFade, durataInSecondi);
	}

	@Override
	protected void disegna(Graphics2D g, float x, float y, float scala) {
		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		int disegnaX = Math.round(x) - (larghezzaScalata >> 1);
		int disegnaY = Math.round(y) - (altezzaScalata >> 1);
		g.drawImage(immagine, disegnaX, disegnaY, larghezzaScalata, altezzaScalata, null);
	}
}
