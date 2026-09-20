package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

public class SpriteInDissolvenza implements SpriteInterface {

	private static final float DURATA_IN_SECONDI = 3.2f;

	private final String descrizione;
	private final BufferedImage immagine;
	boolean attivo;
	private final int x;
	private final int y;
	private float secondiTrascorsi;

	SpriteInDissolvenza(String descrizione, BufferedImage immagine, int x, int y) {
		this.descrizione = descrizione;
		this.immagine = immagine;
		this.x = x;
		this.y = y;
		secondiTrascorsi = 0;
		attivo = true;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void anima(Graphics2D g) {
		if (attivo) {
			// La curva era tarata sui tick storici da 0.1s (1/(1+ticks)): moltiplicando per 10
			// i secondi trascorsi si ottiene lo stesso andamento.
			float trasparenza = 1.0f / (1 + secondiTrascorsi * 10);
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, trasparenza);
			g.setComposite(ac);
			int dimensione = 3;
            int numeroCoordinate = dimensione * dimensione;
			float fattoreSfocatura = 1.0f / (float)numeroCoordinate;
			float[] kernelSfocatura = new float[numeroCoordinate];
            Arrays.fill(kernelSfocatura, fattoreSfocatura);
			ConvolveOp convoluzione = new ConvolveOp(new Kernel(dimensione, dimensione, kernelSfocatura), ConvolveOp.EDGE_NO_OP, null);
			g.drawImage(immagine, convoluzione, x, y);
			secondiTrascorsi += 1f / 30;
			if (secondiTrascorsi >= DURATA_IN_SECONDI) {
				attivo = false;
			}
		}
	}
	
	public boolean isAttivo() {
		return attivo;
	}

}
