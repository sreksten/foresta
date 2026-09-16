package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

public class SpriteInDissolvenza implements SpriteInterface {

	private static final int MAX_TICKS = 32;

	private final String descrizione;
	private final BufferedImage image;
	boolean active;
	private final int x;
	private final int y;
	private int ticks;
	
	SpriteInDissolvenza(String descrizione, BufferedImage image, int x, int y) {
		this.descrizione = descrizione;
		this.image = image;
		this.x = x;
		this.y = y;
		ticks = 0;
		active = true;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void animate(Graphics2D g) {
		if (active) {
			float transparency = 1.0f / (float)(1 + ticks);
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
			g.setComposite(ac);
			int size = 3;
            int numCoords = size * size;
			float blurFactor = 1.0f / (float)numCoords;
			float[] blurKernel = new float[numCoords];
            Arrays.fill(blurKernel, blurFactor);
			ConvolveOp blurringOp = new ConvolveOp(new Kernel(size, size, blurKernel), ConvolveOp.EDGE_NO_OP, null);
			g.drawImage(image, blurringOp, x, y);
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
