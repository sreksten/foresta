package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

/**
 * Un'immagine ferma che sfuma sul posto, subito o dopo un'attesa durante la quale resta intera.
 */
public class SpriteInDissolvenza extends SpriteBase {

	private static final float DURATA_IN_SECONDI = 1.2f;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = 0;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.0f;

	private final String descrizione;
	private final BufferedImage immagineFornita;
	// Per la dissolvenza con attesa: restare interi per un po' e poi sfumare in modo lineare, senza sfocare
	private final boolean conAttesa;

	public SpriteInDissolvenza(String descrizione, BufferedImage immagine, int xIniziale, int yIniziale) {
		this.descrizione = descrizione;
		this.immagineFornita = immagine;
		this.conAttesa = false;
		inizializza(buildImage(),
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI, Dissolvenza.DA_SUBITO,
				null, 0,
				xIniziale, yIniziale,
				xIniziale, yIniziale,
				SCALA_INIZIALE, SCALA_FINALE);
	}

	/**
	 * Un'immagine che resta intera per {@code attesaInSecondi} e poi sfuma in modo lineare in {@code dissolvenzaInSecondi},
	 * senza sfocarsi: per un riquadro che si deve vedere un attimo prima di andare via (vedi
	 * DisplayableCanvasRiquadroSfida).
	 */
	public SpriteInDissolvenza(String descrizione, BufferedImage immagine, int xIniziale, int yIniziale, float attesaInSecondi,
						float dissolvenzaInSecondi) {
		this.descrizione = descrizione;
		this.immagineFornita = immagine;
		this.conAttesa = true;
		inizializza(buildImage(),
				attesaInSecondi + dissolvenzaInSecondi, attesaInSecondi, Dissolvenza.LINEARE_CON_SOGLIA,
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
	public void disegna(Graphics2D g, float x, float y, float scala) {
		if (conAttesa) {
			g.drawImage(immagine, Math.round(x), Math.round(y), null);
			return;
		}
		int dimensione = 3;
		int numeroCoordinate = dimensione * dimensione;
		float fattoreSfocatura = 1.0f / (float)numeroCoordinate;
		float[] kernelSfocatura = new float[numeroCoordinate];
		Arrays.fill(kernelSfocatura, fattoreSfocatura);
		ConvolveOp convoluzione = new ConvolveOp(new Kernel(dimensione, dimensione, kernelSfocatura), ConvolveOp.EDGE_NO_OP, null);
		g.drawImage(immagine, convoluzione, Math.round(x), Math.round(y));
	}

}
