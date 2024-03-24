package com.threeamigos.foresta.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class SpriteInDissolvenza implements SpriteInterface {

	private static final int MAX_TICKS = 32;
	
	boolean active;
	private BufferedImage image;
	private int x;
	private int y;
	private int ticks;
	
	SpriteInDissolvenza(BufferedImage image, int x, int y) {
		this.image = image;
		this.x = x;
		this.y = y;
		ticks = 0;
		active = true;
	}
	
	public void animate(Graphics2D g) {
		if (active) {
			float transparency = 1.0f / (float)(1 + ticks);
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
			g.setComposite(ac);
			int size = 3;
			if ((size & 1) == 0) {
				size++;
			}
			int numCoords = size * size;
			float blurFactor = 1.0f / (float)numCoords;
			float[] blurKernel = new float[numCoords];
			for (int i = 0; i < numCoords; i++) {
				blurKernel[i] = blurFactor;
			}
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
