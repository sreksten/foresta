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
public class SpriteEffetto implements SpriteInterface {

	private static final float DURATA_IN_SECONDI = 2.0f;
	private static final float DURATA_FADE_IN_SECONDI = DURATA_IN_SECONDI / 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;
	private static final int OMBRA_SCOSTAMENTO = 2;
	private static final Color OMBRA_COLORE = new Color(0, 0, 0, 160);

	private final String testo;
	private final BufferedImage immagine;
	private final int x;
	private final int y;
	private float secondiTrascorsi;
	private boolean active;

	SpriteEffetto(String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y) {
		this(testo, costruisciTestoColorato(testo, font, color), x, y);
	}

	SpriteEffetto(String testo, DoomdarkFont font, BufferedImage pattern, int x, int y) {
		this(testo, costruisciTestoConPattern(testo, font, pattern), x, y);
	}

	private SpriteEffetto(String testo, BufferedImage testoRenderizzato, int x, int y) {
		this.testo = testo;
		this.immagine = aggiungiOmbra(testoRenderizzato);
		this.x = x;
		this.y = y;
		active = true;
	}

	public String getTesto() {
		return testo;
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

	private static BufferedImage aggiungiOmbra(BufferedImage testo) {
		BufferedImage ombra = creaSagomaScura(testo);

		int larghezzaTotale = testo.getWidth() + OMBRA_SCOSTAMENTO * 2;
		int altezzaTotale = testo.getHeight() + OMBRA_SCOSTAMENTO * 2;
		BufferedImage risultato = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = risultato.createGraphics();
		int[] scostamenti = {-OMBRA_SCOSTAMENTO, 0, OMBRA_SCOSTAMENTO};
		for (int dx : scostamenti) {
			for (int dy : scostamenti) {
				if (dx != 0 || dy != 0) {
					g.drawImage(ombra, OMBRA_SCOSTAMENTO + dx, OMBRA_SCOSTAMENTO + dy, null);
				}
			}
		}
		g.drawImage(testo, OMBRA_SCOSTAMENTO, OMBRA_SCOSTAMENTO, null);
		g.dispose();
		return risultato;
	}

	private static BufferedImage creaSagomaScura(BufferedImage sorgente) {
		BufferedImage sagoma = new BufferedImage(sorgente.getWidth(), sorgente.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sagoma.createGraphics();
		g.drawImage(sorgente, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setColor(OMBRA_COLORE);
		g.fillRect(0, 0, sagoma.getWidth(), sagoma.getHeight());
		g.dispose();
		return sagoma;
	}

	@Override
	public void anima(Graphics2D g) {
		if (!active) {
			return;
		}
		if (secondiTrascorsi >= DURATA_IN_SECONDI) {
			active = false;
			return;
		}

		float progresso = secondiTrascorsi / DURATA_IN_SECONDI;
		float scala = SCALA_INIZIALE + (SCALA_FINALE - SCALA_INIZIALE) * progresso;
		float alpha;
		if (secondiTrascorsi <= DURATA_FADE_IN_SECONDI) {
			alpha = 1.0f;
		} else {
			alpha = Math.max(0.0f, 1.0f - (secondiTrascorsi - DURATA_FADE_IN_SECONDI) / (DURATA_IN_SECONDI - DURATA_FADE_IN_SECONDI));
		}

		Composite compositeOriginale = g.getComposite();
		Object interpolazioneOriginale = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		int disegnaX = x - (larghezzaScalata >> 1);
		int disegnaY = y - (altezzaScalata >> 1);
		g.drawImage(immagine, disegnaX, disegnaY, larghezzaScalata, altezzaScalata, null);

		g.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
		}

		secondiTrascorsi += 1f / 30;
	}

	@Override
	public boolean isAttivo() {
		return active;
	}
}
