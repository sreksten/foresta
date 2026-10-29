package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La base degli sprite con un'immagine che si muove da un punto a un altro, cambia scala e sfuma. La sottoclasse costruisce l'immagine ({@link #buildImage()}), dice come disegnarla e come
 * cresce la trasparenza e la richiama {@link #inizializza}; la base misura il tempo reale che passa fra una chiamata e l'altra di {@link #anima(Graphics2D)}, calcola posizione e scala per interpolazione
 * e disattiva lo sprite alla fine. Se si indica un ritaglio, lo sprite si vede solo dentro quell'area.
 */
public abstract class SpriteBase implements SpriteInterface {

	protected BufferedImage immagine;
	protected float durataInSecondi;
	protected float momentoInizioFade;
	private float xIniziale;
	private float yIniziale;
	private float xFinale;
	private float yFinale;
	private float scalaIniziale;
	private float scalaFinale;
	protected float secondiTrascorsi;
	protected boolean attivo = true;
	// Se impostata, lo sprite si vede solo dentro quest'area (in coordinate del canvas): parte dal punto giusto anche
	// quando questo è fuori vista, e compare man mano che si muove
	private Rectangle ritaglio;
	// -1 = nessuna chiamata precedente ad anima(): il primo impulso non fa avanzare
	// secondiTrascorsi (idem al comportamento storico a passo fisso), i successivi
	// misurano il tempo reale trascorso invece di assumere una chiamata ogni 1/30 di
	// secondo, assunzione non più valida da quando anima() può essere invocato anche
	// da un repaint() estraneo all'animatore (es. per l'hover del mouse).
	private long ultimoAggiornamentoNanos = -1;

	/**
	 * Va richiamato dal costruttore della sottoclasse, dopo {@link #buildImage()},
	 * quando i campi necessari a costruire l'immagine sono già stati impostati.
	 */
	protected final void inizializza(BufferedImage immagine,
									 float durataInSecondi, float attaccoDissolvenzaDopoSecondi,
									 Color coloreOmbra, int scostamentoOmbra,
									 float xIniziale, float yIniziale,
									 float xFinale, float yFinale,
									 float scalaIniziale, float scalaFinale) {
		this.immagine = coloreOmbra != null ? applicaOmbra(immagine, coloreOmbra, scostamentoOmbra) : immagine;
		this.durataInSecondi = durataInSecondi;
		this.momentoInizioFade = attaccoDissolvenzaDopoSecondi;
		this.xIniziale = xIniziale;
		this.yIniziale = yIniziale;
		this.xFinale = xFinale;
		this.yFinale = yFinale;
		this.scalaIniziale = scalaIniziale;
		this.scalaFinale = scalaFinale;
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
		this.ritaglio = ritaglio == null ? null : new Rectangle(ritaglio);
	}

	@Override
	public final void anima(Graphics2D g) {
		if (!attivo) {
			return;
		}
		if (secondiTrascorsi >= durataInSecondi) {
			attivo = false;
			return;
		}

		float progresso = secondiTrascorsi / durataInSecondi;

		// Calcola il valore della trasparenza in base al tempo trascorso
		float alpha = calcolaAlpha(secondiTrascorsi);

		// Calcola lo spostamento in base al tempo trascorso
		float x = interpola(xIniziale, xFinale, progresso);
		float y = interpola(yIniziale, yFinale, progresso);

		// Calcola la scala da applicare in base al tempo trascorso
		float scala = interpola(scalaIniziale, scalaFinale, progresso);

		// Copia del contesto, così composite, hint e colori impostati qui o in disegna()
		// non ricadono su chi disegna dopo: su un Graphics2D nuovo l'hint di interpolazione
		// vale null e non si potrebbe ripristinare, lasciando il bilineare (su Retina,
		// dove c'è già una scala 2x, sfoca e scurisce tutto quello che segue).
		Graphics2D gSprite = (Graphics2D) g.create();
		try {
			if (ritaglio != null) {
				gSprite.clip(ritaglio);
			}
			gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			gSprite.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			disegna(gSprite, x, y, scala);
		} finally {
			gSprite.dispose();
		}

		long ora = System.nanoTime();
		if (ultimoAggiornamentoNanos >= 0) {
			secondiTrascorsi += (ora - ultimoAggiornamentoNanos) / 1_000_000_000f;
		}
		ultimoAggiornamentoNanos = ora;
	}

	/**
	 * Azzera il tempo trascorso, per far ripartire da capo dissolvenze/interpolazioni
	 * (es. un nuovo impulso dello stesso fumetto). Va usato al posto di assegnare
	 * direttamente {@link #secondiTrascorsi}, perché deve anche invalidare l'ultimo
	 * timestamp reale registrato: altrimenti la prossima {@link #anima} misurerebbe
	 * il tempo trascorso da una chiamata precedente al reset, non da esso.
	 */
	protected final void resettaTempoTrascorso() {
		secondiTrascorsi = 0;
		ultimoAggiornamentoNanos = -1;
	}

	/**
	 * L'opacità dello sprite, da 0 (invisibile) a 1, dopo quel tempo dall'inizio.
	 */
	protected abstract float calcolaAlpha(float secondiTrascorsi);

	/**
	 * Disegna lo sprite in quella posizione e a quella scala.
	 */
	public abstract void disegna(Graphics2D g, float x, float y, float scala);

	@Override
	public final boolean isAttivo() {
		return attivo;
	}

	/**
	 * Il valore che sta a quel progresso (da 0 a 1) fra l'iniziale e il finale.
	 */
	protected static float interpola(float iniziale, float finale, float progresso) {
		return iniziale + (finale - iniziale) * progresso;
	}

	/**
	 * Una dissolvenza che non comincia finché non passa la soglia e poi sfuma con una curva iperbolica, rapida all'inizio e lenta in coda.
	 */
	protected static float dissolvenzaIperbolicaConSoglia(float secondiTrascorsi, float sogliaInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		// Curva tarata sui tick storici da 0.1s (1/(ticks-limite)): moltiplicando per 10
		// i secondi oltre soglia si ottiene lo stesso andamento.
		return Math.min(1.0f, 1.0f / ((secondiTrascorsi - sogliaInSecondi) * 10));
	}

	/**
	 * Una dissolvenza che non comincia finché non passa la soglia e poi sfuma in linea retta fino alla fine della durata.
	 */
	protected static float dissolvenzaLineareConSoglia(float secondiTrascorsi, float sogliaInSecondi, float durataInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		return Math.max(0.0f, 1.0f - (secondiTrascorsi - sogliaInSecondi) / (durataInSecondi - sogliaInSecondi));
	}
}
