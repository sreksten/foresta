package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

public class SpriteInDissolvenza extends SpriteBase {

	private static final float DURATA_IN_SECONDI = 3.2f;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = 0;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.0f;

	private final String descrizione;
	private final BufferedImage immagineFornita;

	SpriteInDissolvenza(String descrizione, BufferedImage immagine, int xIniziale, int yIniziale) {
		this.descrizione = descrizione;
		this.immagineFornita = immagine;
		inizializza(buildImage(),
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				null, 0,
				xIniziale, yIniziale,
				xIniziale, yIniziale,
				SCALA_INIZIALE, SCALA_FINALE);
	}

	public String getDescrizione() {
		return descrizione;
	}

	@Override
	protected BufferedImage buildImage() {
		return immagineFornita;
	}

	@Override
	protected float calcolaAlpha(float secondiTrascorsi) {
		// Curva tarata sui tick storici da 0.1s (1/(1+ticks)): moltiplicando per 10 i secondi
		// trascorsi si ottiene lo stesso andamento. A differenza degli altri sfuma da subito.
		return 1.0f / (1 + secondiTrascorsi * 10);
	}

	@Override
	protected void disegna(Graphics2D g, float x, float y, float scala) {
		int dimensione = 3;
		int numeroCoordinate = dimensione * dimensione;
		float fattoreSfocatura = 1.0f / (float)numeroCoordinate;
		float[] kernelSfocatura = new float[numeroCoordinate];
		Arrays.fill(kernelSfocatura, fattoreSfocatura);
		ConvolveOp convoluzione = new ConvolveOp(new Kernel(dimensione, dimensione, kernelSfocatura), ConvolveOp.EDGE_NO_OP, null);
		g.drawImage(immagine, convoluzione, Math.round(x), Math.round(y));
	}

}
