package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpriteATempo extends SpriteBase {

	private static final float DURATA_IN_SECONDI = 1.6f;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	private static final float DISTANZA_TOTALE_MOVIMENTO = 10f * DURATA_IN_SECONDI;

	private final String descrizione;

	/**
	 * Campi usati solo da {@link #buildImage()}: da un lato il caso "immagine già pronta"
	 * (icona singola), dall'altro il caso "icona + testo Doomdark da comporre".
	 */
	private BufferedImage immagineFornita;
	private BufferedImage icona;
	private String testoVariazione;
	private DoomdarkFont font;
	private DoomdarkColorModel.Color color;
	private boolean ancoraSinistra;

	/**
	 * Coordinate di appoggio, mutabili: {@link #buildImage()} le corregge in base alle
	 * dimensioni dell'immagine appena costruita (es. per allineare icona e testo una volta
	 * composti), prima che {@link #completaCostruzione()} le passi a {@code inizializza()}
	 * come posizione iniziale/finale definitiva dello sprite.
	 */
	private float x;
	private float y;

	SpriteATempo(BufferedImage icona, int xIniziale, int yIniziale, String descrizione) {
		this.descrizione = descrizione;
		this.immagineFornita = icona;
		this.x = xIniziale;
		this.y = yIniziale;
		completaCostruzione();
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int xIniziale, int yIniziale, String descrizione) {
		this(icona, variazione, font, xIniziale, yIniziale, false, descrizione);
	}

	/**
	 * @param ancoraSinistra se {@code true}, {@code xIniziale} è il bordo sinistro dell'icona
	 *                       (l'immagine composta icona+testo si estende verso destra); se
	 *                       {@code false} (comportamento storico) è il bordo destro del testo
	 *                       (l'immagine composta si estende verso sinistra).
	 */
	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int xIniziale, int yIniziale,
				 boolean ancoraSinistra, String descrizione) {
		this(icona, variazione, font,
				variazione >= 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED,
				xIniziale, yIniziale, ancoraSinistra, descrizione);
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color,
				 int xIniziale, int yIniziale, String descrizione) {
		this(icona, variazione, font, color, xIniziale, yIniziale, false, descrizione);
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color,
				 int xIniziale, int yIniziale, boolean ancoraSinistra, String descrizione) {
		this.descrizione = descrizione;
		this.icona = icona;
		this.testoVariazione = testoVariazione(variazione);
		this.font = font;
		this.color = color;
		this.x = xIniziale;
		this.y = yIniziale;
		this.ancoraSinistra = ancoraSinistra;
		completaCostruzione();
	}

	private void completaCostruzione() {
		BufferedImage immagineCostruita = buildImage();
		inizializza(immagineCostruita,
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				null, 0,
				x, y,
				x, y - DISTANZA_TOTALE_MOVIMENTO,
				1f, 1f);
	}

	public String getDescrizione() {
		return descrizione;
	}

	private static String testoVariazione(int variazione) {
		StringBuilder sb = new StringBuilder();
		if (variazione >= 0) {
			sb.append("+");
		}
		sb.append(variazione);
		return sb.toString();
	}

	@Override
	protected BufferedImage buildImage() {
		if (immagineFornita != null) {
			return immagineFornita;
		}

		Image doomdark = DoomdarkTextProducer.getImage(testoVariazione, font, color);
		Image blackDoomdark = DoomdarkTextProducer.getImage(testoVariazione, font, DoomdarkColorModel.Color.BLACK);
		int doomdarkHeight = doomdark.getHeight(null) + 2; // per il bordo nero
		int width;
		int height;
		int offsetYIcona;
		int offsetXTesto;
		int offsetYTesto;
		if (icona != null) {
			width = icona.getWidth() + doomdark.getWidth(null) + 3;
			offsetXTesto = icona.getWidth() + 1;
			height = Math.max(icona.getHeight(), doomdarkHeight);
			if (icona.getHeight() < doomdarkHeight) {
				offsetYIcona = ((doomdarkHeight - icona.getHeight()) >> 1);
				offsetYTesto = 0;
			} else {
				offsetYIcona = 0;
				offsetYTesto = ((icona.getHeight() - doomdarkHeight) >> 1);
			}
		} else {
			width = doomdark.getWidth(null) + 2;
			height = doomdarkHeight;
			offsetXTesto = 0;
			offsetYTesto = 0;
			offsetYIcona = 0;
		}

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		BufferedImage image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g2d = image.createGraphics();
		if (icona != null) {
			g2d.drawImage(icona, 0, offsetYIcona, null);
		}

		g2d.drawImage(blackDoomdark, offsetXTesto, offsetYTesto, null);
		g2d.drawImage(blackDoomdark, offsetXTesto + 1, offsetYTesto, null);
		g2d.drawImage(blackDoomdark, offsetXTesto + 2, offsetYTesto, null);

		g2d.drawImage(blackDoomdark, offsetXTesto, offsetYTesto + 1, null);
		g2d.drawImage(blackDoomdark, offsetXTesto + 2, offsetYTesto + 1, null);

		g2d.drawImage(blackDoomdark, offsetXTesto, offsetYTesto + 2, null);
		g2d.drawImage(blackDoomdark, offsetXTesto + 1, offsetYTesto + 2, null);
		g2d.drawImage(blackDoomdark, offsetXTesto + 2, offsetYTesto + 2, null);

		g2d.drawImage(doomdark, offsetXTesto + 1, offsetYTesto + 1, null);
		g2d.dispose();

		if (!ancoraSinistra) {
			this.x -= image.getWidth();
		}
		this.y -= offsetYTesto;
		return image;
	}

	@Override
	protected float calcolaAlpha(float secondiTrascorsi) {
		return dissolvenzaIperbolicaConSoglia(secondiTrascorsi, momentoInizioFade);
	}

	@Override
	protected void disegna(Graphics2D g, float x, float y, float scala) {
		g.drawImage(immagine, Math.round(x), Math.round(y), null);
	}
}
