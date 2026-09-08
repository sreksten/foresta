package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class BufferedImageBuilder {
	
	private BufferedImageBuilder() {
	}

	private static final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	public static BufferedImage buildBufferedImage(String resource) {
		if (resource != null && !resource.isEmpty()) {
			try {
				InputStream in = BufferedImageBuilder.class.getResourceAsStream("/com/threeamigos/foresta/img/" + resource);
				if (in == null) {
					throw new IllegalArgumentException("Non trovo il file " + resource);
				}
				BufferedImage img = ImageIO.read(in);
				BufferedImage copy = gc.createCompatibleImage(img.getWidth(), img.getHeight(), img.getTransparency());
				Graphics2D g2d = copy.createGraphics();
				g2d.drawImage(img, 0, 0, null);
				g2d.dispose();
				Logger.log("Image resource: " + resource + ", " + copy.getWidth() + "x" + copy.getHeight());
				return copy;
			} catch (Exception e) {
				Logger.log(e);
				System.exit(0);
			}
		}
		return null;
	}

}
