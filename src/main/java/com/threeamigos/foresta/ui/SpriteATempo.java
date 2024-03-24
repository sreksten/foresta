package com.threeamigos.foresta.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

class SpriteATempo implements SpriteInterface {

	private static final int MAX_TICKS = 16;
	private static final int TICK_LIMIT_BEFORE_FADING = MAX_TICKS >> 1;
	
	boolean active;
	private BufferedImage image;
	private int x;
	private int y;
	private int ticks;
	
	SpriteATempo(BufferedImage image, int x, int y) {
		this.image = image;
		this.x = x;
		this.y = y;
		ticks = 0;
		active = true;
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int x, int y) {
		DoomdarkColorModel.Color color;
		StringBuilder sb = new StringBuilder();
		if (variazione < 0) {
			color = DoomdarkColorModel.Color.RED;
		} else {
			sb.append("+");
			color = DoomdarkColorModel.Color.GREEN;
		}
		sb.append(variazione);
		init(icona, sb.toString(), font, color, x, y);
	}

	SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y) {
		StringBuilder sb = new StringBuilder();
		if (variazione >= 0) {
			sb.append("+");
		}
		sb.append(variazione);
		init(icona, sb.toString(), font, color, x, y);
	}
	
	
	SpriteATempo(BufferedImage icona, String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y) {
		init(icona, testo, font, color, x, y);
	}
	
	private void init(BufferedImage icona, String testo, DoomdarkFont font, DoomdarkColorModel.Color color, int x, int y) {
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
		ticks = 0;
		active = true;
	}
	
	public void animate(Graphics2D g) {
		if (active) {
			if (ticks > TICK_LIMIT_BEFORE_FADING) {
				float transparency = 1.0f / (float)(ticks - TICK_LIMIT_BEFORE_FADING);
				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
				g.setComposite(ac);
			} else {
				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g.setComposite(ac);
			}
			g.drawImage(image, x, y, null);
			y--;
			ticks++;
			if (ticks >= MAX_TICKS) {
				active = false;
			}
		}
	}
	
	public boolean isActive() {
		return active;
	}
}
