package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpriteATempo implements SpriteInterface {

	private static final float DURATA_IN_SECONDI = 1.6f;
	private static final float LIMITE_PRE_DISSOLVENZA_IN_SECONDI = DURATA_IN_SECONDI / 2;

	private String descrizione;
	boolean active;
	private BufferedImage image;
	private int x;
	private float y;
	private float secondiTrascorsi;

	SpriteATempo(BufferedImage image, int x, int y, String descrizione) {
		this.image = image;
		this.x = x;
		this.y = y;
		secondiTrascorsi = 0;
		active = true;
		this.descrizione = descrizione;
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int x, int y, String descrizione) {
		DoomdarkColorModel.Color color;
		StringBuilder sb = new StringBuilder();
		if (variazione >= 0) {
			sb.append("+");
			color = DoomdarkColorModel.Color.GREEN;
		} else {
			color = DoomdarkColorModel.Color.RED;
		}
		sb.append(variazione);
		init(icona, sb.toString(), font, color, x, y, descrizione);
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y,
				 String descrizione) {
		StringBuilder sb = new StringBuilder();
		if (variazione >= 0) {
			sb.append("+");
		}
		sb.append(variazione);
		init(icona, sb.toString(), font, color, x, y, descrizione);
	}

	public String getDescrizione() {
		return descrizione;
	}

	private void init(BufferedImage icona, String testo, DoomdarkFont font, DoomdarkColorModel.Color color,
					  int x, int y, String descrizione) {
		this.descrizione = descrizione;
		Image doomdark = DoomdarkTextProducer.getImage(testo, font, color);
		Image blackDoomdark = DoomdarkTextProducer.getImage(testo, font, DoomdarkColorModel.Color.BLACK);
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

		image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
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
		
		this.x = x - image.getWidth();
		this.y = y - offsetYTesto;
		secondiTrascorsi = 0;
		active = true;
	}

	public void anima(Graphics2D g) {
		if (active) {
			if (secondiTrascorsi > LIMITE_PRE_DISSOLVENZA_IN_SECONDI) {
				// La curva di dissolvenza era tarata sui tick storici da 0.1s (1/(ticks-limite)):
				// moltiplicando per 10 i secondi oltre soglia si ottiene lo stesso andamento.
				float transparency = Math.min(1.0f, 1.0f / ((secondiTrascorsi - LIMITE_PRE_DISSOLVENZA_IN_SECONDI) * 10));
				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
				g.setComposite(ac);
			} else {
				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g.setComposite(ac);
			}
			g.drawImage(image, x, Math.round(y), null);
			// 10 pixel al secondo, come nel vecchio loop a 10 fps (1 pixel per fotogramma).
			y -= 10f / 30;
			secondiTrascorsi += 1f / 30;
			if (secondiTrascorsi >= DURATA_IN_SECONDI) {
				active = false;
			}
		}
	}
	
	public boolean isAttivo() {
		return active;
	}
}
