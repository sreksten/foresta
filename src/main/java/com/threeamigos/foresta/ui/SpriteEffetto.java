package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Notifica di interazione elementale o effetto di stato da sovrapporre a un nemico.
 * Il testo è costruito con il Doomdark font (come {@link SpriteATempo}) ma l'animazione
 * è quella di {@link SpriteMissione}: ingrandimento progressivo con dissolvenza finale.
 * A differenza di {@link SpriteMissione} non è centrato sullo schermo ma ancorato alle
 * coordinate passate al costruttore. Il testo può essere colorato con un colore pieno
 * oppure con un pattern (bitmap tilata) al posto del colore.
 */
class SpriteEffetto implements SpriteInterface {

	//private static final long DURATA_MS = 3500L;
	private static final long DURATA_MS = 2000L;
	private static final long DURATA_FADE_MS = DURATA_MS / 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;
	private static final int OMBRA_SCOSTAMENTO = 2;
	private static final Color OMBRA_COLORE = new Color(0, 0, 0, 160);

	private final BufferedImage image;
	private final int x;
	private final int y;
	private long inizio = -1;
	private boolean active;

	SpriteEffetto(String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y) {
		this(costruisciTestoColorato(testo, font, color), x, y);
	}

	SpriteEffetto(String testo, DoomdarkFont font, BufferedImage pattern, int x, int y) {
		this(costruisciTestoConPattern(testo, font, pattern), x, y);
	}

	private SpriteEffetto(BufferedImage testo, int x, int y) {
		this.image = aggiungiOmbra(testo);
		this.x = x;
		this.y = y;
		active = true;
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
	public void animate(Graphics2D g) {
		if (!active) {
			return;
		}
		long adesso = System.currentTimeMillis();
		if (inizio < 0) {
			inizio = adesso;
		}
		long trascorsi = adesso - inizio;
		if (trascorsi >= DURATA_MS) {
			active = false;
			return;
		}

		float progresso = trascorsi / (float) DURATA_MS;
		float scala = SCALA_INIZIALE + (SCALA_FINALE - SCALA_INIZIALE) * progresso;
		float alpha;
		if (trascorsi <= DURATA_FADE_MS) {
			alpha = 1.0f;
		} else {
			alpha = Math.max(0.0f, 1.0f - (trascorsi - DURATA_FADE_MS) / (float) (DURATA_MS - DURATA_FADE_MS));
		}

		Composite compositeOriginale = g.getComposite();
		Object interpolazioneOriginale = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int larghezzaScalata = Math.round(image.getWidth() * scala);
		int altezzaScalata = Math.round(image.getHeight() * scala);
		int disegnaX = x - (larghezzaScalata >> 1);
		int disegnaY = y - (altezzaScalata >> 1);
		g.drawImage(image, disegnaX, disegnaY, larghezzaScalata, altezzaScalata, null);

		g.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
