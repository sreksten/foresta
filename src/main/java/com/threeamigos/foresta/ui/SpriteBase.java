package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

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
		BufferedImage ombra = creaSagomaScura(sorgente, colore);
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

		Composite compositeOriginale = g.getComposite();
		Object interpolazioneOriginale = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		disegna(g, x, y, scala);

		g.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
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

	protected abstract float calcolaAlpha(float secondiTrascorsi);

	protected abstract void disegna(Graphics2D g, float x, float y, float scala);

	@Override
	public final boolean isAttivo() {
		return attivo;
	}

	protected static float interpola(float iniziale, float finale, float progresso) {
		return iniziale + (finale - iniziale) * progresso;
	}

	protected static float dissolvenzaIperbolicaConSoglia(float secondiTrascorsi, float sogliaInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		// Curva tarata sui tick storici da 0.1s (1/(ticks-limite)): moltiplicando per 10
		// i secondi oltre soglia si ottiene lo stesso andamento.
		return Math.min(1.0f, 1.0f / ((secondiTrascorsi - sogliaInSecondi) * 10));
	}

	protected static float dissolvenzaLineareConSoglia(float secondiTrascorsi, float sogliaInSecondi, float durataInSecondi) {
		if (secondiTrascorsi <= sogliaInSecondi) {
			return 1.0f;
		}
		return Math.max(0.0f, 1.0f - (secondiTrascorsi - sogliaInSecondi) / (durataInSecondi - sogliaInSecondi));
	}

	private static BufferedImage creaSagomaScura(BufferedImage sorgente, Color colore) {
		BufferedImage sagoma = new BufferedImage(sorgente.getWidth(), sorgente.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sagoma.createGraphics();
		g.drawImage(sorgente, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setColor(colore);
		g.fillRect(0, 0, sagoma.getWidth(), sagoma.getHeight());
		g.dispose();
		return sagoma;
	}
}
