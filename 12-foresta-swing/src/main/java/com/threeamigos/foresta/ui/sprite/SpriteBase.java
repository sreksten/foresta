package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La base degli sprite con un'immagine che si muove da un punto a un altro, cambia scala e sfuma. La sottoclasse costruisce l'immagine ({@link #buildImage()}), dice come disegnarla e come
 * cresce la trasparenza e la richiama {@link #inizializza}; la base misura il tempo reale che passa fra una chiamata e l'altra di {@link #anima(Graphics2D)}, calcola posizione e scala per interpolazione
 * e disattiva lo sprite alla fine. Se si indica un ritaglio, lo sprite si vede solo dentro quell'area.
 */
public abstract class SpriteBase implements SpriteInterface {

	protected BufferedImage immagine;
	// Il tempo, il movimento e la dissolvenza: sono del modulo comune, qui resta il disegno
	private ModelloDelloSprite modello;

	/**
	 * Va richiamato dal costruttore della sottoclasse, dopo {@link #buildImage()},
	 * quando i campi necessari a costruire l'immagine sono già stati impostati.
	 */
	protected final void inizializza(BufferedImage immagine,
									 float durataInSecondi, float attaccoDissolvenzaDopoSecondi, Dissolvenza dissolvenza,
									 Color coloreOmbra, int scostamentoOmbra,
									 float xIniziale, float yIniziale,
									 float xFinale, float yFinale,
									 float scalaIniziale, float scalaFinale) {
		this.immagine = coloreOmbra != null ? applicaOmbra(immagine, coloreOmbra, scostamentoOmbra) : immagine;
		this.modello = new ModelloDelloSprite(durataInSecondi, attaccoDissolvenzaDopoSecondi, dissolvenza,
				xIniziale, yIniziale, xFinale, yFinale, scalaIniziale, scalaFinale);
	}

	private BufferedImage applicaOmbra(BufferedImage sorgente, Color colore, int scostamento) {
		BufferedImage ombra = ImageCache.sagomaScura(sorgente, colore);
		int larghezzaTotale = sorgente.getWidth() + scostamento * 2;
		int altezzaTotale = sorgente.getHeight() + scostamento * 2;
		BufferedImage risultato = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = risultato.createGraphics();
		int[] scostamenti = {-scostamento, 0, scostamento};
		for (int dx : scostamenti) {
			for (int dy : scostamenti) {
				if (dx != 0 || dy != 0) {
					g.drawImage(ombra, scostamento + dx, scostamento + dy, null);
				}
			}
		}
		g.drawImage(sorgente, scostamento, scostamento, null);
		g.dispose();
		return risultato;
	}
	/**
	 * Costruisce l'immagine dello sprite. Va richiamato esplicitamente dal costruttore
	 * della sottoclasse (mai da {@link SpriteBase}), quando i campi che servono a
	 * costruirla sono già stati impostati.
	 */
	protected abstract BufferedImage buildImage();

	/**
	 * Limita il disegno dello sprite a quest'area, in coordinate del canvas (se ne tiene una copia)
	 */
	public final void setRitaglio(Rectangle ritaglio) {
		// Una copia: i riquadri spostano la loro area (y) quando cambiano posizione
		modello.setRitaglio(ritaglio == null ? null : new Area(ritaglio.x, ritaglio.y, ritaglio.width, ritaglio.height));
	}

	@Override
	public final void anima(Graphics2D g) {
		if (!modello.prepara()) {
			return;
		}
		Area ritaglio = modello.getRitaglio();
		// Copia del contesto, così composite, hint e colori impostati qui o in disegna()
		// non ricadono su chi disegna dopo: su un Graphics2D nuovo l'hint di interpolazione
		// vale null e non si potrebbe ripristinare, lasciando il bilineare (su Retina,
		// dove c'è già una scala 2x, sfoca e scurisce tutto quello che segue).
		Graphics2D gSprite = (Graphics2D) g.create();
		try {
			if (ritaglio != null) {
				gSprite.clip(new Rectangle(ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza()));
			}
			gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, modello.getAlpha()));
			gSprite.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			disegna(gSprite, modello.getX(), modello.getY(), modello.getScala());
		} finally {
			gSprite.dispose();
		}
		modello.aggiornaConOrologio(System.nanoTime());
	}

	/**
	 * Sposta orizzontalmente lo sprite, fermo in x, in modo che il suo punto di ancoraggio sia quello: per metterlo a posto dopo averlo creato.
	 */
	protected final void impostaX(float x) {
		modello.impostaX(x);
	}

	/**
	 * Azzera il tempo trascorso, per far ripartire da capo dissolvenze/interpolazioni
	 * (es. un nuovo impulso dello stesso fumetto): anche l'ultima misura dell'orologio, così la prossima {@link #anima} non misura da prima del reset.
	 */
	protected final void resettaTempoTrascorso() {
		modello.resettaTempoTrascorso();
	}

	/**
	 * Disegna lo sprite in quella posizione e a quella scala.
	 */
	public abstract void disegna(Graphics2D g, float x, float y, float scala);

	@Override
	public final boolean isAttivo() {
		return modello.isAttivo();
	}
}
